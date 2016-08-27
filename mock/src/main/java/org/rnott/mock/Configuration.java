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


import java.io.File;
import java.util.UUID;

/**
 * Simple server configuration model.
 */
public class Configuration {

	private static File path;
	static {
		path = new File( new File( System.getProperty( "java.io.tmpdir" ) ), UUID.randomUUID().toString() );
		path.mkdirs();
	}

	/**
	 * Get the server working directory.
	 * <p>
	 * @return the working directory.
	 */
	public static File getWorkDirectory() {
		return path;
	}

	/**
	 * Determine the server working directory.
	 * <p>
	 * @return the working directory to use.
	 */
	public static String getWorkPath() {
		return path.getAbsolutePath();
	}
}
