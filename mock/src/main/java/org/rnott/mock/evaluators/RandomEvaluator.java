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
 * Support for rqndom values during expression language evaluation.
 * The following psuedo-methods are available:
 * <ul>
 * <li>uuid(): generate a new random v4 UUID.
 * <li>integer(): generate the next random integer value.
 * <li>long(): generate the next random long value.
 * </ul>
 * <p>
 * @see java.security.SecureRandom
 * @todo use reflection to support all SecureRandom methods.
 */
public class RandomEvaluator implements Evaluator {

	// is this thread-safe?
	static final SecureRandom RANDOM = new SecureRandom();

	/*
	 * (non-Javadoc)
	 * @see org.rnott.mock.Evaluator#key()
	 */
	@Override
	public String key() {
		return "random";
	}

	/*
	 * (non-Javadoc)
	 * @see org.rnott.mock.Evaluator#evaluate(java.lang.String, java.lang.Object[])
	 */
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
