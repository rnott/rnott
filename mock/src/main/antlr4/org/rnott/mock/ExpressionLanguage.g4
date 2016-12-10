//
//   Copyright 2016 Randy Nott
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
// ANTLR v4 grammar for mock expression language

grammar	ExpressionLanguage;

content
	:	(expression | verbatim)*
	;

literal
	:	STRING
	|	INT
	|   LONG
	|	'true'
	|	'false'
	|	'null'
	;

parameter
	:	literal
	|	property
	|	method
	;

parameters
	:	OPEN_PARAMS CLOSE_PARAMS
	|	OPEN_PARAMS parameter (COMMA parameter)* CLOSE_PARAMS
	;

expression
	:	method
	|	property
	;

method
    :	EXP_START CID parameters EXP_END
    ;

property
    :	EXP_START CID EXP_END
    |	EXP_START ID EXP_END
    ;

verbatim
	:   CHARACTER+
	|	ID
	|	CID
	|	COMMA
	|	DOT
	|	DOLLAR
	|	CURLY_OPEN
	|	OPEN_BRACKET
	|	CLOSE_BRACKET
	|	EXP_END
	|   OPEN_PARAMS
	|   CLOSE_PARAMS
	|   ESCAPE
	|   ESCAPED
	|	literal
	;

/*
 * Use single quotes ONLY for literals as double quotes cause just about
 * everything to be parsed as literals in JSON, e.g. expressions enclosed
 * by double quotes are not evaluated.
 */
STRING
	: '\'' ('\\\\' | '\\\'' | ~[\\'])* '\''
//	: '"' ('\\\\' | '\\"' | ~[\\"])* '"'
	;

INT
	:	[0-9]+
	;

LONG
	:	INT 'L'
	;

OPEN_PARAMS
	:	'('
	;

CLOSE_PARAMS
	:	')'
	;

OPEN_BRACKET
	:	'['
	;

CLOSE_BRACKET
	:	']'
	;

DOLLAR
	:	'$'
	;

CURLY_OPEN
	:	'{'
	;

EXP_START
	:	DOLLAR CURLY_OPEN
	;

EXP_END
	:	'}'
	;

CID
	:	ID (DOT ID)+
	;

ID
	:	[a-zA-Z] [a-zA-Z0-9]*
	;

DOT
	:	'.'
	;

COMMA
	:	','
	;

ESCAPED
	: ESCAPE DOLLAR
	| ESCAPE CURLY_OPEN
	| ESCAPE EXP_END
	;

ESCAPE
	: '\\'
	;

CHARACTER
	:	.
	;
