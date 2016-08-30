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


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * TODO: document MockServlet
 *
 */
public class MockServlet extends HttpServlet {

	private static final long serialVersionUID = 8169961897058098636L;

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
				log( "Testing endpoint: " + e.getMethod() + " " + e.getUriTemplate().getTemplate() );
			}
			if ( request.getMethod().equalsIgnoreCase( e.getMethod() ) ) {
				if ( debug ) {
					log( "Matched method: " + e.getMethod() );
				}
				Map<String, String> params = context.getParameters();
				params.clear();
				if ( e.getUriTemplate().match( request.getRequestURI(), params ) ) {
					// add any query parameters
					Enumeration<?> names = request.getParameterNames();
					while ( names.hasMoreElements() ) {
						String key = (String) names.nextElement();
						params.put( key, request.getParameter( key ) );
					}
					if ( debug ) {
						log( "Matched URI: " + e.getUriTemplate().getTemplate() );
					}
					Response r = getResponse( e );
					if ( r == null ) {
						throw new IllegalStateException( "No response available for endpoint: "
							+ e.getMethod() + " " + e.getUriTemplate().getTemplate() );
					}

					if ( debug ) {
						log( "Response status code: " + r.getStatus() );
					}
					response.setStatus( r.getStatus() );
					for ( String key : r.getHeaders().keySet() ) {
						response.addHeader( key, context.evaluate( r.getHeaders().get( key ) ) );
					}
					String body = context.evaluate( r.getBody() );
					if ( body != null ) {
						response.getOutputStream().write( body.getBytes() );
					}

					if ( r.getDelay() > 0 ) {
						if ( debug ) {
							log( "Delaying response: " + r.getDelay() + "ms" );
						}
						try {
							Thread.sleep( r.getDelay() );
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
		return e.getPercentile().get( index );
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

	public static final void main( String [] args ) throws Throwable {
		MockServlet servlet = new MockServlet();
		servlet.initialize( MockServlet.class.getResourceAsStream( "/service.json" ) );
		for ( Endpoint e : servlet.endpoints ) {
			System.out.println( e );
		}
	}
}
