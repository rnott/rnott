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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sun.jersey.api.uri.UriTemplate;

public class Endpoint {

	private final UriTemplate uriTemplate;
	
	private final String method;
	private final String handler; 
	private final int status;
	private final long delay;
	private final List<Response> responses;

	@SuppressWarnings( "unchecked" )
	public Endpoint( Map<String, ?> attributes ) {
		if ( ! attributes.containsKey( "uri" ) ) {
			throw new IllegalStateException( "Endpoint definition missing required 'uri' attribute: " + attributes );
		}
		if ( ! attributes.containsKey( "method" ) ) {
			throw new IllegalStateException( "Endpoint definition missing required 'method' attribute: " + attributes );
		}
		if ( ! attributes.containsKey( "response" ) ) {
			throw new IllegalStateException( "Endpoint definition missing required 'response' attribute:" + attributes );
		}

		Object obj = attributes.get( "uri" );
		uriTemplate = new UriTemplate( (String) obj );
		obj = attributes.get( "method" );
		method = (String) obj;
		if ( attributes.containsKey( "handler" ) ) {
			handler = (String) attributes.get( "handler" );
		} else {
			handler = null;
		}
		if ( attributes.containsKey( "status" ) ) {
			obj = attributes.get( "status" );
			status = (int) obj;
		} else {
			status = 200;
		}
		if ( attributes.containsKey( "delay" ) ) {
			obj = attributes.get( "delay" );
			delay = (int) obj;
		} else {
			delay = 0;
		}

		responses = new ArrayList<Response>();
		List<Map<String, Object>> entries = (List<Map<String, Object>>) attributes.get( "response" );
		for ( Map<String, Object> r : entries ) {
			Map<String, String> headers = new HashMap<String, String>();
			if ( attributes.containsKey( "headers" ) ) {
				headers.putAll( (Map<String, String>) attributes.get( "headers" ) );
			}
			responses.add( new Response( status, headers, r ) );
		}
	}

    /**
     * Retrieve the current value of the URI template property.
     * <p>
     * @return the current property value.
     */
    public UriTemplate getUriTemplate() {
    	return uriTemplate;
    }

	
    /**
     * Retrieve the current value of the method property.
     * <p>
     * @return the current property value.
     */
    public String getMethod() {
    	return method;
    }

    /**
     * Retrieve the current value of the handler property.
     * <p>
     * @return the current property value or <code>null</code> if no
     * value has been assigned.
     */
    public String getHandlerType() {
    	return handler;
    }
	
    /**
     * Retrieve the current value of the status property.
     * <p>
     * @return the current property value. The default value is 200.
     */
    public int getStatus() {
    	return status;
    }

	
    /**
     * Retrieve the current value of the delay property.
     * <p>
     * @return the current property value. The default value is 0.
     */
    public long getDelay() {
    	return delay;
    }

	
    /**
     * Retrieve the current value of the responses property.
     * <p>
     * @return the current property value.
     */
    public List<Response> getResponses() {
    	return responses;
    }


	@Override
	public String toString() {
		return method + " " + uriTemplate.getTemplate();
	}
}