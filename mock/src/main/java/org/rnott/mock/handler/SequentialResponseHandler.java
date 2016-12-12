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

import org.rnott.mock.Endpoint;
import org.rnott.mock.Response;


/**
 * A response handler that iterates over the configured entries. When the
 * sequence has completed, it is restarted from the beginning.
 */
public class SequentialResponseHandler implements ResponseHandler {

	// register with the factory
	static {
		ResponseFactory.register( "sequential", SequentialResponseHandler.class );
	}

	private int index = 0;

	/* 
     * (non-Javadoc)
     * @see org.rnott.mock.handler.ResponseHandler#getResponse(org.rnott.mock.Endpoint)
     */
    @Override
    public Response getResponse( Endpoint endpoint ) {
	    Response r = endpoint.getResponses().get( index );
	    index = (index + 1) % endpoint.getResponses().size();
	    return r;
    }
}
