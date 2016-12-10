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

import org.testng.annotations.Test;


/**
 * Test functionality of type <code>NumericSetting</code>.
 * <p>
 * @see NumericSetting
 */
public class NumericSettingTest {

	@Test
	public void getKey() {
		NumericSetting setting = new NumericSetting( "foo" );
		assert "foo".equals( setting.getKey() ) : "Unexpected key:" + setting.getKey();
	}

	@Test
	public void getDefaultValue() {
		NumericSetting setting = new NumericSetting( "foo", 1 );
		assert setting.getDefaultValue() == 1 : "Unexpected value: " + setting.getValue() + ", expected 1";
	}

	@Test
	public void getValue() {
		NumericSetting setting = new NumericSetting( "foo", 1 );
		assert setting.getValue() == 1 : "Unexpected value: " + setting.getValue() + ", expected 1";
	}

	@Test
	public void getValue_Cast() {
		NumericSetting setting = new NumericSetting( "foo", 1 );
		assert setting.getValue( Integer.class ).intValue() == 1 : "Unexpected value: " + setting.getValue() + ", expected 1";
	}

	@Test
	public void parse() {
		NumericSetting setting = new NumericSetting( "foo", 2 );
		assert setting.parse( "1" ) == 1 : "Expected parsed value: 1";
		assert setting.parse( "" ) == 2 : "Expected default value: 2";
		assert setting.parse( (String) null ) == 2 : "Expected default value: 2";
	}

	@Test
	public void parse_Args() {
		NumericSetting setting = new NumericSetting( "foo" );
		assert setting.parse( new String [] {"--foo=1", "--bar=2"} ) == true : "Setting does not contain arg: foo";
		assert setting.getValue() == 1 : "Expected value: 1";
		assert setting.parse( new String [] {"--foz=1", "--bar=2"} ) == false : "Setting contains arg: foo";
	}
}
