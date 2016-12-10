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

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;


/**
 * React to parser errors by throwing an exception rather than simply
 * logging to STDERR.
 */
public class ParserErrorListener extends BaseErrorListener {

	public static void register( ExpressionLanguageLexer lexer, ExpressionLanguageParser parser ) {
		ParserErrorListener instance = new ParserErrorListener();
		lexer.addErrorListener( instance );
		parser.addErrorListener( instance );
	}

	/*
	 * (non-Javadoc)
	 * @see org.antlr.v4.runtime.BaseErrorListener#syntaxError(org.antlr.v4.runtime.Recognizer, java.lang.Object, int, int, java.lang.String, org.antlr.v4.runtime.RecognitionException)
	 */
	@Override
	public void syntaxError( Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e )
	      throws ParseCancellationException
	{
		throw new ParseCancellationException( msg + " (line " + line + ":" + charPositionInLine + ")" );
	}
}
