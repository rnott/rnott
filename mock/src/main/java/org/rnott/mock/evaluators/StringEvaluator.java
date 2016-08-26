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

package org.rnott.mock.evaluators;

import java.lang.reflect.Method;
import org.rnott.mock.Evaluator;


/**
 * TODO: document StringEvaluator
 *
 */
public class StringEvaluator implements Evaluator {

	@Override
	public String key() {
		return "string";
	}

	@Override
	public Object evaluate( String method, Object ... args ) {
		if ( args == null || args.length == 0 || ! (args[0] instanceof String) ) {
			throw new IllegalStateException( "String target instance must be the first parameter provided" );
		}
		Object [] params = new Object[ args.length - 1];
		System.arraycopy( args, 1, params, 0, args.length - 1 );
		Method match = null;
		for ( Method m : String.class.getMethods() ) {
			if ( matches( m, method, params ) ) {
				match = m;
				break;
			}
		}
		if ( match == null ) {
			throw new IllegalStateException( "String." + method + format( params ) + " does not exist or is not accessible" );
		}
		try {
			Object value = match.invoke( args[0], params );
			return value;
		} catch ( Throwable t ) {
			throw new RuntimeException( "Failed to invoke method: " + match, t );
		}
	}

	private boolean matches( Method m, String name, Object ... args ) {
		if ( m.getName().equals( name ) ) {
			Class<?>[] types = m.getParameterTypes();
			if ( args.length == types.length ) {
				for ( int i = 0; i < types.length; i++ ) {
					if ( ! matches( args[i].getClass(), types[i] ) ) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	private boolean matches( Class<?> source, Class<?> target ) {
		if ( target.isAssignableFrom( source ) ) {
			return true;
		} else if ( target == long.class ) {
			return ( Long.class.isAssignableFrom( source ) );

		} else if ( target == int.class ) {
			return ( Integer.class.isAssignableFrom( source ) );

		} else if ( target == short.class ) {
			return ( Short.class.isAssignableFrom( source ) );

		} else if ( target == char.class ) {
			return ( Character.class.isAssignableFrom( source ) );

		} else if ( target == byte.class ) {
			return ( Byte.class.isAssignableFrom( source ) );
		}

		return false;
	}

	private String format( Object ... args ) {
		StringBuilder sb = new StringBuilder( "(" );
		for ( int i = 0; i < args.length; i++ ) {
			if ( sb.length() > 1 ) {
				sb.append( ", " );
			}
			sb.append( args[i].getClass().getName() );
		}
		sb.append( ")" );
		return sb.toString();
	}
}
