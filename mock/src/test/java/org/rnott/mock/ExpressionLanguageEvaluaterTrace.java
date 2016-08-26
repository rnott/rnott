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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.rnott.mock.ExpressionLanguageBaseListener;
import org.rnott.mock.ExpressionLanguageLexer;
import org.rnott.mock.ExpressionLanguageParser;
import org.rnott.mock.ExpressionLanguageParser.ContentContext;
import org.rnott.mock.ExpressionLanguageParser.ExpressionContext;
import org.rnott.mock.ExpressionLanguageParser.LiteralContext;
import org.rnott.mock.ExpressionLanguageParser.MethodContext;
import org.rnott.mock.ExpressionLanguageParser.ParameterContext;
import org.rnott.mock.ExpressionLanguageParser.ParametersContext;
import org.rnott.mock.ExpressionLanguageParser.PropertyContext;
import org.rnott.mock.ExpressionLanguageParser.VerbatimContext;


/**
 * TODO: document ExpressionLanguageEvaluaterTrace
 *
 */
public class ExpressionLanguageEvaluaterTrace {


	public static class ExpressionLanguageListenerImpl extends ExpressionLanguageBaseListener {
		private boolean debug = false;
		private StringBuilder content = new StringBuilder();

		public String getContent() {
			return "";
			//return content == null ? null : content.toString();
		}

		@Override
		public void visitErrorNode( ErrorNode node ) {
			super.visitErrorNode( node );
			System.out.println( "Error in input at line "
				+ ((org.antlr.v4.runtime.CommonToken)node.getPayload()).getLine() + ":"
				+ ((org.antlr.v4.runtime.CommonToken)node.getPayload()).getCharPositionInLine() );
		}

		@Override
		public void enterVerbatim( VerbatimContext ctx ) {
			super.enterVerbatim( ctx );
			content.append( ctx.getText() );
			if ( debug ) {
				System.out.print( " START: VERBATIM [" + ctx.getText() + "]" );
			}
		}

		@Override
		public void exitVerbatim( VerbatimContext ctx ) {
			super.exitVerbatim( ctx );
			if ( debug ) {
				System.out.print( " END: VERBATIM" );
			}
		}

		@Override
		public void enterExpression( ExpressionContext ctx ) {
			super.enterExpression( ctx );
			if ( debug ) {
				System.out.print( " START: EXPRESSION [" + ctx.getText() + "]" );
			}
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

		@Override
		public void exitExpression( ExpressionContext ctx ) {
			super.exitExpression( ctx );
			if ( debug ) {
				System.out.print( " END: EXPRESSION" );
			}
		}

		@Override
		public void enterContent( ContentContext ctx ) {
			super.enterContent( ctx );
			if ( debug ) {
				System.out.println( "START: CONTENT" );
			}
		}

		@Override
		public void exitContent( ContentContext ctx ) {
			super.exitContent( ctx );
			if ( debug ) {
				System.out.println( " END: CONTENT" );
			}
		}

		@Override
		public void enterLiteral( LiteralContext ctx ) {
			super.enterLiteral( ctx );
			if ( debug ) {
				System.out.print( " START: LITERAL [" + ctx.getText() + "]" );
			}
		}

		@Override
		public void exitLiteral( LiteralContext ctx ) {
			super.exitLiteral( ctx );
			if ( debug ) {
				System.out.print( " END: LITERAL" );
			}
		}

		@Override
		public void enterProperty( PropertyContext ctx ) {
			super.enterProperty( ctx );
			if ( debug ) {
				System.out.print( " START: PROPERTY [" + ctx.getText() + "]" );
			}
		}

		@Override
		public void exitProperty( PropertyContext ctx ) {
			super.exitProperty( ctx );
			if ( debug ) {
				System.out.print( " END: PROPERTY" );
			}
		}

		@Override
		public void enterMethod( MethodContext ctx ) {
			super.enterMethod( ctx );
			if ( debug ) {
				System.out.print( " START: METHOD [" + ctx.getText() + "]" );
			}
		}

		@Override
		public void exitMethod( MethodContext ctx ) {
			super.exitMethod( ctx );
			if ( debug ) {
				System.out.print( " END: METHOD" );
			}
		}

		@Override
		public void enterParameter( ParameterContext ctx ) {
			super.enterParameter( ctx );
			if ( debug ) {
				System.out.print( " START: PARAMETER [" + ctx.getText() + "]" );
			}
		}

		@Override
		public void exitParameter( ParameterContext ctx ) {
			super.exitParameter( ctx );
			if ( debug ) {
				System.out.print( " END: PARAMETER" );
			}
		}

		@Override
		public void enterParameters( ParametersContext ctx ) {
			super.enterParameters( ctx );
			if ( debug ) {
				System.out.print( " START: PARAMETERS [" + ctx.getText() + "]" );
			}
		}

		@Override
		public void exitParameters( ParametersContext ctx ) {
			super.exitParameters( ctx );
			if ( debug ) {
				System.out.print( " END: PARAMETERS" );
			}
		}

		private Object evaluate( PropertyContext ctx ) {
			return ctx.getChild( 1 ).getText().toUpperCase();
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

			StringBuilder sb = new StringBuilder().append( type ).append( "." ).append( method ).append( "(" );
			for ( Object o : params ) {
				sb.append( o ).append( ' ' );
			}
			sb.append(  ")" );

			return sb.toString();
		}

		private Object evaluate( LiteralContext ctx ) {
			return ctx.getText();
		}
	}

	public String evaluate( String text ) {
		if ( text == null ) {
			return null;
		}
		System.out.print( text  + " => " );

		return evaluate( new ExpressionLanguageLexer( new ANTLRInputStream( text ) ) );
	}

	public String evaluate( InputStream in ) throws IOException {
		if ( in == null ) {
			return null;
		}

		return evaluate( new ExpressionLanguageLexer( new ANTLRInputStream( in ) ) );
	}

	private String evaluate( ExpressionLanguageLexer lexer ) {
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		ExpressionLanguageParser parser = new ExpressionLanguageParser( tokens );
	    ParseTree tree = parser.content();
	    ParseTreeWalker walker = new ParseTreeWalker();
	    ExpressionLanguageListenerImpl listener = new ExpressionLanguageListenerImpl();
	    listener.debug = true;
	    walker.walk( listener, tree );

	    return listener.getContent();
	}

	public static final void main( String [] args ) throws Throwable {
		ExpressionLanguageEvaluaterTrace e = new ExpressionLanguageEvaluaterTrace();

		/*
		 * CHARACTER (verbatim)
		 */
		System.out.println( e.evaluate( "character" ) );
		System.out.println( e.evaluate( "aa1234bb" ) );
		System.out.println( e.evaluate( "aa12bb34cc" ) );

		/*
		 * Literals
		 */
		System.out.println( e.evaluate( "1234" ) );
		System.out.println( e.evaluate( "'string'" ) );
		System.out.println( e.evaluate( "'st\"ring'" ) );
		System.out.println( e.evaluate( "'st\\'ring'" ) );
		System.out.println( e.evaluate( "'st\\\\ring'" ) );
		System.out.println( e.evaluate( "\"string\"" ) );
		System.out.println( e.evaluate( "\"st'ring\"" ) );
		System.out.println( e.evaluate( "\"st\\\"ring\"" ) );
		System.out.println( e.evaluate( "\"st\\\\ring\"" ) );

		/*
		 * Parameters
		 */
		System.out.println( e.evaluate( "${foobar}" ) );
		System.out.println( e.evaluate( "${foo.bar}" ) );
		System.out.println( e.evaluate( "${foo.bar.bas}" ) );
		System.out.println( e.evaluate( "preamble:${test}" ) );
		System.out.println( e.evaluate( "preamble:${test}:epilogue" ) );

		/*
		 * Methods
		 */
		System.out.println( e.evaluate( "${date.now()}" ) );
		System.out.println( e.evaluate( "${date.now('yyyy-MM-dd\\'T\\'HH:mm Z')}" ) );
		System.out.println( e.evaluate( "${request.foo()}" ) );
		System.out.println( e.evaluate( "${request.foo('args')}" ) );
		System.out.println( e.evaluate( "${request.foo(1234)}" ) );
		System.out.println( e.evaluate( "${request.foo('args',1234)}" ) );
		System.out.println( e.evaluate( "${request.foo(${foobar})}" ) );
		System.out.println( e.evaluate( "${request.foo(${foobar},${request.foo('args',1234)})}" ) );
		System.out.println( e.evaluate( "${request.foo(${foobar},${request.foo(${foo.bar},1234)})}" ) );

		// TODO: load from file
		//e.evaluate( ExpressionLanguageEvaluaterTrace.class.getResourceAsStream( "/test.json" ) );
	}
}
