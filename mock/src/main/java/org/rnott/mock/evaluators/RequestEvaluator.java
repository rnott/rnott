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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import javax.servlet.http.HttpServletRequest;
import org.rnott.mock.MockContext;


/**
 * Support for <code>javax.servlet.http.HttpServletRequest</code> during expression language evaluation.
 * All public instance methods are exposed via reflection. The evaluator is activated using the psuedo-class
 * 'request'.
 * <p>
 * @see javax.servlet.http.HttpServletRequest
 */
public class RequestEvaluator implements Evaluator {

	/*
	 * (non-Javadoc)
	 * @see org.rnott.mock.Evaluator#key()
	 */
	@Override
	public String key() {
		return "request";
	}

	/*
	 * (non-Javadoc)
	 * @see org.rnott.mock.Evaluator#evaluate(java.lang.String, java.lang.Object[])
	 */
	@Override
	public String evaluate( String method, Object ... args ) {
		// substitute request properties
		HttpServletRequest request = MockContext.REQUESTS.get();
		// all interesting attributes appear to be JavaBean properties
		try {
			for ( PropertyDescriptor pd: Introspector.getBeanInfo( HttpServletRequest.class ).getPropertyDescriptors() ) {
				if ( pd.getName().equals( method ) ) {
					return String.valueOf( pd.getReadMethod().invoke( request ) );
				}
			}
		} catch ( Throwable ignore ) {
			ignore.printStackTrace();
		}

		return null;
	}
}
