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

	@Test(dataProvider = "delay")
	public void delay( long delay ) {
		assert response.withDelay( delay ).getDelay() == delay : "Unexpected delay: " + response.getDelay();
	}

	@DataProvider(name = "percentile")
	public Object [][] percentile() {
		return new Object [][] {
			{ 0 },
			{ 10 },
			{ 100 },
		};
	}

	@Test(dataProvider = "percentile")
	public void percentile( int percentile ) {
		assert response.withPercentile( percentile ).getPercentile() == percentile : "Unexpected percentile " + response.getPercentile();
	}

	@DataProvider(name = "invalidPercentile")
	public Object [][] invalidPercentile() {
		return new Object [][] {
			{ -1 },
			{ 101 },
		};
	}

	@Test(dataProvider = "invalidPercentile", expectedExceptions = IllegalStateException.class)
	public void percentile_IllegalStateException( int percentile ) {
		response.withPercentile( percentile );
	}

	@DataProvider(name = "stgtus")
	public Object [][] stgtus() {
		return new Object [][] {
			{ 100 },
			{ 200 },
			{ 599 },
		};
	}

	@Test(dataProvider = "stgtus")
	public void status( int status ) {
		assert response.withStatus( status ).getStatus() == status : "Unexpected status " + response.getStatus();
	}

	@DataProvider(name = "invalidStgtus")
	public Object [][] invalidStgtus() {
		return new Object [][] {
			{ 0 },
			{ 99 },
			{ 600 },
		};
	}

	@Test(dataProvider = "invalidStgtus", expectedExceptions = IllegalStateException.class)
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

	@Test
	public void compareTo() {
		assert new Response().withPercentile( 100 ).compareTo( response.withPercentile( 100 ) ) == 0 : "Equivalence failed";
		assert new Response().withPercentile( 100 ).compareTo( response.withPercentile( 99 ) ) > 0 : "After failed";
		assert new Response().withPercentile( 99 ).compareTo( response.withPercentile( 100 ) ) < 0 : "Before failed";
		assert new Response().withPercentile( 100 ).withDelay( 100 ).withStatus( 200 ).compareTo( response.withPercentile( 100 ) ) == 0 : "Equivalence failed";
		assert new Response().withPercentile( 100 ).withDelay( 100 ).withStatus( 200 ).compareTo( response.withPercentile( 99 ) ) > 0 : "After failed";
		assert new Response().withPercentile( 99 ).withDelay( 100 ).withStatus( 200 ).compareTo( response.withPercentile( 100 ) ) < 0 : "Before failed";
	}
}
