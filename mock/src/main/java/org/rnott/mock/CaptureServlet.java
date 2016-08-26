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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 
 * TODO: document CaptureServlet
 *
 */
public class CaptureServlet extends HttpServlet {

	private static final long serialVersionUID = 5027400758317704816L;

	@Override
	protected void service( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
		response.setContentType( "application/json" );
		File dir = Configuration.getWorkDirectory();
		OutputStream out = response.getOutputStream();

		// all or some
		int count = 0;
		out.write( "[".getBytes() );
		String [] ids = request.getParameterValues( "correlation-id" );
		if ( ids == null || ids.length == 0 ) {
			// all
			for ( File f : dir.listFiles() ) {
				if ( f.getName().endsWith( "-request.json" ) ) {
					// one of ours
					write( out, f, count );
					count++;
				}
			}

		} else {
			// handle both id=1, id=2, ...
			// and id=1,2...
			for ( String s : ids ) {
				String [] values = s.split( "," );
				for ( String id : values ) {
					File f = new File( dir, id.trim() + "-request.json" );
					write( out, f, count );
					count++;
				}
			}
		}
		out.write( "]".getBytes() );

		response.setStatus( HttpServletResponse.SC_OK );
	}

	private void write( OutputStream out, File source, int count ) throws IOException {
		if ( count > 0 ) {
			out.write( ",".getBytes() );
		}
		InputStream in = new FileInputStream( source );
		byte [] b = new byte[8192];
		try {
			int bytes = in.read( b );
			while ( bytes > 0 ) {
				out.write( b, 0, bytes );
				bytes = in.read( b );
			}
		} finally {
			in.close();
		}
	}
}
