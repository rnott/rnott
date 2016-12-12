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

import java.util.HashMap;
import java.util.Map;
import org.rnott.mock.Endpoint;
import org.rnott.mock.Response;


/**
 * TODO: document ResponseHandler
 *
 */
public class ResponseFactory {

	static Map<String, Class<? extends ResponseHandler>> implementations =
		new HashMap<String, Class<? extends ResponseHandler>>();
	static Map<Endpoint, ResponseHandler> handlers = new HashMap<Endpoint, ResponseHandler>();

	// cause handler classes to load so that they register
	// TODO: automatically discover response handler implementations
	private static void bootstrap() {
	    Class<?> [] classes = {
			SequentialResponseHandler.class,
			RandomResponseHandler.class,
			RateResponseHandler.class,
			ParameterMatchingResponseHandler.class
		};
		for ( Class<?> c : classes ) {
			try {
				Class.forName( c.getName() );
			} catch ( Throwable ignore ) {}
		}
	}

	/**
	 * Register a handler implementation.
	 * <p>
	 * @param key the key to identify the handler (case-insensitive). The endpoint handler attribute
	 * is used to select a handler and must match the key for that handler to be selected.
	 * @param impl the handler class which will be instantiated at most once per endpoint.
	 */
	static void register( String key, Class<? extends ResponseHandler> impl ) {
		implementations.put( key == null ? null : key.toLowerCase(), impl );
	}

	/**
	 * Select one of the configured responses for responding to a request.
	 * <p>
	 * @param endpoint the requested endpoint.
	 * @return the request to respond with or <code>null</code> if no response
	 * is appropriate.
	 */
	public static Response getResponse( Endpoint endpoint ) {
		if ( implementations.size() == 0 ) {
			bootstrap();
		}

		Response response = null;

		// find appropriate handler
		ResponseHandler handler = handlers.get( endpoint );
		if ( handler == null ) {
			String key = endpoint.getHandlerType();
			Class<? extends ResponseHandler> impl = implementations.get( key == null ? null : key.toLowerCase() );
			if ( impl != null ) {
				try {
					handler = impl.newInstance();
					handlers.put( endpoint, handler );
				} catch ( Throwable t ) {}					
			}
		}
		if ( handler != null ) {
			response = handler.getResponse( endpoint );
		}

		// no response assigned yet so simply use the first one
		if ( response == null && endpoint.getResponses().size() > 0 ) {
			response = endpoint.getResponses().get( 0 );
		}

		return response;
	}
}
