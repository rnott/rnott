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
 * Test functionality of type <code>BooleanSetting</code>.
 * <p>
 * @see BooleanSetting
 */
public class BooleanSettingTest {

	@Test
	public void getKey() {
		BooleanSetting setting = new BooleanSetting( "foo" );
		assert "foo".equals( setting.getKey() ) : "Unexpected key:" + setting.getKey();
	}

	@Test
	public void getDefaultValue() {
		BooleanSetting setting = new BooleanSetting( "foo", false );
		assert setting.getDefaultValue() == false : "Unexpected value: " + setting.getValue() + ", expected false";
		setting = new BooleanSetting( "foo", true );
		assert setting.getValue() == true : "Unexpected value: " + setting.getValue() + ", expected true";
	}

	@Test
	public void getValue() {
		BooleanSetting setting = new BooleanSetting( "foo", false );
		assert setting.getValue() == false : "Unexpected value: " + setting.getValue() + ", expected false";
		setting = new BooleanSetting( "foo", true );
		assert setting.getValue() == true : "Unexpected value: " + setting.getValue() + ", expected true";
	}

	@Test
	public void getValue_Cast() {
		BooleanSetting setting = new BooleanSetting( "foo", false );
		assert setting.getValue( Boolean.class ).booleanValue() == false : "Unexpected value: " + setting.getValue() + ", expected false";
		setting = new BooleanSetting( "foo", true );
		assert setting.getValue( Boolean.class ).booleanValue() == true : "Unexpected value: " + setting.getValue() + ", expected true";
	}

	@Test
	public void parse() {
		BooleanSetting setting = new BooleanSetting( "foo", false );
		assert setting.parse( "true" ) == true : "Expected parsed value: true";
		setting = new BooleanSetting( "foo" );
		assert setting.parse( "false" ) == false : "Expected parsed value: false";
		assert setting.parse( (String) null ) == false : "Expected default value: false";
	}

	@Test
	public void parse_Args() {
		BooleanSetting setting = new BooleanSetting( "foo" );
		assert setting.parse( new String [] {"--foo=true", "--bar=false"} ) == true : "Setting does not contain arg: foo";
		assert setting.getValue() == true : "Expected value: true";
		assert setting.parse( new String [] {"--foz=true", "--bar=false"} ) == false : "Setting contains arg: foo";
	}
}
