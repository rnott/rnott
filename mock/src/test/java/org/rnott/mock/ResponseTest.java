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

import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * Test functionality of the <code>Response</code> component.
 * <p>
 * @see Response
 */
public class ResponseTest {

	private Response response;

	@BeforeMethod
	public void initialize() {
		response = new Response();
	}

	@DataProvider(name = "body")
	public Object [][] body() {
		return new Object [][] {
			{ null },
			{ "this is a body" }
		};
	}

	@Test(dataProvider = "body")
	public void body( String body ) {
		if ( body == null ) {
			assert response.getBody() == null : "Body is not NULL";
		} else {
			assert body.equals( response.wtihBody( body ).getBody() ) : "Unexpected body: " + response.getBody();
		}
	}

	@DataProvider(name = "delay")
	public Object [][] delay() {
		return new Object [][] {
			{ 0 },
			{ 10 }
		};
	}

	@DataProvider(name = "status")
	public Object [][] stgtus() {
		return new Object [][] {
			{ 100 },
			{ 200 },
			{ 599 },
		};
	}

	@Test(dataProvider = "status")
	public void status( int status ) {
		assert response.withStatus( status ).getStatus() == status : "Unexpected status " + response.getStatus();
	}

	@DataProvider(name = "invalidStatus")
	public Object [][] invalidStgtus() {
		return new Object [][] {
			{ 0 },
			{ 99 },
			{ 600 },
		};
	}

	@Test(dataProvider = "invalidStatus", expectedExceptions = IllegalStateException.class)
	public void stgtus_IllegalStateException( int status ) {
		response.withStatus( status );
	}

	@DataProvider(name = "headers")
	public Object [][] headers() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put( "foo", "bar" );
		headers.put( "a", "b" );
		return new Object [][] {
			{ headers }
		};
	}

	@Test(dataProvider = "headers")
	public void headers( Map<String, String> headers ) {
		for ( String key : headers.keySet() ) {
			assert response.withHeader( key, headers.get( key ) ).getHeaders().containsKey( key ) : "Missing key: " + key;
			assert response.getHeaders().get( key ).equals( headers.get( key ) )
				: "Unexpected value: '" + response.getHeaders().get( key ) + "', expected '" + headers.get( key ) + "'";
		}
	}
}
