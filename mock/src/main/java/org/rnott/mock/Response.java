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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Configuration for a mock service response endpoint. The response is configured using
 * a JSON file or programmatically using a builder pattern.
 */
public class Response /*implements Comparable<Response>*/ {

	private int status;
	private final Map<String, String> headers;
	private final Map<String, Object> attributes;
	private String body;

	public Response() {
		this.attributes = new HashMap<String, Object>();
		this.headers = new HashMap<String, String>();
	}

	@SuppressWarnings( "unchecked" )
    public Response( int defaultStatus, Map<String, String> headers, Map<String, Object> attributes ) {
		this.attributes = new HashMap<String, Object>( attributes );
		this.headers = new HashMap<String, String>( headers );
		if ( attributes.containsKey( "status" ) ) {
			Object obj = attributes.get( "status" );
			status = (int) obj;
		} else {
			status = defaultStatus;
		}
		if ( attributes.containsKey( "headers" ) ) {
			this.headers.putAll( (Map<String, String>) attributes.get( "headers" ) );
		}
		if ( attributes.containsKey( "body" ) ) {
			// text or reference
			Object value = attributes.get( "body" );
			if ( value instanceof String ) {
				try {
					body = streamAsString( StreamFactory.getStream( (String) value ) );
				} catch ( IOException e ) {
					throw new RuntimeException( "Failed to parse endpoint response", e );
				}

			} else {
				// TODO: need response factory to handle more than just JSON
				StringWriter out = new StringWriter();
				try {
					new ObjectMapper().configure( SerializationFeature.INDENT_OUTPUT, true ).writeValue( out, value );
				} catch ( Throwable e ) {
					throw new RuntimeException( "Failed to parse endpoint response", e );
				}
				body = out.toString();
			}
		}
	}

	private String streamAsString( InputStream in ) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte [] b = new byte[8192];
		int bytes = in.read( b );
		try {
			while ( bytes >= 0 ) {
				out.write( b, 0, bytes );
				bytes = in.read( b );
			}
		} finally {
			try {
				in.close();
			} catch ( IOException ignore ) {}
		}

		return out.toString();
	}

	/**
	 * Retrieve a configuration attribute by name.
	 * <p>
	 * @param attr the name of the attribute.
	 * @return the attribute value or <code>null</code> if the attribute is undefined.
	 */
	public Object get( String attr ) {
		return get( attr, Object.class );
	}

	/**
	 * Retrieve a configuration attribute by name.
	 * <p>
	 * @param attr the name of the attribute.
	 * @param type the Java type the value should be returned as.
	 * @return the attribute value or <code>null</code> if the attribute is undefined.
	 */
	@SuppressWarnings( "unchecked" )
    public <T> T get( String attr, Class<T> type ) {
		return (T) attributes.get( attr );
	}

	/**
	 * Assign an attribute to the endpoint configuration. 
	 * <p>
	 * @param attr the name of the attribute.
	 * @param value the attribute value.
	 * @return the current response.
	 */
	public Response with( String attr, Object value ) {
		this.attributes.put( attr, value );
		return this;
	}

	/**
     * Retrieve the current value of the status property.
     * <p>
     * @return the current property value.
     */
    public int getStatus() {
    	return status;
    }

    /**
     * Configure the HTTP status to be returned for the response.
     * <p>
     * @param status the HTTP status in the range 100..599.
     * @return the current response.
     */
	public Response withStatus( int status ) {
		if ( status < 100 || status > 599 ) {
			throw new IllegalStateException( "Status must be in the range 100..599" );
		}
		this.status = status;
		return this;
	}

	/**
     * Retrieve the current value of the headers property.
     * <p>
     * @return the current property value.
     */
    public Map<String, String> getHeaders() {
    	return headers;
    }

    /**
     * Configures an HTTP response to be included with the response.
     * <p>
     * @param key the header key.
     * @param value the header value.
     * @return the current response.
     */
    public Response withHeader( String key, String value ) {
    	headers.put( key, value );
    	return this;
    }

    /**
     * Retrieve the current value of the body property.
     * <p>
     * @return the current property value.
     */
    public String getBody() {
    	return body;
    }

    /**
     * Register a body for use with the response.
     * <p>
     * @param body the body to register.
     * @return the current response.
     */
    public Response wtihBody( String body ) {
    	this.body = body;
    	return this;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
		return String.valueOf( status );
	}
}