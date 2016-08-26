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

import java.security.SecureRandom;
import java.util.UUID;
import org.rnott.mock.Evaluator;


/**
 * TODO: document RandomEvaluator
 *
 */
public class RandomEvaluator implements Evaluator {

	// is this thread-safe?
	static final SecureRandom RANDOM = new SecureRandom();

	@Override
	public String key() {
		return "random";
	}

	@Override
	public String evaluate( String method, Object ... args ) {
		if ( "uuid".equalsIgnoreCase( method ) ) {
			return UUID.randomUUID().toString();
		} else if ( "integer".equalsIgnoreCase( method ) ) {
			return String.valueOf( RANDOM.nextInt() );
		} else if ( "long".equalsIgnoreCase( method ) ) {
			return String.valueOf( RANDOM.nextLong() );
		}
		return null;
	}
}
