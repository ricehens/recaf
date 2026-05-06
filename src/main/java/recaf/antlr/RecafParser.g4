parser grammar RecafParser;
options { tokenVocab = RecafLexer; }

program: program_decl declaration* main_block EOF;

program_decl: PROGRAM ID SEMICOLON;
declaration: type_section
           | const_section
           | var_section
           | function_decl
           | procedure_decl;

type_section: TYPE type_decl SEMICOLON (type_decl SEMICOLON)*;
type_decl: ID EQUALS type_ref;
type_ref: base_type | array_type | record_type | enum_type | pointer_type;

base_type: ID;

array_type: ARRAY LBRACK array_index_range (COMMA array_index_range)* RBRACK (OF type_ref)?;
array_index_range: expr RANGE expr;

record_type: RECORD var_decls END;

enum_type: LPAREN id_list RPAREN;

pointer_type: CARET base_type;

const_section: CONST (const_decl SEMICOLON)*;
const_decl: ID EQUALS expr;

var_section: VAR var_decls;
var_decls: (var_decl SEMICOLON)*;
var_decl: id_list (COLON type_ref)?;
id_list: ID (COMMA ID)*;

function_decl: FUNCTION ID formal_params? (COLON type_ref)? SEMICOLON (EXTERNAL | FORWARD | routine_block) SEMICOLON;
procedure_decl: PROCEDURE ID formal_params? SEMICOLON (EXTERNAL | FORWARD | routine_block) SEMICOLON;
formal_params: LPAREN (ELLIPSIS | param_decls?) RPAREN;
param_decls: var_decl (SEMICOLON var_decl)*;

routine_block: local_declaration* block;
local_declaration: type_section | const_section | var_section;

main_block: block DOT;
block: BEGIN statement_list? END;
statement_list: statement (SEMICOLON statement)* SEMICOLON?;

statement: assign_statement
         | method_call_statement
         | if_else
         | for_loop
         | while_loop
         | repeat_loop
         | block;

assign_statement: location ASSIGN expr;

method_call_statement: method_call;
method_call: ID (LPAREN args? RPAREN)?;
args: arg (COMMA arg)*;
arg: expr | STRING_LITERAL;

location: ID location_access*;
location_access: indexer | field_selector | deref_selector;
indexer: LBRACK expr (COMMA expr)* RBRACK;
field_selector: DOT ID;
deref_selector: CARET;

expr: location
   | method_call
   | literal
   | (MINUS | NOT) expr
   | expr mul_op expr // */
   | expr add_op expr // +-
   | expr rel_op expr // relational
   | LPAREN expr RPAREN;
mul_op: TIMES | DIV | MOD | AND;
add_op: PLUS | MINUS | OR;
rel_op: EQUALS | NEQ | LT | GT | LEQ | GEQ;

literal: INT_LITERAL | BOOL_LITERAL;

if_else: IF expr THEN statement (ELSE statement)?;
for_loop: FOR ID ASSIGN expr (TO | DOWNTO) expr DO statement;
while_loop: WHILE expr DO statement;
repeat_loop: REPEAT statement_list UNTIL expr;
