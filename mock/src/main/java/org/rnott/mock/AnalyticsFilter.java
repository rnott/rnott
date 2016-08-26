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
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * TODO: document AnalyticsFilter
 *
 */
public class AnalyticsFilter implements Filter {

	private ServletContext context;

	@Override
	public void init( FilterConfig filterConfig ) throws ServletException {
		context = filterConfig.getServletContext();
	}

	@Override
	public void destroy() {
	}

	private AtomicLong count = new AtomicLong();
	private AtomicLong elapsed = new AtomicLong();

	@Override
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {
		// skip calls to retrieve captured requests
		if ( CaptureFilter.CAPTURE_URI.equals( ((HttpServletRequest) request ).getRequestURI() ) ) {
			chain.doFilter( request, response );
			return;
		}

		// TODO: other analytics
		long start = System.currentTimeMillis();
		chain.doFilter( request, response );
		long ms = System.currentTimeMillis() - start;
		long currentCount = count.incrementAndGet();
		long currentElapsed = elapsed.addAndGet( ms );
		((HttpServletResponse) response).addHeader( "X-Elapsed-Time", String.valueOf( ms ) + "ms" );
		if ( currentCount % 10000 == 0 ) {
			context.log( "Serviced " + currentCount + " requests in " + currentElapsed + "ms" );
		}
	}
}
