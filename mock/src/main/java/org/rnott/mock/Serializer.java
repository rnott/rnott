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
 * Serializes an HTTP request for later inspection.
 */
public class Serializer {

	/**
	 * Dictionary key for retrieving the HTTP method.
	 */
	public static final String KEY_METHOD = "method";
	
	/**
	 * Dictionary key for retrieving the URI.
	 */
	public static final String KEY_URI = "uri";

	/**
	 * Dictionary key for retrieving the HTTP headers dictionary.
	 */
	public static final String KEY_HEADERS = "headers";

	/**
	 * Dictionary key for retrieving the query parameters dictionary.
	 */
	public static final String KEY_PARAMETERS = "parameters";

	/**
	 * Dictionary key for retrieving the HTTP entity (body).
	 */
	public static final String KEY_BODY = "body";

	/**
	 * Serializes an HTTP request for later inspection.
	 * <p>
	 * @param request the request to serialize.
	 * @param body an optional request body.
	 * @return a dictionary of request elements.
	 * @throws IOException if the request cannot be serialized for any reason.
	 */
	public static Map<String, Object> serialize( HttpServletRequest request, byte [] body ) throws IOException {
		Map<String, Object> serialized = new HashMap<String, Object>();
		serialized.put( KEY_METHOD, request.getMethod() );
		serialized.put( KEY_URI, request.getRequestURI() );

		// query parameters
		Map<String, List<String>> params = new HashMap<String, List<String>>();
		Enumeration<String> names = request.getParameterNames();
		while ( names.hasMoreElements() ) {
			String key = names.nextElement();
			if ( ! params.containsKey( key ) ) {
				params.put( key, new ArrayList<String>() );
			}
			for ( String value : request.getParameterValues( key ) ) {
				params.get( key ).add( value );
			}
		}
		String query = request.getQueryString();
		if ( query != null ) {
			for ( String q : query.split( "&" ) ) {
				int pos = q.indexOf( '=' );
				String key = q.substring( 0, pos );
				String value = q.substring( pos + 1 );
				if ( ! params.containsKey( key ) ) {
					params.put( key, new ArrayList<String>() );
				}
				params.get( key ).add( value );
			}
		}
		serialized.put( KEY_PARAMETERS, params );

		// headers
		Map<String, Object> headers = new HashMap<String, Object>();
		Enumeration<?> hdrs = request.getHeaderNames();
		while ( hdrs.hasMoreElements() ) {
			String name = (String) hdrs.nextElement();
			headers.put( name, getHeaderValues( request, name ) );
		}
		serialized.put( KEY_HEADERS, headers );

		// body
		if ( body != null && body.length > 0 ) {
			BufferedReader in = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( body ) ) );
			ByteArrayOutputStream encoded = new ByteArrayOutputStream();
			String line = in.readLine();
			while ( line != null ) {
				byte [] b = line.getBytes();
				encoded.write( b );
				line = in.readLine();
			}
			serialized.put( KEY_BODY, new String( encoded.toByteArray() ) );
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
		}
		return v;
	}
}
