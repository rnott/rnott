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


/**
 * Support for <code>java.lang.String</code> during expression language evaluation.
 * All public instance methods are exposed via reflection. The evaluator is activated using the psuedo-class
 * 'string'. Method invocation requires the following pattern:
 * <pre>
 * method(instance[,parameters ...])
 * where <i>method</i> is the method name
 * <i>instance</i> is an instance of <code>java.lang.String</code> to target (usually a named request property)
 * <i>parameters</i> are zero or more parameters to be passed to the method
 * </pre>
 * <p>
 * @see java.lang.String
 */
public class StringEvaluator implements Evaluator {

	@Override
	public String key() {
		return "string";
	}

	/*
	 * (non-Javadoc)
	 * @see org.rnott.mock.Evaluator#evaluate(java.lang.String, java.lang.Object[])
	 */
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
