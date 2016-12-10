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
import org.rnott.mock.evaluators.DateEvaluator;
import org.rnott.mock.evaluators.Evaluator;
import org.rnott.mock.evaluators.RandomEvaluator;
import org.rnott.mock.evaluators.RequestEvaluator;
import org.rnott.mock.evaluators.StringEvaluator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * Test functionality of <code>MockContext</code>.
 * <p>
 * @see MockContext
 */
public class MockContextTest {

	@DataProvider(name = "evaluators")
	public Object [][] evaluators() {
		return new Object [][] {
			{ new RandomEvaluator().key(), RandomEvaluator.class },	
			{ new RequestEvaluator().key(), RequestEvaluator.class },	
			{ new DateEvaluator().key(), DateEvaluator.class },	
			{ new StringEvaluator().key(), StringEvaluator.class },	
		};
	}

	@DataProvider(name = "expressions")
	public Object [][] expressions() {
		return new Object [][] {
			{ "${foo}", "bar" },
		};
	}

	@Test
	public void get() {
		assert MockContext.get() != null : "Mock context is NULL";
	}

	@Test(dependsOnMethods = "get")
	public void getParameters() {
		assert MockContext.get().getParameters() != null : "Mock context parameters are NULL";
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void getRequest_IllegalStateException() {
		MockContext.get().getRequest();
	}

	@Test(dependsOnMethods = {"get", "getRequest_IllegalStateException"})
	public void setRequest() {
		MockContext.get().setRequest( new MockHttpServletRequest() );
	}

	@Test(dependsOnMethods = "setRequest")
	public void getRequest() {
		assert MockContext.get().getRequest() != null : "Mock context request is NULL";
	}

	@Test(dependsOnMethods = "get", dataProvider = "evaluators")
	public void getEvaluator( String key, Class<Evaluator> type ) {
		Evaluator e = MockContext.get().getEvaluator( key );
		assert e != null : "No evaluator for key: " + key;
		assert type.isAssignableFrom( e.getClass() );
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void getEvaluator_IllegalStateException() {
		MockContext.get().getEvaluator( "missing" );
	}

	@Test(dependsOnMethods = "getEvaluator")
	public void addEvaluator() {
		Evaluator e = new Evaluator() {
			@Override
            public String key() {
	            return "test";
            }
			@Override
            public Object evaluate( String text, Object... args ) {
	            return null;
            }
		};
		MockContext context = MockContext.get();
		context.addEvaluator( e ); 
		assert context.getEvaluator( "test" ) != null : "No evaluator reigstered for key: test";
		assert context.getEvaluator( "test" ) == e : "Unexpected evaluator for key: test";
	}

	@Test(dataProvider = "expressions", dependsOnMethods = "getParameters")
	public void evaluate( String expression, String expected ) {
		Map<String, String> params = MockContext.get().getParameters();
		params.put( "foo", "bar" );
		String s = MockContext.get().evaluate( expression );
		assert s != null : "Evaluated text is NULL";
		assert s.equals( expected ) : "Unexpected evaluated text: '" + s + "', expected '" + expected + "'";
	}

	@Test(dependsOnMethods = "get")
	public void evaluate_NULL() {
		assert MockContext.get().evaluate( null ) == null : "Evaluation of NULL should be NULL";
	}
}
