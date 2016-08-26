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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class CaptureFilter implements Filter {

	public static final String CAPTURE_URI = "/requests";

	private ServletContext context;
	private ObjectMapper mapper;
	private boolean debug = false;

	@Override
	public void destroy() {
		// TODO: delete files?
	}

	@Override
	public void doFilter( ServletRequest httpRequest, ServletResponse httpResponse, FilterChain filters ) throws IOException, ServletException {
		// skip calls to retrieve captured requests
		if ( CAPTURE_URI.equals( ((HttpServletRequest) httpRequest ).getRequestURI() ) ) {
			filters.doFilter( httpRequest, httpResponse );
			return;
		}

		// add correlation id to response
		String uuid = UUID.randomUUID().toString();
		((HttpServletResponse) httpResponse).addHeader( "X-Request-Correlation-Id", uuid );

		// capture the request
		RequestWrapper request = new RequestWrapper( (HttpServletRequest) httpRequest );

		// process the request
		filters.doFilter( request, httpResponse );

		// log the request
        capture( uuid, request );
	}

	@Override
	public void init( FilterConfig config ) throws ServletException {
		context = config.getServletContext();

		mapper = new ObjectMapper()
			.configure( SerializationFeature.INDENT_OUTPUT, true );
		try {
			debug = Boolean.parseBoolean( config.getInitParameter( "debug" ) );
		} catch ( Throwable ignore ) {}

		if ( debug ) {
			context.log( "Using capture directory: " + Configuration.getWorkDirectory().getAbsolutePath() );
		}
	}

	private void capture( String id, RequestWrapper request ) {
		try {
			File f = new File( Configuration.getWorkDirectory(), id + "-request.json" );
			if ( debug ) {
				context.log( "Created capture file: " + f.getAbsolutePath() );
			}
			FileOutputStream stream = new FileOutputStream( f );
			mapper.writeValue( stream, Serializer.serialize( request, request.getContent() ) );

		} catch ( Throwable t ) {
			t.printStackTrace();
		}
	}

	public static class RequestWrapper extends HttpServletRequestWrapper {

		private TeeInputStream stream;

		public RequestWrapper( HttpServletRequest request ) {
			super( request );
		}

		public byte [] getContent() {
			if ( stream == null ) {
				return new byte[0];
			} else {
				byte [] content = stream.baos.toByteArray();
				if ( content.length == 1 && content[0] == -1 ) {
					// empty body will be considered no body
					return new byte[0];
				}
				return content;
			}
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			if ( stream == null ) {
				stream = new TeeInputStream( super.getInputStream() );
			}
			return stream;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			return new BufferedReader( new InputStreamReader( getInputStream() ) );
		}
	}

	private static class TeeInputStream extends ServletInputStream {

		private final ServletInputStream sink;
		private final ByteArrayOutputStream baos;

		private TeeInputStream( ServletInputStream sink ) {
			this.sink = sink;
			baos = new ByteArrayOutputStream();
		}

		@Override
		public int read() throws IOException {
			int c = sink.read();
			// don't record EOF
			if ( c >= 0 ) {
				baos.write( c );
			}
			return c;
		}

		@Override
		public boolean isFinished() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isReady() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setReadListener( ReadListener readListener ) {
			// TODO Auto-generated method stub
			
		}
	}
}
