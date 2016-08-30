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
 * Global service context cache. Each request thread is provided a private instance
 * of this context that can be used to stash references for use later in the request
 * processing chain. Put another way, this provides the means for loosely coupled
 * components to share data with each other.
 */
public class MockContext {

	/*
	 * Global mock context cache. Each request thread has one that can be used to share state between loosely
	 * coupled components.
	 */
	private static final ThreadLocal<MockContext> CONTEXTS = new ThreadLocal<MockContext>() {

		@Override
		protected MockContext initialValue() {
			return new MockContext();
		}
	};

	/*
	 * Global HTTP parameter cache. Each request thread has one that can be used to share request
	 * parameters between loosely coupled components. 
	 */
	private static final ThreadLocal<Map<String, String>> PARAMS = new ThreadLocal<Map<String, String>>() {

		@Override
		protected Map<String, String> initialValue() {
			return new HashMap<String, String>();
		}
	};

	/**
	 * Global HTTP request cache. Each request thread has one that can be used to share state between loosely
	 * coupled components.
	 */
	public static final ThreadLocal<HttpServletRequest> REQUESTS = new ThreadLocal<HttpServletRequest>() {

		@Override
		protected HttpServletRequest initialValue() {
			throw new IllegalStateException( "Cannot create request, must use set() instead" );
		}
	};

	/**
	 * Get the context associated with the current thread.
	 * <p>
	 * @return the associated context.
	 */
	public static MockContext get() {
		return CONTEXTS.get();
	}

	/**
	 * Expression Language (EL) evaluator. Instances of this type are used to transform
	 * a service request according to the EL syntax.
	 */
	private static class ExpressionLanguageEvaluator extends ExpressionLanguageBaseListener {

		private StringBuilder content = new StringBuilder();

		/**
		 * Get the transformed text.
		 * <p>
		 * @return the transformed text.
		 */
		public String getText() {
			return content.toString();
		}

		/**
		 * Process verbatim text.
		 * <p>
		 * @param the EL context.
		 */
		@Override
		public void enterVerbatim( VerbatimContext ctx ) {
			// append verbatim text
			super.enterVerbatim( ctx );
			content.append( ctx.getText() );
		}

		/**
		 * Begin processing an EL expression.
		 * <p>
		 * @param ctx the EL expression context.
		 */
		@Override
		public void enterExpression( ExpressionContext ctx ) {
			// expression is an EL method or property
			super.enterExpression( ctx );
			ParserRuleContext p = (ParserRuleContext) ctx.getChild( 0 );
			switch ( p.getRuleIndex() ) {
			case ExpressionLanguageParser.RULE_method:
				// append invocation of an EL method
				content.append( evaluate( (MethodContext) p ) );
				break;
			case ExpressionLanguageParser.RULE_property:
				// append resolution of an EL property
				content.append(  evaluate( (PropertyContext) p ) );
				break;
			}
		}

		/**
		 * Evaluate an EL literal.
		 * <p>
		 * @param ctx the EL literal context.
		 * @return the resolved value.
		 */
		private Object evaluate( LiteralContext ctx ) {
			String s = ctx.getText();
			switch ( ((CommonToken) ctx.getChild( 0 ).getPayload()).getType() ) {
			case ExpressionLanguageParser.STRING:
				// remove surrounding quotes
				return s.substring( 1, s.length() - 1 );
			case ExpressionLanguageParser.INT:
				// parse as integer
				return Integer.parseInt( s );
			case ExpressionLanguageParser.LONG:
				// parse as long
				return Long.parseLong( s );
			}
			return s;
		}

		/**
		 * Evaluate an EL property.
		 * <p>
		 * @param ctx the EL property context.
		 * @return the resolved value.
		 * @throws IllegalStateException if the property specified by the expression
		 * is not a request parameter.
		 */
		private Object evaluate( PropertyContext ctx ) {
			Map<String, String> params = MockContext.get().getParameters();
			String key = ctx.getChild( 1 ).getText();
			if ( params.containsKey( key ) ) {
				return params.get( key );
			}
			throw new IllegalStateException( "Property not present as a request parameter: " + key + " " + params.keySet() );
		}

		/**
		 * Evaluate an EL method.
		 * <p>
		 * @param ctx the EL method context.
		 * @return the resolved value.
		 * @throws IllegalStateException if the EL method specifies an unknown evaluator.
		 */
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

	/*
	 * Collection of managed evaluators.
	 */
	private final Map<String, Evaluator> managed = new HashMap<String, Evaluator>();

	/**
	 * Construct a new thread-private context. All known evaluators are registered
	 * during construction.
	 */
	private MockContext() {
		// register EL evaluators
		// TODO: implement a more sophisticated means of registering Evaluator instances.
		Evaluator e = new RandomEvaluator();
		managed.put( e.key(), e );
		e = new RequestEvaluator();
		managed.put( e.key(), e );
		e = new DateEvaluator();
		managed.put( e.key(), e );
		e = new StringEvaluator();
		managed.put( e.key(), e );
	}

	/**
	 * Get any request parameters associated with the current thread.
	 * <p>
	 * @return the request parameters associated with the thread. The collection
	 * may be empty but never <code>null</code>.
	 */
	public Map<String, String> getParameters() {
		return PARAMS.get();
	}

	/**
	 * Get the HTTP request associated with the current thread.
	 * <p>
	 * @return the associated HTTP request.
	 */
	public HttpServletRequest getRequest() {
		return REQUESTS.get();
	}

	/**
	 * Associate an HTTP request with the current thread.
	 * <p>
	 * @param request the request to be associated with the thread.
	 */
	public void setRequest( HttpServletRequest request ) {
		// register the request associated with the thread
		REQUESTS.set( request );
	}

	/**
	 * Evaluate the provided text using the built-in expression language.
	 * <p>
	 * @param text the text to process.
	 * @return the resolved text or <code>null</code> if no text was provided.
	 */
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

	/**
	 * Resolved the evaluator bound to the specified key.
	 * <p>
	 * @param key the evaluator identifier.
	 * @return the evaluator bound to the key.
	 * @throws IllegalStateException if no evaluator is bound to the specified key.
	 */
	private Evaluator getEvaluator( String key ) {
		if ( managed.containsKey( key ) ) {
			return managed.get( key );
		}
		throw new IllegalStateException( "Evaluator key not registered: " + key );
	}
}
