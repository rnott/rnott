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

package org.rnott.mock.evaluators;

import org.rnott.mock.Evaluator;
import org.rnott.mock.MockContext;
import org.rnott.mock.MockHttpServletRequest;
import org.rnott.mock.evaluators.RequestEvaluator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * TODO: document RequestEvaluatorTest
 *
 */
public class RequestEvaluatorTest {

	@DataProvider(name = "methods")
	Object [][] methods() {
		return new Object [][] {
			{ "session", null },
		};
	}

	@DataProvider(name = "invalid")
	Object [][] invalid() {
		return new Object [][] {
			{ "method", null },
			{ "method", new Object [] {} },
			{ "method", new Object [] { "1234" } },
			{ "method", new Object [] { "1234", false } },
		};
	}

	@Test
	public void key() {
		Evaluator eval = new RequestEvaluator();
		assert "request".equals( eval.key() ) : "Unexpected key: " + eval.key() + ", expected 'request'";
	}

	@Test(dataProvider = "methods")
	public void evaluate( String method, Object [] args ) {
		MockContext.REQUESTS.set( new MockHttpServletRequest() );
		Object value = new RequestEvaluator().evaluate( method, args );
		assert value != null : "Expected non-null value";
	}

	//@Test(dataProvider = "invalid", expectedExceptions = IllegalStateException.class)
	public void evaluate_IllegalStateException( String method, Object [] args ) {
		MockContext.REQUESTS.set( new MockHttpServletRequest() );
		new RequestEvaluator().evaluate( method, args );
	}
}
