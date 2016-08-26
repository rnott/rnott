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


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * TODO: document Serializer
 *
 */
public class Serializer {

	public static Map<String, Object> serialize( HttpServletRequest request, byte [] body ) throws IOException {
		Map<String, Object> serialized = new HashMap<String, Object>();
		serialized.put( "method", request.getMethod() );
		serialized.put( "uri", request.getRequestURI() );

		// query parameters
		// TODO: handle query parameter style name=v1&name=v2...&name=vN
		String query = request.getQueryString();
		if ( query != null ) {
			Map<String, Object> params = new HashMap<String, Object>();
			for ( String q : query.split( "&" ) ) {
				int pos = q.indexOf( '=' );
				params.put( q.substring( 0, pos ), q.substring( pos + 1 ) );
			}
			serialized.put( "query", params );
		}

		// headers
		Map<String, Object> headers = new HashMap<String, Object>();
		Enumeration<?> hdrs = request.getHeaderNames();
		while ( hdrs.hasMoreElements() ) {
			String name = (String) hdrs.nextElement();
			headers.put( name, getHeaderValues( request, name ) );
		}
		serialized.put( "headers", headers );

		// body
		if ( body != null && body.length > 0 ) {
			BufferedReader in = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( body ) ) );
			ByteArrayOutputStream encoded = new ByteArrayOutputStream();
			//Base64OutputStream out = new Base64OutputStream( encoded );
			//out.write( body );
			//out.close();
			//encoded.write( body );
			String line = in.readLine();
			while ( line != null ) {
				byte [] b = line.getBytes();
				encoded.write( b );
				line = in.readLine();
			}
			serialized.put( "body", new String( encoded.toByteArray() ) );
		}

		return serialized;
	}

	private static Object getHeaderValues( HttpServletRequest request, String name ) {
		Enumeration<?> values = request.getHeaders( name );
		List<Object> v = new ArrayList<Object>( 1 );
		while ( values.hasMoreElements() ) {
			v.add( values.nextElement() );
		}
		if ( v.size() == 0 ) {
			throw new IllegalStateException( "No value for header: " + name );
		} else if ( v.size() == 1 ) {
			return v.get( 0 );
		} else {
			return v.toArray();
		}
	}
}
