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

import java.util.Map;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.rnott.mock.ExpressionLanguageLexer;
import org.rnott.mock.ExpressionLanguageParser;
import org.rnott.mock.evaluators.Evaluator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * Verify the expression language grammar.
 */
public class ExpressionLanguageEvaluaterTest {


	private String evaluate( String text ) {
		if ( text == null ) {
			return null;
		}
		return evaluate( new ExpressionLanguageLexer( new ANTLRInputStream( text ) ) );
	}

	private String evaluate( ExpressionLanguageLexer lexer ) {
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		ExpressionLanguageParser parser = new ExpressionLanguageParser( tokens );
		lexer.removeErrorListeners();
		parser.removeErrorListeners();
		ParserErrorListener.register( lexer, parser );
	    ParseTree tree = parser.content();
	    ParseTreeWalker walker = new ParseTreeWalker();
	    ExpressionLanguageEvaluator evaluator = new ExpressionLanguageEvaluator();
	    walker.walk( evaluator, tree );

	    return evaluator.getText();
	}

	@DataProvider(name = "expressions")
	public Object [][] expressions() {
		Map<String, String> params = MockContext.get().getParameters();
		params.put( "foo", "bar" );
		params.put( "foo.bar", "foobar" );

		MockContext.get().addEvaluator( new Evaluator() {
			@Override
            public String key() {
	            return "test";
            }
			@Override
            public Object evaluate( String text, Object... args ) {
	            StringBuilder sb = new StringBuilder( this.key() ).append( ':' ).append( text );
	            for ( Object o : args ) {
	            	sb.append( ':' ).append( o );
	            }
	            return sb;
            }
		});
		return new Object [][] {
			// CHARACTERS
			{ "abcdefghijklmnopqrstuvwxyz 01234567890.,;:[]{}()_-+~!@#$%^&*/", "abcdefghijklmnopqrstuvwxyz 01234567890.,;:[]{}()_-+~!@#$%^&*/" },
			// ID (identifier)
			{ "a123", "a123" },
			// CID (complex identifier)
			{ "aa.bb", "aa.bb" },
			// COMMA
			{ ",", "," },
			// DOT
			{ ".", "." },
			// DOLLAR
			{ "$", "$" },
			// CURLY_OPEN
			{ "{", "{" },
			// OPEN_BRACKET
			{ "[", "[" },
			// CLOSE_BRACKET
			{ "]", "]" },
			// EXP_END
			{ "}", "}" },
			// OPEN_PARAMS
			{ "(", "(" },
			// CLOSE_PARAMS
			{ ")", ")" },
			// STRING
			{ "'abc'", "'abc'" },
			{ "\"abc\"", "\"abc\"" },
			{ "'st\"ring'", "'st\"ring'" },
			// INT
			{ "1234", "1234" },
			// 'true'
			{ "true", "true" },
			// 'false'
			{ "false", "false" },
			// 'null'
			{ "null", "null" },
			{ "'${foo}'", "'${foo}'" },
			{ "\"${foo}\"", "\"bar\"" },
			// ESCAPED
			{ "\\", "\\" },
			{ "a\\bcd", "a\\bcd" },
			{ "a\\$bcd", "a$bcd" },
			{ "a$\\{bcd", "a${bcd" },
			{ "a$\\{bcd}", "a${bcd}" },
			{ "a$\\{bcd\\}", "a${bcd}" },
			{ "'st\\'ring'", "'st\\'ring'" },
			{ "'st\\\\ring'", "'st\\\\ring'" },
			{ "\"st'ring\"", "\"st'ring\"" },
			{ "\"st\\\"ring\"", "\"st\\\"ring\"" },
			{ "\"st\\\\ring\"", "\"st\\\\ring\"" },
			// property
			{ "${foo}", "bar" },
			{ "${foo.bar}", "foobar" },
			{ "\\${foo}", "${foo}" },
			{ "$\\{foo}", "${foo}" },
			{ "{foo}", "{foo}" },
			// method
			{ "${test.function()}", "test:function" },
			{ "${test.function('abc')}", "test:function:abc" },
			{ "${test.function(1)}", "test:function:1" },
			{ "${test.function(1L)}", "test:function:1" },
			{ "${test.function('a','b','c')}", "test:function:a:b:c" },
			{ "${test.function(${foo})}", "test:function:bar" },
			{ "${test.function(${test.function(${foo})})}", "test:function:test:function:bar" },
		};
	}

	@DataProvider(name = "invalidExpressions")
	public Object [][] invalidExpressions() {
		Map<String, String> params = MockContext.get().getParameters();
		params.put( "foo", "bar" );
		return new Object [][] {
			// no such property
			{ "${foobar}" },
			// no built-in evaluator
			{ "${foo.bar()}" },
			// no evaluator
			{ "${flubber.test()}" },
			{ "${test.function(${flubber.test()})}" },
			// parser exceptions
			{ "${" },
			{ "${}" },
		};
	}

	@Test(dataProvider = "expressions")
	public void evaluate( String expression, String expected ) {
		String s = evaluate( expression );
		assert s != null : "Evaluated text is NULL";
		assert s.equals( expected ) : "Unexpected evaluated text: '" + s + "', expected '" + expected + "'";
	}

	@Test(dataProvider = "invalidExpressions", expectedExceptions = IllegalStateException.class)
	public void evaluate_IllegalStateException( String expression ) {
		evaluate( expression );
	}
}
