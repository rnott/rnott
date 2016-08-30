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


import java.text.SimpleDateFormat;
import java.util.Date;
import org.rnott.mock.Evaluator;

/**
 * Support for <code>java.util.Date</code> during expression language evaluation.
 * No class methods are exposed, however the following psuedo-methods are available:
 * <ul>
 * <li>now(): current time as UNIX-style value.
 * <li>now(format): current time formatted using the provided <code>DataFormat</code> conformant format specification.
 * </ul>
 * <p>
 * @see java.util.Date
 * @see java.text.DateFormat
 */
public class DateEvaluator implements Evaluator {

	/*
	 * (non-Javadoc)
	 * @see org.rnott.mock.Evaluator#key()
	 */
	@Override
	public String key() {
		return "date";
	}

	/*
	 * (non-Javadoc)
	 * @see org.rnott.mock.Evaluator#evaluate(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object evaluate( String method, Object ... args ) {
		// current date/time
		if ( "now".equals( method ) ) {
			if ( args == null || args.length == 0 ) {
				return System.currentTimeMillis();
			} else {
				return new SimpleDateFormat( (String) args[0] ).format( new Date() );
			}
		}
		throw new UnsupportedOperationException( method );
	}
}
