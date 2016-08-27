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


/**
 * An extension to the expression language (EL) grammar that allows native types
 * to participate in the EL grammar.
 */
public interface Evaluator {

	/**
	 * Determine the evaluator identifier.
	 * <p>
	 * @return the identifer value.
	 */
	String key();

	/**
	 * Invoke the evaluator. Any transformations supported by the evaluator
	 * are applied to the provided text.
	 * <p>
	 * @param text the text to transform.
	 * @param args optional evaluator-specific values. These are used to customize
	 * the transformation.
	 * @return the transformed text.
	 */
	Object evaluate( String text, Object ... args );
}
