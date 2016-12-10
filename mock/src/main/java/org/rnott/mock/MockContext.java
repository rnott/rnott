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


import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.rnott.mock.ExpressionLanguageLexer;
import org.rnott.mock.ExpressionLanguageParser;
import org.rnott.mock.evaluators.DateEvaluator;
import org.rnott.mock.evaluators.Evaluator;
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
	 * Register an EL evaluator extension.
	 * <p>
	 * @param evaluator the evaluator implementation.
	 */
	public void addEvaluator( Evaluator evaluator ) {
		managed.put( evaluator.key(), evaluator );
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
		lexer.removeErrorListeners();
		parser.removeErrorListeners();
		ParserErrorListener.register( lexer, parser );
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
	public Evaluator getEvaluator( String key ) {
		if ( managed.containsKey( key ) ) {
			return managed.get( key );
		}
		throw new IllegalStateException( "Evaluator key not registered: " + key );
	}
}
