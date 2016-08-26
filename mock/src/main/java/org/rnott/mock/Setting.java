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
 * Configuration setting model.
 * <p>
 * @param <T> the setting value type. 
 */
public abstract class Setting <T> {
	private final String key;
	private final T defaultValue; 
	private T value;

	/**
	 * Create a setting with the specified key and no initial value.
	 * <p>
	 * @param key the key value which identifies the setting.
	 */
	public Setting( String key ) {
		this( key, null );
	}

	/**
	 * Create a setting with the specified key and default value.
	 * <p>
	 * @param key the key value which identifies the setting.
	 * @param defaultValue the default value of the setting. The
	 * default value is used when no other value has been assigned.
	 */
	public Setting( String key, T defaultValue ) {
		this.key = key;
		this.defaultValue = defaultValue;
	}

	/**
	 * Determine if a setting is present in a collection of strings.
	 * The setting key is prefixed with '--' (double dash) in command
	 * line option style. 
	 * <p>
	 * This method is most useful when called with command line
	 * arguments as the collection of strings.
	 * <p>
	 * @param args collection of strings to search.
	 * @return <code>true</code'> if the collection of strings
	 * contains the setting key, <code>false</code> otherwise.
	 */
	public boolean parse( String [] args ) {
		for ( String s : args ) {
			if ( s.startsWith( "--" + key + "=" ) ) {
				int pos = s.indexOf( '=' );
				if ( pos > 0 ) {
					value = parse( s.substring( pos + 1 ) );
				} else {
					value = parse( "" );
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine the default value of the setting.
	 * <p>
	 * @return the default value or <code>null</code> if none
	 * is assigned.
	 */
	protected T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Marshal the setting value from a string to the
	 * actual type.
	 * <p>
	 * @param s the string to marshal.
	 * @return the marshaled value.
	 */
	protected abstract T parse( String s );

	/**
	 * Determine the setting key. The key uniquely identifies
	 * the setting within a collection of settings.
	 * <p>
	 * @return the setting key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Determine the value of the setting.
	 * <p>
	 * @return the assigned value of the setting, the default value if
	 * no value is assigned, or <code>null</code> if no default value.
	 */
	public T getValue() {
		return value == null ? defaultValue : value;
	}

	/**
	 * Cast the setting value to the requested type.
	 * <p>
	 * @param type the requested type for the value.
	 * @return the value cast to the specified type.
	 * @throws ClassCastException if the value cannot be cast to the
	 * requested type.
	 */
	public T getValue( Class<T> type ) {
		return (T) value;
	}
}
