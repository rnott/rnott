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
 * Models a setting with a <code>java.lang.Boolean</code> value.
 */
public class BooleanSetting extends Setting<Boolean> {

	/**
	 * Construct a setting identified by the specified key
	 * and a default value of <code>false</code>.
	 * <p>
	 * @param key the setting identifier.
	 */
	public BooleanSetting( String key ) {
		this( key, false );
	}

	/**
	 * Construct a setting identified by the specified key
	 * and a default value.
	 * <p>
	 * @param key the setting identifier.
	 * @param defaultValue the default value to use if none is assigned.
	 */
	public BooleanSetting( String key, Boolean defaultValue ) {
		super( key, defaultValue );
	}

	/*
	 * (non-Javadoc)
	 * @see org.rnott.mock.Setting#parse(java.lang.String)
	 */
	@Override
	protected Boolean parse( String s ) {
		if ( s != null ) {
			return Boolean.valueOf( s );
		}
		return getDefaultValue();
	}
}
