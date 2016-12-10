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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * Test the functionality of the <code>org.rnott.mock.StreamFactoryTest</code> component.
 * <p>
 * @see org.rnott.mock.StreamFactory
 */
public class StreamFactoryTest {

	@DataProvider(name = "uri")
	private Object [][] getURIs() throws MalformedURLException, IOException {
		File f = File.createTempFile( "test", ".txt" );
		f.deleteOnExit();
		byte [] b = new byte[8192];
		FileOutputStream out = new FileOutputStream( f );
		out.write( b );
		out.close();

		return new Object [][] {
			// text stream 
			{ "this is sample text", digest( "this is sample text" ) },
			{ "/foo/bar", digest( "/foo/bar" ) },
			// classpath resource
			{ "classpath:mock-config.json", digest( StreamFactoryTest.class.getResourceAsStream( "/mock-config.json" ) ) },
			{ "classpath:", digest( "classpath:" ) },
			// HTTP resource
			{ "http://web.archive.org/web/20160823001242/http://www.google.com/", digest( new URL( "http://web.archive.org/web/20160823001242/http://www.google.com/" ).openStream() ) },
			// file scheme
			{ "file:" + f.getAbsolutePath(), digest( new FileInputStream( f ) ) },
			// file without scheme
			{ f.getAbsolutePath(), digest( new FileInputStream( f ) ) },
		};
	}

	@DataProvider(name = "invalid")
	private Object [][] invalid() {
		return new Object [][] {
			{ "classpath:missing.properties" },
		};
	}

	private static byte [] digest( String value ) {
		return digest( new ByteArrayInputStream( value.getBytes() ) );
	}

	private static byte [] digest( InputStream sink ) {
		try {
			DigestInputStream in = new DigestInputStream( sink, MessageDigest.getInstance( "SHA-1" ) );
			byte [] b = new byte[8192];
			int bytes = in.read( b );
			while ( bytes >= 0 ) {
				bytes = in.read( b );
			}
			return in.getMessageDigest().digest();
		} catch ( Throwable t ) {
			t.printStackTrace();
			return new byte [0];
		}
	}

	/**
	 * Assert streaming functionality.
	 * <p>
	 * @param uri the URI defining the resource to be streamed.
	 * @param expected the digest which is expected to match the digest of the streamed content.
	 * @throws IOException if the stream cannot be consumed for any reason.
	 */
	@Test(dataProvider = "uri")
	public void getStream( String uri, byte [] expected ) throws IOException {
		InputStream stream = StreamFactory.getStream( uri );
		assert stream != null : "No input stream created";
		assert MessageDigest.isEqual( digest( stream ), expected ) : "Digest does not match expected value";
	}

	@Test(dataProvider = "invalid", expectedExceptions = {IllegalStateException.class, IOException.class})
	public void getStream_IllegalStateException( String uri ) throws IOException {
		StreamFactory.getStream( uri );
	}
}
