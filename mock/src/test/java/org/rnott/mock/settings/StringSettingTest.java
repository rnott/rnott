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
 * Test functionality of type <code>StringSetting</code>.
 * <p>
 * @see StringSetting
 */
public class StringSettingTest {

	private static final String YES = "yes";
	private static final String NO = "no";

	@Test
	public void getKey() {
		StringSetting setting = new StringSetting( "foo" );
		assert "foo".equals( setting.getKey() ) : "Unexpected key:" + setting.getKey();
	}

	@Test
	public void getDefaultValue() {
		StringSetting setting = new StringSetting( "foo", YES );
		assert "yes".equals( setting.getDefaultValue() ) : "Unexpected value: " + setting.getValue() + ", expected 'yes'";
	}

	@Test
	public void getValue() {
		StringSetting setting = new StringSetting( "foo", YES );
		assert "yes".equals( setting.getValue() ) : "Unexpected value: " + setting.getValue() + ", expected 'yes'";
	}

	@Test
	public void getValue_Cast() {
		StringSetting setting = new StringSetting( "foo", YES );
		assert "yes".equals( setting.getValue( String.class ) ) == true : "Unexpected value: " + setting.getValue() + ", expected 'yes'";
	}

	@Test
	public void parse() {
		StringSetting setting = new StringSetting( "foo", NO );
		assert "yes".equals( setting.parse( "yes" ) ) : "Unexpected value: " + setting.getValue() + ", expected 'yes'";
		assert "".equals( setting.parse( "" ) ) : "Unexpected value: " + setting.getValue() + ", expected ''";
		assert "no".equals( setting.parse( (String) null ) ) : "Unexpected value: " + setting.getValue() + ", expected 'no'";
	}

	@Test
	public void parse_Args() {
		StringSetting setting = new StringSetting( "foo" );
		assert setting.parse( new String [] {"--foo=foo", "--bar=bar"} ) == true : "Setting does not contain arg: foo";
		assert "foo".equals( setting.getValue() ) : "Expected value 'foo': '" + setting.getValue() + "'";
		assert setting.parse( new String [] {"--foz=foo", "--bar=bar"} ) == false : "Setting contains arg: foo";
	}
}
