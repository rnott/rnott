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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.jersey.api.uri.UriTemplate;

/**
 * An HTTP servlet implementation that can be configured to serve canned
 * service responses.
 */
public class MockServlet extends HttpServlet {

	private static final long serialVersionUID = 8169961897058098636L;

	/**
	 * Models configuration for a service endpoint response option.
	 */
	private static class Response implements Comparable<Response> {
		private final int status;
		private final long delay;
		private int percentile;
		private final Map<String, String> headers;
		private String body;

		/**
		 * Create the configuration for a service endpoint response option.
		 * <p>
		 * @param defaultStatus the HTTP status to use if not configured.
		 * @param defaultDelay the wait to use if not configured.
		 * @param attributes collection of configured settings.
		 */
		private Response( int defaultStatus, long defaultDelay, Map<String, ?> attributes ) {
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

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.valueOf( status );
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
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

	/**
	 * Models configuration for a service endpoint.
	 */
	private static class Endpoint {
		private final UriTemplate template;
		private final String method;
		private final int status;
		private final long delay;
		private final List<Response> responses;
		private final List<Response> percentile;

		/**
		 * Create the configuration for a service endpoint.
		 * <p>
		 * @param attributes collection of configured settings.
		 */
		@SuppressWarnings( "unchecked" )
		private Endpoint( Map<String, ?> attributes ) {
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
			template = new UriTemplate( (String) obj );
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

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return method + " " + template.getTemplate();
		}
	}

	private boolean debug = false;
	private boolean trace = false;
	private List<Endpoint> endpoints;

	@Override
	public void init( ServletConfig config ) throws ServletException {
		super.init( config );

		try {
			debug = Boolean.parseBoolean( config.getInitParameter( "debug" ) );
		} catch ( Throwable ignore ) {}

		try {
			trace = Boolean.parseBoolean( config.getInitParameter( "trace" ) );
		} catch ( Throwable ignore ) {}

		String resource = config.getInitParameter( "config" );
		if ( resource == null ) {
			throw new ServletException( "No mock configuration provided" );
		}
		try {
			initialize( StreamFactory.getStream(  resource ) );
		} catch ( Throwable t ) {
			throw new ServletException( "Failed to initialize", t );
		}
	}


	@Override
	public void destroy() {
		if ( endpoints != null ) {
			endpoints.clear();
			endpoints = null;
		}

		super.destroy();
	}

	private static final String NEWLINE = System.getProperty( "line.separator", "\n" );

	@Override
	public void service( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
		MockContext context = MockContext.get();
		context.setRequest( request );

		if ( trace ) {
			StringBuilder sb = new StringBuilder()
				.append( "Source-IP: ")
				.append( request.getRemoteAddr() )
				.append( ":" )
				.append( request.getRemotePort() )
				.append( NEWLINE )
				.append( request.getMethod() )
				.append( " " )
				.append( request.getProtocol() )
				.append( " " )
				.append( request.getRequestURI() );
			String q = request.getQueryString();
			if ( q != null && q.length() > 0 ) {
				sb.append( "?" ).append( q );
			}
			sb.append( NEWLINE );
			Enumeration<String> headers = request.getHeaderNames();
			while ( headers.hasMoreElements() ) {
				String name = headers.nextElement();
				sb.append( name )
					.append( ": " )
					.append( request.getHeader( name ) )
					.append( NEWLINE );
			}
			log( sb.toString() );
		}

		// match path/method
		if ( debug ) {
			log( "Matching request: " + request.getMethod() + " " + request.getRequestURI() );
		}
		for ( Endpoint e : endpoints ) {
			if ( debug ) {
				log( "Testing endpoint: " + e.method + " " + e.template.getTemplate() );
			}
			if ( request.getMethod().equalsIgnoreCase( e.method ) ) {
				if ( debug ) {
					log( "Matched method: " + e.method );
				}
				Map<String, String> params = context.getParameters();
				params.clear();
				if ( e.template.match( request.getRequestURI(), params ) ) {
					// add any query parameters
					Enumeration<?> names = request.getParameterNames();
					while ( names.hasMoreElements() ) {
						String key = (String) names.nextElement();
						params.put( key, request.getParameter( key ) );
					}
					if ( debug ) {
						log( "Matched URI: " + e.template.getTemplate() );
					}
					Response r = getResponse( e );
					if ( r == null ) {
						throw new IllegalStateException( "No response available for endpoint: "
							+ e.method + " " + e.template.getTemplate() );
					}

					if ( debug ) {
						log( "Response status code: " + r.status );
					}
					response.setStatus( r.status );
					for ( String key : r.headers.keySet() ) {
						response.addHeader( key, context.evaluate( r.headers.get( key ) ) );
					}
					String body = context.evaluate( r.body );
					if ( body != null ) {
						response.getOutputStream().write( body.getBytes() );
					}

					if ( r.delay > 0 ) {
						if ( debug ) {
							log( "Delaying response: " + r.delay + "ms" );
						}
						try {
							Thread.sleep( r.delay );
						} catch ( InterruptedException ignore ) {}
					}

					// commit
					return;
				}
			}
		}

		// no match
		response.setStatus( HttpServletResponse.SC_NOT_FOUND );
		if ( debug ) {
			log( "No match for: " + request.getMethod() + " " + request.getRequestURI() );
		}
	}

	
	private Response getResponse( Endpoint e ) {
		int index = (int)(Math.random() * 100);
		return e.percentile.get( index );
	}

	private void initialize( InputStream config ) throws IOException {
		endpoints = new ArrayList<Endpoint>();
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings( "unchecked" )
		Map<String, ?> [] entries = mapper.readValue( config, Map [].class );
		for ( Map<String, ?> entry : entries ) {
			endpoints.add( new Endpoint( entry ) );
		}
	}
}
