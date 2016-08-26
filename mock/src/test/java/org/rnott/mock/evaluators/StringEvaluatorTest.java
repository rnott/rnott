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
import org.rnott.mock.evaluators.StringEvaluator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * TODO: document StringEvaluatorTest
 *
 */
public class StringEvaluatorTest {

	@DataProvider(name = "methods")
	Object [][] methods() {
		return new Object [][] {
			{ "length", new Object [] { "1234" } },
			{ "substring", new Object [] { "1234", 0 } },
			{ "substring", new Object [] { "1234", 0, 1 } },
			{ "compareTo", new Object [] { "1234", "1234" } },
			{ "replace", new Object [] { "1234", '2', '1' } },
			{ "valueOf", new Object [] { "", 'a' } },
			{ "valueOf", new Object [] { "", (short)1 } },
			{ "valueOf", new Object [] { "", (int)1 } },
			{ "valueOf", new Object [] { "", (long)1 } },
			{ "valueOf", new Object [] { "", (double)1 } },
			{ "valueOf", new Object [] { "", (float)1 } },
			{ "valueOf", new Object [] { "", (byte)1 } },
			{ "valueOf", new Object [] { "", true } },
		};
	}

	@DataProvider(name = "invalid")
	Object [][] invalid() {
		return new Object [][] {
			{ "method", null },  // null args
			{ "method", new Object [] {} },  // no target instance
			{ "method", new Object [] { 123 } },  // target instance wrong type
			{ "method", new Object [] { "foo" } },  // method name not found
			{ "length", new Object [] { "foo", 123 } },  // signature mismatch
			{ "indexOf", new Object [] { "1234", '2' } },
			{ "substring", new Object [] { "1234", 0, 'a' } },
		};
	}

	@Test
	public void key() {
		Evaluator eval = new StringEvaluator();
		assert "string".equals( eval.key() ) : "Unexpected key: " + eval.key() + ", expected 'string'";
	}

	@Test(dataProvider = "methods")
	public void evaluate( String method, Object [] args ) {
		Object value = new StringEvaluator().evaluate( method, args );
		assert value != null : "Expected non-null value";
	}

	@Test(dataProvider = "invalid", expectedExceptions = IllegalStateException.class)
	public void evaluate_IllegalStateException( String method, Object [] args ) {
		new StringEvaluator().evaluate( method, args );
	}
}
