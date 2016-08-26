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


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provides utility methods for creating input streams.
 */
public class StreamFactory {

	/**
	 * Create a stream for reading a URI.
	 * This works the same as <code>URL.openStream()</code>, using the scheme
	 * specified in the URI. If scheme is omitted, this implementation first
	 * tries to open the URI as a file if the it represents an existing file.
	 * If no such file exists, or when the uri is not valid, the text will be
	 * the source of the data returned by reading the stream. 
	 * <p>
	 * @param uri the URI of the resource.
	 * @return a readable stream of bytes.
	 * @throws IOException if the resource could not be found on the classpath,
	 * @see java.net.URL#openStream()
	 */
	public static InputStream getStream( String uri ) throws IOException {
		URI spec;
		try {
			spec = new URI( uri );
		} catch ( URISyntaxException e ) {
			// treat this as data
			return new ByteArrayInputStream( uri.getBytes() );
		}

		/*
		 *  based on scheme
		 */

		// classpath resource
		if ( "classpath".equals( spec.getScheme() ) ) {
			int pos = uri.indexOf( ':' );
			return getResourceStream( uri.substring( pos + 1 ) );
		}

		// file or data
		else if ( spec.getScheme() == null ) {
			// default to filesystem
			File f = new File( spec.getPath() );
			if ( f.exists() ) {
				return new FileInputStream( f );
			}
			else {
				// treat this as data
				return new ByteArrayInputStream( uri.getBytes() );
			}
		}

		// URL stream
		else {
			return spec.toURL().openStream();
		}
	}

	/**
	 * Create a stream for reading a resource on the classpath.
	 * This works the same as <code>ClassLoader.getResourceAsStream()</code>
	 * except that this method will throw an exception if the resource cannot
	 * be found.
	 * <p>
	 * @param resource the name of the resource.
	 * @return the specified resource as a readable stream of bytes.
	 * @throws IOException if the resource could not be found on the classpath,
	 */
	private static InputStream getResourceStream( String resource ) throws IOException {
		if ( resource == null ) {
			throw new IllegalArgumentException( "Resource must be specified" );
		}
		InputStream stream = StreamFactory.class.getClassLoader().getResourceAsStream( resource );
		if ( stream == null ) {
			throw new IOException( "No such resource: " + resource );
		}
		return stream;
	}

}
