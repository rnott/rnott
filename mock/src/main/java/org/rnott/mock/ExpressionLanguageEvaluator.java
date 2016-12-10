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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.rnott.mock.ExpressionLanguageParser.ExpressionContext;
import org.rnott.mock.ExpressionLanguageParser.LiteralContext;
import org.rnott.mock.ExpressionLanguageParser.MethodContext;
import org.rnott.mock.ExpressionLanguageParser.ParameterContext;
import org.rnott.mock.ExpressionLanguageParser.ParametersContext;
import org.rnott.mock.ExpressionLanguageParser.PropertyContext;
import org.rnott.mock.ExpressionLanguageParser.VerbatimContext;

/**
 * Expression Language (EL) evaluator. Instances of this type are used to transform
 * a service request according to the EL syntax.
 */
public class ExpressionLanguageEvaluator extends ExpressionLanguageBaseListener {

	private StringBuilder content = new StringBuilder();

	/**
	 * Get the transformed text.
	 * <p>
	 * @return the transformed text.
	 */
	public String getText() {
		return content.toString();
	}

	/**
	 * Process verbatim text.
	 * <p>
	 * @param the EL context.
	 */
	@Override
	public void enterVerbatim( VerbatimContext ctx ) {
		// append verbatim text
		// skip escape character
		Object obj = ctx.getChild( 0 ).getPayload();
		if ( obj instanceof CommonToken && ((CommonToken) obj).getType() == ExpressionLanguageParser.ESCAPED ) {
			content.append( ctx.getText().substring( 1 ) );
		} else {
			content.append( ctx.getText() );
		}
	}

	/**
	 * Begin processing an EL expression.
	 * <p>
	 * @param ctx the EL expression context.
	 */
	@Override
	public void enterExpression( ExpressionContext ctx ) {
		// expression is an EL method or property
		super.enterExpression( ctx );
		ParserRuleContext p = (ParserRuleContext) ctx.getChild( 0 );
		if ( p == null ) {
			//throw new IllegalStateException( ctx. );
		}
		switch ( p.getRuleIndex() ) {
		case ExpressionLanguageParser.RULE_method:
			// append invocation of an EL method
			content.append( evaluate( (MethodContext) p ) );
			break;
		case ExpressionLanguageParser.RULE_property:
			// append resolution of an EL property
			content.append(  evaluate( (PropertyContext) p ) );
			break;
		}
	}

	/**
	 * Evaluate an EL literal.
	 * <p>
	 * @param ctx the EL literal context.
	 * @return the resolved value.
	 */
	private Object evaluate( LiteralContext ctx ) {
		String s = ctx.getText();
		switch ( ((CommonToken) ctx.getChild( 0 ).getPayload()).getType() ) {
		case ExpressionLanguageParser.STRING:
			// remove surrounding quotes
			return s.substring( 1, s.length() - 1 );
		case ExpressionLanguageParser.INT:
			// parse as integer
			return Integer.parseInt( s );
		case ExpressionLanguageParser.LONG:
			// parse as long
			return Long.parseLong( s.substring( 0, s.length() - 1 ) );
		}
		return s;
	}

	/**
	 * Evaluate an EL property.
	 * <p>
	 * @param ctx the EL property context.
	 * @return the resolved value.
	 * @throws IllegalStateException if the property specified by the expression
	 * is not a request parameter.
	 */
	private Object evaluate( PropertyContext ctx ) {
		Map<String, String> params = MockContext.get().getParameters();
		String key = ctx.getChild( 1 ).getText();
		if ( params.containsKey( key ) ) {
			return params.get( key );
		}
		throw new IllegalStateException( "Property not present as a request parameter: " + key + " " + params.keySet() );
	}

	/**
	 * Evaluate an EL method.
	 * <p>
	 * @param ctx the EL method context.
	 * @return the resolved value.
	 * @throws IllegalStateException if the EL method specifies an unknown evaluator.
	 */
	private Object evaluate( MethodContext ctx ) {
		String s = ctx.getChild( 1 ).getText();
		int pos = s.indexOf( '.' );
		String type = s.substring( 0, pos );
		String method = s.substring( pos + 1 );
		ParametersContext pc = (ParametersContext) ctx.getChild( 2 );
		List<Object> params = new ArrayList<Object>();
		for ( int i = 1, count = pc.getChildCount() - 1; i < count; i++ ) {
			if ( pc.getChild( i ) instanceof ParameterContext ) {
				ParserRuleContext p = (ParserRuleContext) pc.getChild( i ).getChild( 0 );
				switch ( p.getRuleIndex() ) {
				case ExpressionLanguageParser.RULE_literal:
					params.add( evaluate( (LiteralContext) p ) );
					break;
				case ExpressionLanguageParser.RULE_method:
					params.add( evaluate( (MethodContext) p ) );
					break;
				case ExpressionLanguageParser.RULE_property:
					params.add( evaluate( (PropertyContext) p ) );
					break;
				}
			}
		}

		return MockContext.get().getEvaluator( type ).evaluate( method, params.toArray() );
	}
}