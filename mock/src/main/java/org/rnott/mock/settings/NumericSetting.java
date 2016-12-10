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

package org.rnott.mock.settings;



/**
 * Models a setting with a <code>java.lang.Integer</code> value.
 */
public class NumericSetting extends Setting<Integer> {

	/**
	 * Construct a setting identified by the specified key
	 * and a default value of <code>0</code>.
	 * <p>
	 * @param key the setting identifier.
	 */
	public NumericSetting( String key) {
		this( key, 0 );
	}

	/**
	 * Construct a setting identified by the specified key
	 * and a default value.
	 * <p>
	 * @param key the setting identifier.
	 * @param defaultValue the default value to use if none is assigned.
	 */
	public NumericSetting( String key, Integer defaultValue ) {
		super( key, defaultValue );
	}

	/*
	 * (non-Javadoc)
	 * @see org.rnott.mock.Setting#parse(java.lang.String)
	 */
	@Override
	protected Integer parse( String s ) {
		return s != null && s.length() > 0 ? Integer.parseInt( s ) : getDefaultValue();
	}
}
