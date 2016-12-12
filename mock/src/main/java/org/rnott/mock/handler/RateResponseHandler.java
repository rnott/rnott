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

package org.rnott.mock.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.rnott.mock.Endpoint;
import org.rnott.mock.Response;


/**
 * A response handler that selects a response from configured entries based
 * on a percentage distribution.
 */
public class RateResponseHandler implements ResponseHandler {

	// register with the factory
	static {
		ResponseFactory.register( "rate", RateResponseHandler.class );
	}

	private final List<Response> responses = new ArrayList<Response>( 100 );
	private Random RANDOM = new Random();

	/* 
     * (non-Javadoc)
     * @see org.rnott.mock.handler.ResponseHandler#getResponse(org.rnott.mock.Endpoint)
     */
    @Override
    public Response getResponse( Endpoint endpoint ) {
	    // need to distribute responses the first time
    	if ( responses.size() == 0 ) {
    		for ( Response r : endpoint.getResponses() ) {
    			int percentile = r.get( "rate", Integer.class );
    			for ( int i = 0; i < percentile; i++ ) {
    				responses.add( r );
    			}
    		}
    		if ( responses.size() != 100 ) {
    			throw new IllegalStateException( "Invalid response configuration: percentiles do not total 100%" );
    		}
    	}

    	int index = RANDOM.nextInt( 100 );
	    return responses.get( index );
    }
}
