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
import java.util.List;
import java.util.Map;
import com.sun.jersey.api.uri.UriTemplate;

public class Endpoint {

	private final UriTemplate uriTemplate;
	
	private final String method;
	private final int status;
	private final long delay;
	private final List<Response> responses;
	private final List<Response> percentile;

	@SuppressWarnings( "unchecked" )
	public Endpoint( Map<String, ?> attributes ) {
		if ( ! attributes.containsKey( "uri" ) ) {
			throw new IllegalStateException( "Endpoint definition missing required 'uri' attribute: " + attributes );
		}
		if ( ! attributes.containsKey( "method" ) ) {
			throw new IllegalStateException( "Endpoint definition missing required 'method' attribute: " + attributes );
		}
		if ( ! attributes.containsKey( "status" ) ) {
			throw new IllegalStateException( "Endpoint definition missing required 'status' attribute:" + attributes );
		}
		if ( ! attributes.containsKey( "response" ) ) {
			throw new IllegalStateException( "Endpoint definition missing required 'response' attribute:" + attributes );
		}

		Object obj = attributes.get( "uri" );
		uriTemplate = new UriTemplate( (String) obj );
		obj = attributes.get( "method" );
		method = (String) obj;
		obj = attributes.get( "status" );
		status = (int) obj;
		if ( attributes.containsKey( "delay" ) ) {
			obj = attributes.get( "delay" );
			delay = (int) obj;
		} else {
			delay = 0;
		}

		responses = new ArrayList<Response>();
		List<Map<String, ?>> entries = (List<Map<String, ?>>) attributes.get( "response" );
		for ( Map<String, ?> r : entries ) {
			responses.add( new Response( status, delay, r ) );
		}

		// normalize response percentiles
		List<Response> unassigned = new ArrayList<Response>();
		int sum = 0;
		for ( Response r : responses ) {
			sum += r.percentile;
			if ( r.percentile <= 0 ) {
				unassigned.add( r );
			}
		}
		if ( sum > 100 ) {
			throw new IllegalStateException( "Sum of enpoint response percentiles exceeds 100:" + this );
		}
		if ( sum < 100 && unassigned.size() == 0 ) {
			throw new IllegalStateException( "Sum of enpoint response percentiles less than 100:" + this );
		}
		if ( sum == 100 && unassigned.size() > 0 ) {
			throw new IllegalStateException( "Enpoint responses cannot be assigned a percentile: " + this + " " + unassigned );
		}

		if ( unassigned.size() > 0 ) {
			// spread remaining percentile evenly over unassigned endpoints
			int value = (100 - sum) / unassigned.size();
			if ( value == 0 ) {
				throw new IllegalStateException( "Enpoint responses cannot be assigned a percentile: " + this + " " + unassigned );
			}
			for ( Response r : unassigned ) {
				r.percentile = value;
			}
			int updated = sum + (unassigned.size() * value);
			for ( Response r : unassigned ) {
				if ( updated == 100 ) {
					break;
				}
				r.percentile -= 1;
				updated -= 1;
			}
		}

		percentile = new ArrayList<Response>( 100 );
		for ( Response r : responses ) {
			for ( int i = 0; i < r.percentile; i++ ) {
				percentile.add( r );
			}
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
     * Retrieve the current value of the responses property.
     * <p>
     * @return the current property value.
     */
    public List<Response> getResponses() {
    	return responses;
    }

	
    /**
     * Retrieve the current value of the percentile property.
     * <p>
     * @return the current property value.
     */
    public List<Response> getPercentile() {
    	return percentile;
    }

	@Override
	public String toString() {
		return method + " " + uriTemplate.getTemplate();
	}
}