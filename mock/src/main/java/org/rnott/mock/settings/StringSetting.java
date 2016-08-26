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

import org.rnott.mock.Setting;


/**
 * TODO: document StringSetting
 *
 */
public class StringSetting extends Setting<String> {

	public StringSetting( String key ) {
		super( key );
	}

	public StringSetting( String key, String defaultValue ) {
		super( key, defaultValue );
	}

	@Override
	protected String parse( String s ) {
		return s == null ? getDefaultValue() : s;
	}
}
