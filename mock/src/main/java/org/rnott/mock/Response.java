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

public class Response implements Comparable<Response> {

	private final int status;
	private final long delay;
	int percentile;
	private final Map<String, String> headers;
	private String body;

	public Response( int defaultStatus, long defaultDelay, Map<String, ?> attributes ) {
		this.headers = new HashMap<String, String>();
		if ( attributes.containsKey( "status" ) ) {
			Object obj = attributes.get( "status" );
			status = (int) obj;
		} else {
			status = defaultStatus;
		}
		if ( attributes.containsKey( "delay" ) ) {
			Object obj = attributes.get( "delay" );
			delay = (int) obj;
		} else {
			delay = defaultDelay;
		}
		if ( attributes.containsKey( "percentile" ) ) {
			Object obj = attributes.get( "percentile" );
			percentile = (int) obj;
		}
		if ( attributes.containsKey( "headers" ) ) {
			@SuppressWarnings( "unchecked" )
			Map<String, String> hdrs = (Map<String, String>) attributes.get( "headers" );
			for ( String key : hdrs.keySet() ) {
				headers.put( key, hdrs.get( key ) );
			}
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
     * Retrieve the current value of the status property.
     * <p>
     * @return the current property value.
     */
    public int getStatus() {
    	return status;
    }

	
    /**
     * Retrieve the current value of the delay property.
     * <p>
     * @return the current property value.
     */
    public long getDelay() {
    	return delay;
    }

	
    /**
     * Retrieve the current value of the percentile property.
     * <p>
     * @return the current property value.
     */
    public int getPercentile() {
    	return percentile;
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
     * Retrieve the current value of the body property.
     * <p>
     * @return the current property value.
     */
    public String getBody() {
    	return body;
    }

	@Override
	public String toString() {
		return String.valueOf( status );
	}

	@Override
	public int compareTo( Response o ) {
		if ( this.percentile == o.percentile ) {
			return 0;
		} else if ( this.percentile > o.percentile ) {
			return 1;
		} else {
			return -1;
		}
	}
}