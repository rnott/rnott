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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * Test functionality of the <code>Serializer</code> component.
 * <p>
 * @see Serializer
 */
public class SerializerTest {

	@DataProvider(name = "requests")
	public Object [][] requests() {
		return new Object [][] {
			{ "GET", "/foo/bar", new String [][] {}, new String [][] {}, "no headers, no parameters" },
			{ "GET", "/foo/bar", new String [][] {{"foo","bar"}, {"one","two"}}, new String [][] {}, "no headers, some parameters" },
			{ "GET", "/foo/bar", new String [][] {}, new String [][] {{"foo","bar"}, {"one","two"}}, "some headers, no parameters" },
			{ "GET", "/foo/bar", new String [][] {{"foo","bar"}, {"foo","bar2"}}, new String [][] {{"foo","bar"}, {"foo","bar2"}}, "some headers, some parameters" },
		};
	}

	private static class MockHttpServletRequestImpl extends MockHttpServletRequest {

		private final String method;
		private final String uri;
		private final Map<String, List<String>> params;
		private final Map<String, List<String>> headers;

		MockHttpServletRequestImpl( String method, String uri ) {
			this.method = method;
			this.uri = uri;
			this.params = new HashMap<String, List<String>>();
			this.headers = new HashMap<String, List<String>>();
		}

		public MockHttpServletRequestImpl addParameter( String key, String value ) {
			if ( ! params.containsKey( key ) ) {
				params.put( key, new ArrayList<String>() );
			}
			params.get( key ).add( value );

			return this;
		}

		public MockHttpServletRequestImpl addHeader( String key, String value ) {
			if ( ! headers.containsKey( key ) ) {
				headers.put( key, new ArrayList<String>() );
			}
			headers.get( key ).add( value );

			return this;
		}

		/* 
         * (non-Javadoc)
         * @see org.rnott.mock.MockHttpServletRequest#getHeader(java.lang.String)
         */
        @Override
        public String getHeader( String name ) {
        	if ( headers.containsKey( name ) ) {
        		return headers.get( name ).get( 0 );
        	}
    		return null;
        }

		/* 
         * (non-Javadoc)
         * @see org.rnott.mock.MockHttpServletRequest#getMethod()
         */
        @Override
        public String getMethod() {
	        return method;
        }

		/* 
         * (non-Javadoc)
         * @see org.rnott.mock.MockHttpServletRequest#getQueryString()
         */
        @Override
        public String getQueryString() {
        	if ( params.size() == 0 ) {
        		return null;
        	}
	        StringBuilder sb = new StringBuilder();
	        for ( String param : params.keySet() ) {
	        	if ( sb.length() > 0 ) {
	        		sb.append( '&' );
	        	}
	        	for ( String value : params.get( param ) ) {
	        		sb.append( param ).append( '=' ).append( value );
	        	}
	        }
	        return sb.toString();
        }

		/* 
         * (non-Javadoc)
         * @see org.rnott.mock.MockHttpServletRequest#getRequestURI()
         */
        @Override
        public String getRequestURI() {
	        return uri;
        }

		/* 
         * (non-Javadoc)
         * @see org.rnott.mock.MockHttpServletRequest#getParameterNames()
         */
        @Override
        public Enumeration<String> getParameterNames() {
	        return new Enumeration<String>() {

	        	Iterator<String> names = params.keySet().iterator();

	        	@Override
                public boolean hasMoreElements() {
	                return names.hasNext();
                }

				@Override
                public String nextElement() {
	                return names.next();
                }
	        };
        }

		/* 
         * (non-Javadoc)
         * @see org.rnott.mock.MockHttpServletRequest#getParameterValues(java.lang.String)
         */
        @Override
        public String [] getParameterValues( String name ) {
	        if ( params.containsKey( name ) ) {
	        	int i = 0;
	        	List<String> v = params.get( name );
	        	String [] values = new String [v.size()];
	        	for ( String s : v ) {
	        		values[i++] = s;
	        	}
	        	return values;
	        }
	        return null;
        }

		/* 
         * (non-Javadoc)
         * @see org.rnott.mock.MockHttpServletRequest#getHeaderNames()
         */
        @Override
        public Enumeration<String> getHeaderNames() {
	        return new Enumeration<String>() {
	        	private Iterator<String> keys = headers.keySet().iterator();

	        	@Override
                public boolean hasMoreElements() {
	                return keys.hasNext();
                }

				@Override
                public String nextElement() {
	                return keys.next();
                }
	        };
        }

		/* 
         * (non-Javadoc)
         * @see org.rnott.mock.MockHttpServletRequest#getHeaders(java.lang.String)
         */
        @Override
        public Enumeration<String> getHeaders( final String name ) {
	        return new Enumeration<String>() {
	        	Iterator<String> values = headers.containsKey( name ) ? 
	        		headers.get( name ).iterator() :
	        		new ArrayList<String>().iterator();

				@Override
                public boolean hasMoreElements() {
	                return values.hasNext();
                }

				@Override
                public String nextElement() {
	                return values.next();
                }
	        };
        }
	}

	@SuppressWarnings( "unchecked" )
    @Test(dataProvider = "requests")
	public void serialize( String method, String uri, String [][] params, String [][] headers, String body ) throws IOException {
		MockHttpServletRequestImpl request = new MockHttpServletRequestImpl( method, uri );
		if ( params != null ) {
			for ( String [] param : params ) {
				request.addParameter( param[0], param[1] );
			}
		}
		if ( headers != null ) {
			for ( String [] hdr : headers ) {
				request.addHeader( hdr[0], hdr[1] );
			}
		}
		Map<String, Object> result = Serializer.serialize( request, body.getBytes() );

		assert result.containsKey( Serializer.KEY_METHOD ) : "No request method";
		assert method.equals( result.get( Serializer.KEY_METHOD ) ) : "Expected method: " + method + ", got " + result.get( Serializer.KEY_METHOD );
		assert result.containsKey( Serializer.KEY_URI ) : "No request URI";
		assert uri.equals( result.get( Serializer.KEY_URI ) ) : "Expected URI: " + uri + ", got " + result.get( Serializer.KEY_URI );
		assert result.containsKey( Serializer.KEY_HEADERS ) : "No request HEADERS";
		if ( headers == null || headers.length == 0 ) {
			assert ((Map<String,List<String>>) result.get( Serializer.KEY_HEADERS )).size() == 0 : "Headers not empty";
		} else {
			Map<String,List<Object>> serialized = (Map<String,List<Object>>) result.get( Serializer.KEY_HEADERS );
			for ( String [] hdr : headers ) {
				assert serialized.containsKey( hdr[0] );
				List<Object> values = serialized.get( hdr[0] );
				assert values != null : "No header values: " + hdr[0];
				assert values.contains( hdr[1] ) : "Missing HEADER value: " + hdr[1];
			}
		}
		assert result.containsKey( Serializer.KEY_PARAMETERS ) : "No request PARAMETERS";
		if ( params == null || params.length == 0 ) {
			assert ((Map<String,List<String>>) result.get( Serializer.KEY_PARAMETERS )).size() == 0 : "Parameters not empty";
		} else {
			Map<String,List<Object>> serialized = (Map<String,List<Object>>) result.get( Serializer.KEY_PARAMETERS );
			for ( String [] p : params ) {
				assert serialized.containsKey( p[0] );
				List<Object> values = serialized.get( p[0] );
				assert values != null : "No parameter values: " + p[0];
				assert values.contains( p[1] ) : "Missing parameter value: " + p[1] + " " + values;
			}
		}
		assert result.containsKey( Serializer.KEY_BODY ) : "No request BODY";
		assert body.equals( result.get( Serializer.KEY_BODY ) ) : "Body mismatch";
	}
}
