lexer grammar RecafLexer;
options { caseInsensitive = true; }

// Literals
STRING_LITERAL: (STRING_SEGMENT | HASH_ESCAPE)+;
INT_LITERAL: DEC_LITERAL | HEX_LITERAL;
fragment DEC_LITERAL: [0-9]+;
fragment HEX_LITERAL: '$' [0-9a-f]+;
fragment STRING_SEGMENT: '\'' (~['\r\n] | '\'\'')* '\'';
fragment HASH_ESCAPE: '#' DEC_LITERAL;

// Keywords
PROGRAM: 'program';
BEGIN: 'begin';
END: 'end';
FUNCTION: 'function';
PROCEDURE: 'procedure';
EXTERNAL: 'external';
FORWARD: 'forward';
IF: 'if';
THEN: 'then';
ELSE: 'else';
FOR: 'for';
TO: 'to';
DOWNTO: 'downto';
WHILE: 'while';
REPEAT: 'repeat';
UNTIL: 'until';
DO: 'do';
CONST: 'const';
TYPE: 'type';
VAR: 'var';
ARRAY: 'array';
RECORD: 'record';
OF: 'of';
DIV: 'div';
MOD: 'mod';
AND: 'and';
OR: 'or';
NOT: 'not';
USES: 'uses';

// Identifiers
ID: ALPHA (ALPHA_NUM)*;
fragment ALPHA: [a-z_];
fragment ALPHA_NUM: ALPHA | [0-9];

// Symbols
LPAREN: '(';
RPAREN: ')';
LBRACK: '[';
RBRACK: ']';
SEMICOLON: ';';
ASSIGN: ':=';
COLON: ':';
COMMA: ',';
ELLIPSIS: '...';
RANGE: '..';
DOT: '.';
CARET: '^';

// Operations
PLUS: '+';
MINUS: '-';
TIMES: '*';
EQUALS: '=';
LEQ: '<=';
GEQ: '>=';
NEQ: '<>';
LT: '<';
GT: '>';

// Skip
PASCAL_BRACE_COMMENT: '{' .*? '}' -> skip;
PASCAL_PAREN_COMMENT: '(*' .*? '*)' -> skip;
WHITESPACE: [ \t\r\n\f]+ -> skip;

// Error
ERROR: .;
