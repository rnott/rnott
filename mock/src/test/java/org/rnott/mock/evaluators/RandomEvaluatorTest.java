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

import org.rnott.mock.evaluators.RandomEvaluator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * TODO: document RandomEvaluatorTest
 *
 */
public class RandomEvaluatorTest {

	@DataProvider(name = "methods")
	Object [][] methods() {
		return new Object [][] {
			{ "uuid" },
			{ "integer" },
			{ "long" }
		};
	}

	@DataProvider(name = "invalid")
	Object [][] invalid() {
		return new Object [][] {
			{ null },
			{ "foobar" },
			{ "" }
		};
	}

	@Test
	public void key() {
		Evaluator eval = new RandomEvaluator();
		assert "random".equals( eval.key() ) : "Unexpected key: " + eval.key() + ", expected 'random'";
	}

	@Test(dataProvider = "methods")
	public void evaluate( String method ) {
		Evaluator eval = new RandomEvaluator();
		Object value = eval.evaluate( method );
		assert value != null : "Expected non-null value";
	}

	@Test(dataProvider = "invalid")
	public void evaluate_Invalid( String method ) {
		Evaluator eval = new RandomEvaluator();
		Object value = eval.evaluate( method );
		assert value == null : "Expected null value";
	}
}
