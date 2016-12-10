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

import org.rnott.mock.evaluators.DateEvaluator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * TODO: document DateEvaluatorTest
 *
 */
public class DateEvaluatorTest {

	/*
	{ "date.now()", "date.now()" },
	{ "${date.now()}", String.valueOf( System.currentTimeMillis() ) },
	System.out.println( e.evaluate( "${date.now('yyyy-MM-dd\\'T\\'HH:mm Z')}" ) );
	 */

	@DataProvider(name = "methods")
	Object [][] methods() {
		return new Object [][] {
			{ "now", null },
			{ "now", new Object [] {} },
			{ "now", new Object [] { "yymmdd" } },
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
		Evaluator eval = new DateEvaluator();
		assert "date".equals( eval.key() ) : "Unexpected key: " + eval.key() + ", expected 'date'";
	}

	@Test(dataProvider = "methods")
	public void evaluate( String method, Object [] args ) {
		Object value = new DateEvaluator().evaluate( method, args );
		assert value != null : "Expected non-null value";
	}

	@Test(dataProvider = "invalid", expectedExceptions = UnsupportedOperationException.class)
	public void evaluate_UnsupportedOperationException( String method, Object [] args ) {
		new DateEvaluator().evaluate( method, args );
	}
}
