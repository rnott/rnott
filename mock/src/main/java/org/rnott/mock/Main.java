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
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
/*
 * requires jetty 9.3+
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
 */
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.rnott.mock.settings.BooleanSetting;
import org.rnott.mock.settings.NumericSetting;
import org.rnott.mock.settings.StringSetting;

/*
 * Test profiles:
 * Integration: need to record request/response pairs for verification, throughput not so important
 * Stress: need to scale and customize throughput; recording not so important
 */
/**
 * 
 * TODO: document Main
 *
 */
public class Main {

	public static class DefaultServlet extends HttpServlet {

		private static final long serialVersionUID = -4270555803465033833L;

		@Override
		public void service( HttpServletRequest request, HttpServletResponse response )	throws ServletException, IOException {
			log( request.getMethod() + " " + request.getRequestURI() );

			BufferedReader in = request.getReader();
	        String s = in.readLine();
	        while ( s != null ) {
	        	s = in.readLine();
	        }

			response.setCharacterEncoding( "text/html; charset=utf-8" );
			response.setStatus( HttpServletResponse.SC_NO_CONTENT );
		}
	}

	private static class Configuration implements Iterable<Setting<?>> {

		protected final List<Setting<?>> settings;

		public Configuration() {
			settings = new ArrayList<Setting<?>>();
		}

		public Configuration add( Setting<?> setting ) {
			settings.add( setting );
			return this;
		}

		public Configuration parse( String [] args ) {
			for ( Setting<?> s : settings ) {
				s.parse( args );
			}
			return this;
		}

		public Setting<?> get( String key ) {
			for ( Setting<?> s : settings ) {
				if ( s.getKey().equals( key ) ) {
					return s;
				}
			}
			return null;
		}

		@SuppressWarnings( "unchecked" )
		public <T> T getValue( String key ) {
			Setting<?> s = get( key );
			if ( s == null ) {
				return null;
			}
			return (T) s.getValue();
		}

		@Override
		public Iterator<Setting<?>> iterator() {
			return settings.iterator();
		}
	}

	/*
	 * NOTE: when listening on the default interface 0.0.0.0,
	 * localhost will work, 127.0.0.1 will not
	 */
	public static final void main( String [] args ) throws Throwable {

		/*
		 * Configuration
		 */
		Configuration config = new Configuration()
			.add( new StringSetting( "host", "127.0.0.1" ) )
			.add( new NumericSetting( "port", 8080 ) )
			.add( new NumericSetting( "maxRequests", 0 ) )
			.add( new BooleanSetting( "captureEnabled", false ) )
			.add( new BooleanSetting( "debug", false ) )
			.add( new BooleanSetting( "trace", false ) )
			.add( new StringSetting( "config" ) )
			.parse( args );

		boolean debug = config.<Boolean>getValue( "debug" );
		if ( debug ) {
			System.err.println( "Server configuration settings:" );
			for ( Setting<?> setting : config ) {
				System.err.println( "\t" + setting.getKey() + "=" + setting.getValue() );
			}
		}

		int port = config.<Integer>getValue( "port" );
		int maxRequests = config.<Integer>getValue( "maxRequests" );
		Server server;
		if ( maxRequests > 0 ) {
			QueuedThreadPool threadPool = new QueuedThreadPool( maxRequests );
			server = new Server( threadPool );
		} else {
			server = new Server();
		}
		ServerConnector connector = new ServerConnector( server );
		connector.setHost( config.<String>getValue( "host" ) );
		connector.setPort( port );
		server.setConnectors( new ServerConnector [] {connector} );


		WebAppContext webapp = new WebAppContext();
        webapp.setContextPath( "/" );
        webapp.setWar( "/" );

        // enable request capture ?
        if ( config.<Boolean>getValue( "caputureEnabled" ) ) {
        	// add filter to capture requests
            webapp.addFilter( (Class<? extends Filter>) CaptureFilter.class, "/*", EnumSet.allOf( DispatcherType.class ) );

            // add servlet to serve captured requests
            ServletHolder holder = new ServletHolder( new CaptureServlet() );
            for ( Setting<?> setting : config ) {
            	holder.setInitParameter( setting.getKey(), String.valueOf( setting.getValue() ) );
            }
            webapp.addServlet( holder, CaptureFilter.CAPTURE_URI );
        }

        // install response handling
        String s = config.<String>getValue( "config" );
        if ( s == null ) {
        	// default handler
	        webapp.addServlet( new ServletHolder( new DefaultServlet() ), "/*" );
        } else {
        	// configuration based handler
	        ServletHolder holder = new ServletHolder( new MockServlet() );
            for ( Setting<?> setting : config ) {
            	holder.setInitParameter( setting.getKey(), String.valueOf( setting.getValue() ) );
            }
	        webapp.addServlet( holder, "/*" );

	        // capture analytics
	        webapp.addFilter( AnalyticsFilter.class, "/*", EnumSet.allOf( DispatcherType.class ) );

	        // gzip
	        webapp.addFilter( GzipFilter.class, "/*", EnumSet.allOf( DispatcherType.class ) );
        }

        /*
         * enable server-wide GZIP encoding
         * requires jetty 9.3+
         *
    	GzipHandler gzip = new GzipHandler();
    	gzip.addIncludedPaths( "/*" );
    	gzip.setServer( server );
         */

        // start the server
		server.setHandler( webapp );
		server.start();
		if ( debug ) {
			server.dumpStdErr();
		}
        server.join();
	}
}
