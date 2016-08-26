/*
 * Copyright 2016 Randy Nott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rnott.mock;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.rnott.mock.ExpressionLanguageBaseListener;
import org.rnott.mock.ExpressionLanguageLexer;
import org.rnott.mock.ExpressionLanguageParser;
import org.rnott.mock.ExpressionLanguageParser.ExpressionContext;
import org.rnott.mock.ExpressionLanguageParser.LiteralContext;
import org.rnott.mock.ExpressionLanguageParser.MethodContext;
import org.rnott.mock.ExpressionLanguageParser.ParameterContext;
import org.rnott.mock.ExpressionLanguageParser.ParametersContext;
import org.rnott.mock.ExpressionLanguageParser.PropertyContext;
import org.rnott.mock.ExpressionLanguageParser.VerbatimContext;
import org.rnott.mock.evaluators.DateEvaluator;
import org.rnott.mock.evaluators.RandomEvaluator;
import org.rnott.mock.evaluators.RequestEvaluator;
import org.rnott.mock.evaluators.StringEvaluator;


/**
 * 
 * TODO: document MockContext
 *
 */
public class MockContext {

	private static final ThreadLocal<MockContext> CONTEXTS = new ThreadLocal<MockContext>() {

		@Override
		protected MockContext initialValue() {
			return new MockContext();
		}
	};

	private static final ThreadLocal<Map<String, String>> PARAMS = new ThreadLocal<Map<String, String>>() {

		@Override
		protected Map<String, String> initialValue() {
			return new HashMap<String, String>();
		}
	};

	public static final ThreadLocal<HttpServletRequest> REQUESTS = new ThreadLocal<HttpServletRequest>() {

		@Override
		protected HttpServletRequest initialValue() {
			throw new IllegalStateException( "Cannot create request, must use set() instead" );
		}
	};

	public static MockContext get() {
		return CONTEXTS.get();
	}

	private static class ExpressionLanguageEvaluator extends ExpressionLanguageBaseListener {

		private StringBuilder content = new StringBuilder();

		public String getText() {
			return content.toString();
		}

		@Override
		public void enterVerbatim( VerbatimContext ctx ) {
			super.enterVerbatim( ctx );
			content.append( ctx.getText() );
		}

		@Override
		public void enterExpression( ExpressionContext ctx ) {
			super.enterExpression( ctx );
			ParserRuleContext p = (ParserRuleContext) ctx.getChild( 0 );
			switch ( p.getRuleIndex() ) {
			case ExpressionLanguageParser.RULE_method:
				content.append( evaluate( (MethodContext) p ) );
				break;
			case ExpressionLanguageParser.RULE_property:
				content.append(  evaluate( (PropertyContext) p ) );
				break;
			}
		}

		private Object evaluate( LiteralContext ctx ) {
			String s = ctx.getText();
			switch ( ((CommonToken) ctx.getChild( 0 ).getPayload()).getType() ) {
			case ExpressionLanguageParser.STRING:
				return s.substring( 1, s.length() - 1 );
			case ExpressionLanguageParser.INT:
				return Integer.parseInt( s );
			case ExpressionLanguageParser.LONG:
				return Long.parseLong( s );
			}
			return s;
		}

		private Object evaluate( PropertyContext ctx ) {
			Map<String, String> params = MockContext.get().getParameters();
			String key = ctx.getChild( 1 ).getText();
			if ( params.containsKey( key ) ) {
				return params.get( key );
			}
			throw new IllegalStateException( "Property not present as a request parameter: " + key + " " + params.keySet() );
		}

		private Object evaluate( MethodContext ctx ) {
			String s = ctx.getChild( 1 ).getText();
			int pos = s.indexOf( '.' );
			String type = s.substring( 0, pos );
			String method = s.substring( pos + 1 );
			ParametersContext pc = (ParametersContext) ctx.getChild( 2 );
			List<Object> params = new ArrayList<Object>();
			for ( int i = 1, count = pc.getChildCount() - 1; i < count; i++ ) {
				if ( pc.getChild( i ) instanceof ParameterContext ) {
					ParserRuleContext p = (ParserRuleContext) pc.getChild( i ).getChild( 0 );
					switch ( p.getRuleIndex() ) {
					case ExpressionLanguageParser.RULE_literal:
						params.add( evaluate( (LiteralContext) p ) );
						break;
					case ExpressionLanguageParser.RULE_method:
						params.add( evaluate( (MethodContext) p ) );
						break;
					case ExpressionLanguageParser.RULE_property:
						params.add( evaluate( (PropertyContext) p ) );
						break;
					}
				}
			}

			Evaluator e = MockContext.get().getEvaluator( type );
			if ( e == null ) {
				throw new IllegalStateException( "No registered component to evaulate type: " + type );
			}

			return e.evaluate( method, params.toArray() );
		}
	}

	private final Map<String, Evaluator> managed = new HashMap<String, Evaluator>();

	private MockContext() {
		// register EL evaluators
		Evaluator e = new RandomEvaluator();
		managed.put( e.key(), e );
		e = new RequestEvaluator();
		managed.put( e.key(), e );
		e = new DateEvaluator();
		managed.put( e.key(), e );
		e = new StringEvaluator();
		managed.put( e.key(), e );
	}

	public Map<String, String> getParameters() {
		return PARAMS.get();
	}

	public HttpServletRequest getRequest() {
		return REQUESTS.get();
	}

	public void setRequest( HttpServletRequest request ) {
		// register the request associated with the thread
		REQUESTS.set( request );
	}

	public String evaluate( String text ) {
		if ( text == null ) {
			return text;
		}

		ExpressionLanguageLexer lexer = new ExpressionLanguageLexer( new ANTLRInputStream( text ) );
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		ExpressionLanguageParser parser = new ExpressionLanguageParser( tokens );
	    ParseTree tree = parser.content();
	    ParseTreeWalker walker = new ParseTreeWalker();
	    ExpressionLanguageEvaluator evaluator = new ExpressionLanguageEvaluator();
	    walker.walk( evaluator, tree );

		return evaluator.getText();
	}

	private Evaluator getEvaluator( String key ) {
		if ( managed.containsKey( key ) ) {
			return managed.get( key );
		}
		throw new IllegalStateException( "Evaluator key not registered: " + key );
	}
}
