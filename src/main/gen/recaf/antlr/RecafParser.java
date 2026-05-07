// Generated from RecafParser.g4 by ANTLR 4.13.2
package recaf.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class RecafParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		BOOL_LITERAL=1, STRING_LITERAL=2, INT_LITERAL=3, PROGRAM=4, BEGIN=5, END=6, 
		FUNCTION=7, PROCEDURE=8, EXTERNAL=9, FORWARD=10, IF=11, THEN=12, ELSE=13, 
		FOR=14, TO=15, DOWNTO=16, WHILE=17, REPEAT=18, UNTIL=19, DO=20, CONST=21, 
		TYPE=22, VAR=23, ARRAY=24, RECORD=25, OF=26, DIV=27, MOD=28, AND=29, OR=30, 
		NOT=31, ID=32, LPAREN=33, RPAREN=34, LBRACK=35, RBRACK=36, SEMICOLON=37, 
		ASSIGN=38, COLON=39, COMMA=40, ELLIPSIS=41, RANGE=42, DOT=43, CARET=44, 
		PLUS=45, MINUS=46, TIMES=47, EQUALS=48, LEQ=49, GEQ=50, NEQ=51, LT=52, 
		GT=53, PASCAL_BRACE_COMMENT=54, PASCAL_PAREN_COMMENT=55, WHITESPACE=56, 
		ERROR=57;
	public static final int
		RULE_program = 0, RULE_program_decl = 1, RULE_declaration = 2, RULE_type_section = 3, 
		RULE_type_decl = 4, RULE_type_ref = 5, RULE_base_type = 6, RULE_array_type = 7, 
		RULE_array_index_range = 8, RULE_record_type = 9, RULE_enum_type = 10, 
		RULE_pointer_type = 11, RULE_const_section = 12, RULE_const_decl = 13, 
		RULE_var_section = 14, RULE_var_decls = 15, RULE_var_decl = 16, RULE_id_list = 17, 
		RULE_function_decl = 18, RULE_procedure_decl = 19, RULE_formal_params = 20, 
		RULE_param_decls = 21, RULE_routine_block = 22, RULE_local_declaration = 23, 
		RULE_main_block = 24, RULE_block = 25, RULE_statement_list = 26, RULE_statement = 27, 
		RULE_assign_statement = 28, RULE_method_call_statement = 29, RULE_method_call = 30, 
		RULE_args = 31, RULE_arg = 32, RULE_location = 33, RULE_location_access = 34, 
		RULE_indexer = 35, RULE_field_selector = 36, RULE_deref_selector = 37, 
		RULE_expr = 38, RULE_mul_op = 39, RULE_add_op = 40, RULE_rel_op = 41, 
		RULE_literal = 42, RULE_if_else = 43, RULE_for_loop = 44, RULE_while_loop = 45, 
		RULE_repeat_loop = 46;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "program_decl", "declaration", "type_section", "type_decl", 
			"type_ref", "base_type", "array_type", "array_index_range", "record_type", 
			"enum_type", "pointer_type", "const_section", "const_decl", "var_section", 
			"var_decls", "var_decl", "id_list", "function_decl", "procedure_decl", 
			"formal_params", "param_decls", "routine_block", "local_declaration", 
			"main_block", "block", "statement_list", "statement", "assign_statement", 
			"method_call_statement", "method_call", "args", "arg", "location", "location_access", 
			"indexer", "field_selector", "deref_selector", "expr", "mul_op", "add_op", 
			"rel_op", "literal", "if_else", "for_loop", "while_loop", "repeat_loop"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, "'program'", "'begin'", "'end'", "'function'", 
			"'procedure'", "'external'", "'forward'", "'if'", "'then'", "'else'", 
			"'for'", "'to'", "'downto'", "'while'", "'repeat'", "'until'", "'do'", 
			"'const'", "'type'", "'var'", "'array'", "'record'", "'of'", "'div'", 
			"'mod'", "'and'", "'or'", "'not'", null, "'('", "')'", "'['", "']'", 
			"';'", "':='", "':'", "','", "'...'", "'..'", "'.'", "'^'", "'+'", "'-'", 
			"'*'", "'='", "'<='", "'>='", "'<>'", "'<'", "'>'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "BOOL_LITERAL", "STRING_LITERAL", "INT_LITERAL", "PROGRAM", "BEGIN", 
			"END", "FUNCTION", "PROCEDURE", "EXTERNAL", "FORWARD", "IF", "THEN", 
			"ELSE", "FOR", "TO", "DOWNTO", "WHILE", "REPEAT", "UNTIL", "DO", "CONST", 
			"TYPE", "VAR", "ARRAY", "RECORD", "OF", "DIV", "MOD", "AND", "OR", "NOT", 
			"ID", "LPAREN", "RPAREN", "LBRACK", "RBRACK", "SEMICOLON", "ASSIGN", 
			"COLON", "COMMA", "ELLIPSIS", "RANGE", "DOT", "CARET", "PLUS", "MINUS", 
			"TIMES", "EQUALS", "LEQ", "GEQ", "NEQ", "LT", "GT", "PASCAL_BRACE_COMMENT", 
			"PASCAL_PAREN_COMMENT", "WHITESPACE", "ERROR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "RecafParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public RecafParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public Program_declContext program_decl() {
			return getRuleContext(Program_declContext.class,0);
		}
		public Main_blockContext main_block() {
			return getRuleContext(Main_blockContext.class,0);
		}
		public TerminalNode EOF() { return getToken(RecafParser.EOF, 0); }
		public List<DeclarationContext> declaration() {
			return getRuleContexts(DeclarationContext.class);
		}
		public DeclarationContext declaration(int i) {
			return getRuleContext(DeclarationContext.class,i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitProgram(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			program_decl();
			setState(98);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 14680448L) != 0)) {
				{
				{
				setState(95);
				declaration();
				}
				}
				setState(100);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(101);
			main_block();
			setState(102);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Program_declContext extends ParserRuleContext {
		public TerminalNode PROGRAM() { return getToken(RecafParser.PROGRAM, 0); }
		public TerminalNode ID() { return getToken(RecafParser.ID, 0); }
		public TerminalNode SEMICOLON() { return getToken(RecafParser.SEMICOLON, 0); }
		public Program_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterProgram_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitProgram_decl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitProgram_decl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Program_declContext program_decl() throws RecognitionException {
		Program_declContext _localctx = new Program_declContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_program_decl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104);
			match(PROGRAM);
			setState(105);
			match(ID);
			setState(106);
			match(SEMICOLON);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeclarationContext extends ParserRuleContext {
		public Type_sectionContext type_section() {
			return getRuleContext(Type_sectionContext.class,0);
		}
		public Const_sectionContext const_section() {
			return getRuleContext(Const_sectionContext.class,0);
		}
		public Var_sectionContext var_section() {
			return getRuleContext(Var_sectionContext.class,0);
		}
		public Function_declContext function_decl() {
			return getRuleContext(Function_declContext.class,0);
		}
		public Procedure_declContext procedure_decl() {
			return getRuleContext(Procedure_declContext.class,0);
		}
		public DeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationContext declaration() throws RecognitionException {
		DeclarationContext _localctx = new DeclarationContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_declaration);
		try {
			setState(113);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TYPE:
				enterOuterAlt(_localctx, 1);
				{
				setState(108);
				type_section();
				}
				break;
			case CONST:
				enterOuterAlt(_localctx, 2);
				{
				setState(109);
				const_section();
				}
				break;
			case VAR:
				enterOuterAlt(_localctx, 3);
				{
				setState(110);
				var_section();
				}
				break;
			case FUNCTION:
				enterOuterAlt(_localctx, 4);
				{
				setState(111);
				function_decl();
				}
				break;
			case PROCEDURE:
				enterOuterAlt(_localctx, 5);
				{
				setState(112);
				procedure_decl();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Type_sectionContext extends ParserRuleContext {
		public TerminalNode TYPE() { return getToken(RecafParser.TYPE, 0); }
		public List<Type_declContext> type_decl() {
			return getRuleContexts(Type_declContext.class);
		}
		public Type_declContext type_decl(int i) {
			return getRuleContext(Type_declContext.class,i);
		}
		public List<TerminalNode> SEMICOLON() { return getTokens(RecafParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(RecafParser.SEMICOLON, i);
		}
		public Type_sectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type_section; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterType_section(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitType_section(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitType_section(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Type_sectionContext type_section() throws RecognitionException {
		Type_sectionContext _localctx = new Type_sectionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_type_section);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			match(TYPE);
			setState(116);
			type_decl();
			setState(117);
			match(SEMICOLON);
			setState(123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ID) {
				{
				{
				setState(118);
				type_decl();
				setState(119);
				match(SEMICOLON);
				}
				}
				setState(125);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Type_declContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(RecafParser.ID, 0); }
		public TerminalNode EQUALS() { return getToken(RecafParser.EQUALS, 0); }
		public Type_refContext type_ref() {
			return getRuleContext(Type_refContext.class,0);
		}
		public Type_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterType_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitType_decl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitType_decl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Type_declContext type_decl() throws RecognitionException {
		Type_declContext _localctx = new Type_declContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_type_decl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			match(ID);
			setState(127);
			match(EQUALS);
			setState(128);
			type_ref();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Type_refContext extends ParserRuleContext {
		public Base_typeContext base_type() {
			return getRuleContext(Base_typeContext.class,0);
		}
		public Array_typeContext array_type() {
			return getRuleContext(Array_typeContext.class,0);
		}
		public Record_typeContext record_type() {
			return getRuleContext(Record_typeContext.class,0);
		}
		public Enum_typeContext enum_type() {
			return getRuleContext(Enum_typeContext.class,0);
		}
		public Pointer_typeContext pointer_type() {
			return getRuleContext(Pointer_typeContext.class,0);
		}
		public Type_refContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type_ref; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterType_ref(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitType_ref(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitType_ref(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Type_refContext type_ref() throws RecognitionException {
		Type_refContext _localctx = new Type_refContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_type_ref);
		try {
			setState(135);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(130);
				base_type();
				}
				break;
			case ARRAY:
				enterOuterAlt(_localctx, 2);
				{
				setState(131);
				array_type();
				}
				break;
			case RECORD:
				enterOuterAlt(_localctx, 3);
				{
				setState(132);
				record_type();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 4);
				{
				setState(133);
				enum_type();
				}
				break;
			case CARET:
				enterOuterAlt(_localctx, 5);
				{
				setState(134);
				pointer_type();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Base_typeContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(RecafParser.ID, 0); }
		public Base_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_base_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterBase_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitBase_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitBase_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Base_typeContext base_type() throws RecognitionException {
		Base_typeContext _localctx = new Base_typeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_base_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(137);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Array_typeContext extends ParserRuleContext {
		public TerminalNode ARRAY() { return getToken(RecafParser.ARRAY, 0); }
		public TerminalNode LBRACK() { return getToken(RecafParser.LBRACK, 0); }
		public List<Array_index_rangeContext> array_index_range() {
			return getRuleContexts(Array_index_rangeContext.class);
		}
		public Array_index_rangeContext array_index_range(int i) {
			return getRuleContext(Array_index_rangeContext.class,i);
		}
		public TerminalNode RBRACK() { return getToken(RecafParser.RBRACK, 0); }
		public List<TerminalNode> COMMA() { return getTokens(RecafParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RecafParser.COMMA, i);
		}
		public TerminalNode OF() { return getToken(RecafParser.OF, 0); }
		public Type_refContext type_ref() {
			return getRuleContext(Type_refContext.class,0);
		}
		public Array_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterArray_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitArray_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitArray_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Array_typeContext array_type() throws RecognitionException {
		Array_typeContext _localctx = new Array_typeContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_array_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			match(ARRAY);
			setState(140);
			match(LBRACK);
			setState(141);
			array_index_range();
			setState(146);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(142);
				match(COMMA);
				setState(143);
				array_index_range();
				}
				}
				setState(148);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(149);
			match(RBRACK);
			setState(152);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OF) {
				{
				setState(150);
				match(OF);
				setState(151);
				type_ref();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Array_index_rangeContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RANGE() { return getToken(RecafParser.RANGE, 0); }
		public Array_index_rangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_index_range; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterArray_index_range(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitArray_index_range(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitArray_index_range(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Array_index_rangeContext array_index_range() throws RecognitionException {
		Array_index_rangeContext _localctx = new Array_index_rangeContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_array_index_range);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			expr(0);
			setState(155);
			match(RANGE);
			setState(156);
			expr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Record_typeContext extends ParserRuleContext {
		public TerminalNode RECORD() { return getToken(RecafParser.RECORD, 0); }
		public Var_declsContext var_decls() {
			return getRuleContext(Var_declsContext.class,0);
		}
		public TerminalNode END() { return getToken(RecafParser.END, 0); }
		public Record_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_record_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterRecord_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitRecord_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitRecord_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Record_typeContext record_type() throws RecognitionException {
		Record_typeContext _localctx = new Record_typeContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_record_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			match(RECORD);
			setState(159);
			var_decls();
			setState(160);
			match(END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Enum_typeContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(RecafParser.LPAREN, 0); }
		public Id_listContext id_list() {
			return getRuleContext(Id_listContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(RecafParser.RPAREN, 0); }
		public Enum_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enum_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterEnum_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitEnum_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitEnum_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Enum_typeContext enum_type() throws RecognitionException {
		Enum_typeContext _localctx = new Enum_typeContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_enum_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			match(LPAREN);
			setState(163);
			id_list();
			setState(164);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Pointer_typeContext extends ParserRuleContext {
		public TerminalNode CARET() { return getToken(RecafParser.CARET, 0); }
		public Base_typeContext base_type() {
			return getRuleContext(Base_typeContext.class,0);
		}
		public Pointer_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pointer_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterPointer_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitPointer_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitPointer_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Pointer_typeContext pointer_type() throws RecognitionException {
		Pointer_typeContext _localctx = new Pointer_typeContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_pointer_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(166);
			match(CARET);
			setState(167);
			base_type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Const_sectionContext extends ParserRuleContext {
		public TerminalNode CONST() { return getToken(RecafParser.CONST, 0); }
		public List<Const_declContext> const_decl() {
			return getRuleContexts(Const_declContext.class);
		}
		public Const_declContext const_decl(int i) {
			return getRuleContext(Const_declContext.class,i);
		}
		public List<TerminalNode> SEMICOLON() { return getTokens(RecafParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(RecafParser.SEMICOLON, i);
		}
		public Const_sectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_const_section; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterConst_section(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitConst_section(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitConst_section(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Const_sectionContext const_section() throws RecognitionException {
		Const_sectionContext _localctx = new Const_sectionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_const_section);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			match(CONST);
			setState(175);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ID) {
				{
				{
				setState(170);
				const_decl();
				setState(171);
				match(SEMICOLON);
				}
				}
				setState(177);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Const_declContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(RecafParser.ID, 0); }
		public TerminalNode EQUALS() { return getToken(RecafParser.EQUALS, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Const_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_const_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterConst_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitConst_decl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitConst_decl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Const_declContext const_decl() throws RecognitionException {
		Const_declContext _localctx = new Const_declContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_const_decl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			match(ID);
			setState(179);
			match(EQUALS);
			setState(180);
			expr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Var_sectionContext extends ParserRuleContext {
		public TerminalNode VAR() { return getToken(RecafParser.VAR, 0); }
		public Var_declsContext var_decls() {
			return getRuleContext(Var_declsContext.class,0);
		}
		public Var_sectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_section; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterVar_section(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitVar_section(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitVar_section(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Var_sectionContext var_section() throws RecognitionException {
		Var_sectionContext _localctx = new Var_sectionContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_var_section);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
			match(VAR);
			setState(183);
			var_decls();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Var_declsContext extends ParserRuleContext {
		public List<Var_declContext> var_decl() {
			return getRuleContexts(Var_declContext.class);
		}
		public Var_declContext var_decl(int i) {
			return getRuleContext(Var_declContext.class,i);
		}
		public List<TerminalNode> SEMICOLON() { return getTokens(RecafParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(RecafParser.SEMICOLON, i);
		}
		public Var_declsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_decls; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterVar_decls(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitVar_decls(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitVar_decls(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Var_declsContext var_decls() throws RecognitionException {
		Var_declsContext _localctx = new Var_declsContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_var_decls);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ID) {
				{
				{
				setState(185);
				var_decl();
				setState(186);
				match(SEMICOLON);
				}
				}
				setState(192);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Var_declContext extends ParserRuleContext {
		public Id_listContext id_list() {
			return getRuleContext(Id_listContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RecafParser.COLON, 0); }
		public Type_refContext type_ref() {
			return getRuleContext(Type_refContext.class,0);
		}
		public Var_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterVar_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitVar_decl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitVar_decl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Var_declContext var_decl() throws RecognitionException {
		Var_declContext _localctx = new Var_declContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_var_decl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(193);
			id_list();
			setState(196);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(194);
				match(COLON);
				setState(195);
				type_ref();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Id_listContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(RecafParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(RecafParser.ID, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RecafParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RecafParser.COMMA, i);
		}
		public Id_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterId_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitId_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitId_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Id_listContext id_list() throws RecognitionException {
		Id_listContext _localctx = new Id_listContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_id_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(198);
			match(ID);
			setState(203);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(199);
				match(COMMA);
				setState(200);
				match(ID);
				}
				}
				setState(205);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Function_declContext extends ParserRuleContext {
		public TerminalNode FUNCTION() { return getToken(RecafParser.FUNCTION, 0); }
		public TerminalNode ID() { return getToken(RecafParser.ID, 0); }
		public List<TerminalNode> SEMICOLON() { return getTokens(RecafParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(RecafParser.SEMICOLON, i);
		}
		public TerminalNode EXTERNAL() { return getToken(RecafParser.EXTERNAL, 0); }
		public TerminalNode FORWARD() { return getToken(RecafParser.FORWARD, 0); }
		public Routine_blockContext routine_block() {
			return getRuleContext(Routine_blockContext.class,0);
		}
		public Formal_paramsContext formal_params() {
			return getRuleContext(Formal_paramsContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RecafParser.COLON, 0); }
		public Type_refContext type_ref() {
			return getRuleContext(Type_refContext.class,0);
		}
		public Function_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterFunction_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitFunction_decl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitFunction_decl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Function_declContext function_decl() throws RecognitionException {
		Function_declContext _localctx = new Function_declContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_function_decl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(206);
			match(FUNCTION);
			setState(207);
			match(ID);
			setState(209);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(208);
				formal_params();
				}
			}

			setState(213);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(211);
				match(COLON);
				setState(212);
				type_ref();
				}
			}

			setState(215);
			match(SEMICOLON);
			setState(219);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXTERNAL:
				{
				setState(216);
				match(EXTERNAL);
				}
				break;
			case FORWARD:
				{
				setState(217);
				match(FORWARD);
				}
				break;
			case BEGIN:
			case CONST:
			case TYPE:
			case VAR:
				{
				setState(218);
				routine_block();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(221);
			match(SEMICOLON);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Procedure_declContext extends ParserRuleContext {
		public TerminalNode PROCEDURE() { return getToken(RecafParser.PROCEDURE, 0); }
		public TerminalNode ID() { return getToken(RecafParser.ID, 0); }
		public List<TerminalNode> SEMICOLON() { return getTokens(RecafParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(RecafParser.SEMICOLON, i);
		}
		public TerminalNode EXTERNAL() { return getToken(RecafParser.EXTERNAL, 0); }
		public TerminalNode FORWARD() { return getToken(RecafParser.FORWARD, 0); }
		public Routine_blockContext routine_block() {
			return getRuleContext(Routine_blockContext.class,0);
		}
		public Formal_paramsContext formal_params() {
			return getRuleContext(Formal_paramsContext.class,0);
		}
		public Procedure_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_procedure_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterProcedure_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitProcedure_decl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitProcedure_decl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Procedure_declContext procedure_decl() throws RecognitionException {
		Procedure_declContext _localctx = new Procedure_declContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_procedure_decl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(223);
			match(PROCEDURE);
			setState(224);
			match(ID);
			setState(226);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(225);
				formal_params();
				}
			}

			setState(228);
			match(SEMICOLON);
			setState(232);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXTERNAL:
				{
				setState(229);
				match(EXTERNAL);
				}
				break;
			case FORWARD:
				{
				setState(230);
				match(FORWARD);
				}
				break;
			case BEGIN:
			case CONST:
			case TYPE:
			case VAR:
				{
				setState(231);
				routine_block();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(234);
			match(SEMICOLON);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Formal_paramsContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(RecafParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RecafParser.RPAREN, 0); }
		public TerminalNode ELLIPSIS() { return getToken(RecafParser.ELLIPSIS, 0); }
		public Param_declsContext param_decls() {
			return getRuleContext(Param_declsContext.class,0);
		}
		public Formal_paramsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formal_params; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterFormal_params(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitFormal_params(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitFormal_params(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Formal_paramsContext formal_params() throws RecognitionException {
		Formal_paramsContext _localctx = new Formal_paramsContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_formal_params);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(236);
			match(LPAREN);
			setState(241);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ELLIPSIS:
				{
				setState(237);
				match(ELLIPSIS);
				}
				break;
			case ID:
			case RPAREN:
				{
				setState(239);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(238);
					param_decls();
					}
				}

				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(243);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Param_declsContext extends ParserRuleContext {
		public List<Var_declContext> var_decl() {
			return getRuleContexts(Var_declContext.class);
		}
		public Var_declContext var_decl(int i) {
			return getRuleContext(Var_declContext.class,i);
		}
		public List<TerminalNode> SEMICOLON() { return getTokens(RecafParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(RecafParser.SEMICOLON, i);
		}
		public Param_declsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param_decls; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterParam_decls(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitParam_decls(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitParam_decls(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Param_declsContext param_decls() throws RecognitionException {
		Param_declsContext _localctx = new Param_declsContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_param_decls);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245);
			var_decl();
			setState(250);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMICOLON) {
				{
				{
				setState(246);
				match(SEMICOLON);
				setState(247);
				var_decl();
				}
				}
				setState(252);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Routine_blockContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public List<Local_declarationContext> local_declaration() {
			return getRuleContexts(Local_declarationContext.class);
		}
		public Local_declarationContext local_declaration(int i) {
			return getRuleContext(Local_declarationContext.class,i);
		}
		public Routine_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_routine_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterRoutine_block(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitRoutine_block(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitRoutine_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Routine_blockContext routine_block() throws RecognitionException {
		Routine_blockContext _localctx = new Routine_blockContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_routine_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(256);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 14680064L) != 0)) {
				{
				{
				setState(253);
				local_declaration();
				}
				}
				setState(258);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(259);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Local_declarationContext extends ParserRuleContext {
		public Type_sectionContext type_section() {
			return getRuleContext(Type_sectionContext.class,0);
		}
		public Const_sectionContext const_section() {
			return getRuleContext(Const_sectionContext.class,0);
		}
		public Var_sectionContext var_section() {
			return getRuleContext(Var_sectionContext.class,0);
		}
		public Local_declarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_local_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterLocal_declaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitLocal_declaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitLocal_declaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Local_declarationContext local_declaration() throws RecognitionException {
		Local_declarationContext _localctx = new Local_declarationContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_local_declaration);
		try {
			setState(264);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TYPE:
				enterOuterAlt(_localctx, 1);
				{
				setState(261);
				type_section();
				}
				break;
			case CONST:
				enterOuterAlt(_localctx, 2);
				{
				setState(262);
				const_section();
				}
				break;
			case VAR:
				enterOuterAlt(_localctx, 3);
				{
				setState(263);
				var_section();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Main_blockContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode DOT() { return getToken(RecafParser.DOT, 0); }
		public Main_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_main_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterMain_block(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitMain_block(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitMain_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Main_blockContext main_block() throws RecognitionException {
		Main_blockContext _localctx = new Main_blockContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_main_block);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(266);
			block();
			setState(267);
			match(DOT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockContext extends ParserRuleContext {
		public TerminalNode BEGIN() { return getToken(RecafParser.BEGIN, 0); }
		public TerminalNode END() { return getToken(RecafParser.END, 0); }
		public Statement_listContext statement_list() {
			return getRuleContext(Statement_listContext.class,0);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(269);
			match(BEGIN);
			setState(271);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4295378976L) != 0)) {
				{
				setState(270);
				statement_list();
				}
			}

			setState(273);
			match(END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Statement_listContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public List<TerminalNode> SEMICOLON() { return getTokens(RecafParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(RecafParser.SEMICOLON, i);
		}
		public Statement_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterStatement_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitStatement_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitStatement_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Statement_listContext statement_list() throws RecognitionException {
		Statement_listContext _localctx = new Statement_listContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_statement_list);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(275);
			statement();
			setState(280);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(276);
					match(SEMICOLON);
					setState(277);
					statement();
					}
					} 
				}
				setState(282);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			}
			setState(284);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMICOLON) {
				{
				setState(283);
				match(SEMICOLON);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public Assign_statementContext assign_statement() {
			return getRuleContext(Assign_statementContext.class,0);
		}
		public Method_call_statementContext method_call_statement() {
			return getRuleContext(Method_call_statementContext.class,0);
		}
		public If_elseContext if_else() {
			return getRuleContext(If_elseContext.class,0);
		}
		public For_loopContext for_loop() {
			return getRuleContext(For_loopContext.class,0);
		}
		public While_loopContext while_loop() {
			return getRuleContext(While_loopContext.class,0);
		}
		public Repeat_loopContext repeat_loop() {
			return getRuleContext(Repeat_loopContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_statement);
		try {
			setState(293);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(286);
				assign_statement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(287);
				method_call_statement();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(288);
				if_else();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(289);
				for_loop();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(290);
				while_loop();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(291);
				repeat_loop();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(292);
				block();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Assign_statementContext extends ParserRuleContext {
		public LocationContext location() {
			return getRuleContext(LocationContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(RecafParser.ASSIGN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Assign_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assign_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterAssign_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitAssign_statement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitAssign_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Assign_statementContext assign_statement() throws RecognitionException {
		Assign_statementContext _localctx = new Assign_statementContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_assign_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(295);
			location();
			setState(296);
			match(ASSIGN);
			setState(297);
			expr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Method_call_statementContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(RecafParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(RecafParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RecafParser.RPAREN, 0); }
		public ArgsContext args() {
			return getRuleContext(ArgsContext.class,0);
		}
		public Method_call_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_method_call_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterMethod_call_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitMethod_call_statement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitMethod_call_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Method_call_statementContext method_call_statement() throws RecognitionException {
		Method_call_statementContext _localctx = new Method_call_statementContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_method_call_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(299);
			match(ID);
			setState(305);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(300);
				match(LPAREN);
				setState(302);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 70383776563214L) != 0)) {
					{
					setState(301);
					args();
					}
				}

				setState(304);
				match(RPAREN);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Method_callContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(RecafParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(RecafParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RecafParser.RPAREN, 0); }
		public ArgsContext args() {
			return getRuleContext(ArgsContext.class,0);
		}
		public Method_callContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_method_call; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterMethod_call(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitMethod_call(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitMethod_call(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Method_callContext method_call() throws RecognitionException {
		Method_callContext _localctx = new Method_callContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_method_call);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(307);
			match(ID);
			setState(308);
			match(LPAREN);
			setState(310);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 70383776563214L) != 0)) {
				{
				setState(309);
				args();
				}
			}

			setState(312);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgsContext extends ParserRuleContext {
		public List<ArgContext> arg() {
			return getRuleContexts(ArgContext.class);
		}
		public ArgContext arg(int i) {
			return getRuleContext(ArgContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RecafParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RecafParser.COMMA, i);
		}
		public ArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_args; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterArgs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitArgs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitArgs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgsContext args() throws RecognitionException {
		ArgsContext _localctx = new ArgsContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_args);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(314);
			arg();
			setState(319);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(315);
				match(COMMA);
				setState(316);
				arg();
				}
				}
				setState(321);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode STRING_LITERAL() { return getToken(RecafParser.STRING_LITERAL, 0); }
		public ArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitArg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgContext arg() throws RecognitionException {
		ArgContext _localctx = new ArgContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_arg);
		try {
			setState(324);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOOL_LITERAL:
			case INT_LITERAL:
			case NOT:
			case ID:
			case LPAREN:
			case MINUS:
				enterOuterAlt(_localctx, 1);
				{
				setState(322);
				expr(0);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(323);
				match(STRING_LITERAL);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LocationContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(RecafParser.ID, 0); }
		public List<Location_accessContext> location_access() {
			return getRuleContexts(Location_accessContext.class);
		}
		public Location_accessContext location_access(int i) {
			return getRuleContext(Location_accessContext.class,i);
		}
		public LocationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_location; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterLocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitLocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitLocation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LocationContext location() throws RecognitionException {
		LocationContext _localctx = new LocationContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_location);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			match(ID);
			setState(330);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(327);
					location_access();
					}
					} 
				}
				setState(332);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Location_accessContext extends ParserRuleContext {
		public IndexerContext indexer() {
			return getRuleContext(IndexerContext.class,0);
		}
		public Field_selectorContext field_selector() {
			return getRuleContext(Field_selectorContext.class,0);
		}
		public Deref_selectorContext deref_selector() {
			return getRuleContext(Deref_selectorContext.class,0);
		}
		public Location_accessContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_location_access; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterLocation_access(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitLocation_access(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitLocation_access(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Location_accessContext location_access() throws RecognitionException {
		Location_accessContext _localctx = new Location_accessContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_location_access);
		try {
			setState(336);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACK:
				enterOuterAlt(_localctx, 1);
				{
				setState(333);
				indexer();
				}
				break;
			case DOT:
				enterOuterAlt(_localctx, 2);
				{
				setState(334);
				field_selector();
				}
				break;
			case CARET:
				enterOuterAlt(_localctx, 3);
				{
				setState(335);
				deref_selector();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IndexerContext extends ParserRuleContext {
		public TerminalNode LBRACK() { return getToken(RecafParser.LBRACK, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RBRACK() { return getToken(RecafParser.RBRACK, 0); }
		public List<TerminalNode> COMMA() { return getTokens(RecafParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RecafParser.COMMA, i);
		}
		public IndexerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterIndexer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitIndexer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitIndexer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexerContext indexer() throws RecognitionException {
		IndexerContext _localctx = new IndexerContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_indexer);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(338);
			match(LBRACK);
			setState(339);
			expr(0);
			setState(344);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(340);
				match(COMMA);
				setState(341);
				expr(0);
				}
				}
				setState(346);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(347);
			match(RBRACK);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Field_selectorContext extends ParserRuleContext {
		public TerminalNode DOT() { return getToken(RecafParser.DOT, 0); }
		public TerminalNode ID() { return getToken(RecafParser.ID, 0); }
		public Field_selectorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field_selector; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterField_selector(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitField_selector(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitField_selector(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Field_selectorContext field_selector() throws RecognitionException {
		Field_selectorContext _localctx = new Field_selectorContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_field_selector);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(349);
			match(DOT);
			setState(350);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Deref_selectorContext extends ParserRuleContext {
		public TerminalNode CARET() { return getToken(RecafParser.CARET, 0); }
		public Deref_selectorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deref_selector; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterDeref_selector(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitDeref_selector(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitDeref_selector(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Deref_selectorContext deref_selector() throws RecognitionException {
		Deref_selectorContext _localctx = new Deref_selectorContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_deref_selector);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(352);
			match(CARET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprContext extends ParserRuleContext {
		public LocationContext location() {
			return getRuleContext(LocationContext.class,0);
		}
		public Method_callContext method_call() {
			return getRuleContext(Method_callContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode MINUS() { return getToken(RecafParser.MINUS, 0); }
		public TerminalNode NOT() { return getToken(RecafParser.NOT, 0); }
		public TerminalNode LPAREN() { return getToken(RecafParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RecafParser.RPAREN, 0); }
		public Mul_opContext mul_op() {
			return getRuleContext(Mul_opContext.class,0);
		}
		public Add_opContext add_op() {
			return getRuleContext(Add_opContext.class,0);
		}
		public Rel_opContext rel_op() {
			return getRuleContext(Rel_opContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 76;
		enterRecursionRule(_localctx, 76, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(364);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				setState(355);
				location();
				}
				break;
			case 2:
				{
				setState(356);
				method_call();
				}
				break;
			case 3:
				{
				setState(357);
				literal();
				}
				break;
			case 4:
				{
				setState(358);
				_la = _input.LA(1);
				if ( !(_la==NOT || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(359);
				expr(5);
				}
				break;
			case 5:
				{
				setState(360);
				match(LPAREN);
				setState(361);
				expr(0);
				setState(362);
				match(RPAREN);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(380);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(378);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
					case 1:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(366);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(367);
						mul_op();
						setState(368);
						expr(5);
						}
						break;
					case 2:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(370);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(371);
						add_op();
						setState(372);
						expr(4);
						}
						break;
					case 3:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(374);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(375);
						rel_op();
						setState(376);
						expr(3);
						}
						break;
					}
					} 
				}
				setState(382);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Mul_opContext extends ParserRuleContext {
		public TerminalNode TIMES() { return getToken(RecafParser.TIMES, 0); }
		public TerminalNode DIV() { return getToken(RecafParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(RecafParser.MOD, 0); }
		public TerminalNode AND() { return getToken(RecafParser.AND, 0); }
		public Mul_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mul_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterMul_op(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitMul_op(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitMul_op(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Mul_opContext mul_op() throws RecognitionException {
		Mul_opContext _localctx = new Mul_opContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_mul_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(383);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 140738427879424L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Add_opContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(RecafParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(RecafParser.MINUS, 0); }
		public TerminalNode OR() { return getToken(RecafParser.OR, 0); }
		public Add_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_add_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterAdd_op(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitAdd_op(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitAdd_op(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Add_opContext add_op() throws RecognitionException {
		Add_opContext _localctx = new Add_opContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_add_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(385);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 105554190008320L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Rel_opContext extends ParserRuleContext {
		public TerminalNode EQUALS() { return getToken(RecafParser.EQUALS, 0); }
		public TerminalNode NEQ() { return getToken(RecafParser.NEQ, 0); }
		public TerminalNode LT() { return getToken(RecafParser.LT, 0); }
		public TerminalNode GT() { return getToken(RecafParser.GT, 0); }
		public TerminalNode LEQ() { return getToken(RecafParser.LEQ, 0); }
		public TerminalNode GEQ() { return getToken(RecafParser.GEQ, 0); }
		public Rel_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rel_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterRel_op(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitRel_op(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitRel_op(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Rel_opContext rel_op() throws RecognitionException {
		Rel_opContext _localctx = new Rel_opContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_rel_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(387);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 17732923532771328L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode INT_LITERAL() { return getToken(RecafParser.INT_LITERAL, 0); }
		public TerminalNode BOOL_LITERAL() { return getToken(RecafParser.BOOL_LITERAL, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_literal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(389);
			_la = _input.LA(1);
			if ( !(_la==BOOL_LITERAL || _la==INT_LITERAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class If_elseContext extends ParserRuleContext {
		public TerminalNode IF() { return getToken(RecafParser.IF, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode THEN() { return getToken(RecafParser.THEN, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(RecafParser.ELSE, 0); }
		public If_elseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_if_else; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterIf_else(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitIf_else(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitIf_else(this);
			else return visitor.visitChildren(this);
		}
	}

	public final If_elseContext if_else() throws RecognitionException {
		If_elseContext _localctx = new If_elseContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_if_else);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(391);
			match(IF);
			setState(392);
			expr(0);
			setState(393);
			match(THEN);
			setState(394);
			statement();
			setState(397);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				{
				setState(395);
				match(ELSE);
				setState(396);
				statement();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class For_loopContext extends ParserRuleContext {
		public TerminalNode FOR() { return getToken(RecafParser.FOR, 0); }
		public TerminalNode ID() { return getToken(RecafParser.ID, 0); }
		public TerminalNode ASSIGN() { return getToken(RecafParser.ASSIGN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode DO() { return getToken(RecafParser.DO, 0); }
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode TO() { return getToken(RecafParser.TO, 0); }
		public TerminalNode DOWNTO() { return getToken(RecafParser.DOWNTO, 0); }
		public For_loopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_for_loop; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterFor_loop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitFor_loop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitFor_loop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final For_loopContext for_loop() throws RecognitionException {
		For_loopContext _localctx = new For_loopContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_for_loop);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(399);
			match(FOR);
			setState(400);
			match(ID);
			setState(401);
			match(ASSIGN);
			setState(402);
			expr(0);
			setState(403);
			_la = _input.LA(1);
			if ( !(_la==TO || _la==DOWNTO) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(404);
			expr(0);
			setState(405);
			match(DO);
			setState(406);
			statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class While_loopContext extends ParserRuleContext {
		public TerminalNode WHILE() { return getToken(RecafParser.WHILE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode DO() { return getToken(RecafParser.DO, 0); }
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public While_loopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_while_loop; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterWhile_loop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitWhile_loop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitWhile_loop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final While_loopContext while_loop() throws RecognitionException {
		While_loopContext _localctx = new While_loopContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_while_loop);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(408);
			match(WHILE);
			setState(409);
			expr(0);
			setState(410);
			match(DO);
			setState(411);
			statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Repeat_loopContext extends ParserRuleContext {
		public TerminalNode REPEAT() { return getToken(RecafParser.REPEAT, 0); }
		public Statement_listContext statement_list() {
			return getRuleContext(Statement_listContext.class,0);
		}
		public TerminalNode UNTIL() { return getToken(RecafParser.UNTIL, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Repeat_loopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_repeat_loop; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).enterRepeat_loop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecafParserListener ) ((RecafParserListener)listener).exitRepeat_loop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RecafParserVisitor ) return ((RecafParserVisitor<? extends T>)visitor).visitRepeat_loop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Repeat_loopContext repeat_loop() throws RecognitionException {
		Repeat_loopContext _localctx = new Repeat_loopContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_repeat_loop);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(413);
			match(REPEAT);
			setState(414);
			statement_list();
			setState(415);
			match(UNTIL);
			setState(416);
			expr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 38:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 4);
		case 1:
			return precpred(_ctx, 3);
		case 2:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u00019\u01a3\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"+
		"-\u0007-\u0002.\u0007.\u0001\u0000\u0001\u0000\u0005\u0000a\b\u0000\n"+
		"\u0000\f\u0000d\t\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0003\u0002r\b\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003z\b\u0003"+
		"\n\u0003\f\u0003}\t\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005"+
		"\u0088\b\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0005\u0007\u0091\b\u0007\n\u0007\f\u0007\u0094"+
		"\t\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0099\b\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f"+
		"\u0001\f\u0001\f\u0001\f\u0005\f\u00ae\b\f\n\f\f\f\u00b1\t\f\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0005\u000f\u00bd\b\u000f\n\u000f\f\u000f\u00c0\t\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u00c5\b\u0010\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0005\u0011\u00ca\b\u0011\n\u0011\f\u0011\u00cd"+
		"\t\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u00d2\b\u0012"+
		"\u0001\u0012\u0001\u0012\u0003\u0012\u00d6\b\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0003\u0012\u00dc\b\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u00e3\b\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u00e9\b\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u00f0\b\u0014"+
		"\u0003\u0014\u00f2\b\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0005\u0015\u00f9\b\u0015\n\u0015\f\u0015\u00fc\t\u0015\u0001"+
		"\u0016\u0005\u0016\u00ff\b\u0016\n\u0016\f\u0016\u0102\t\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0109\b\u0017"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0003\u0019"+
		"\u0110\b\u0019\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0001\u001a"+
		"\u0005\u001a\u0117\b\u001a\n\u001a\f\u001a\u011a\t\u001a\u0001\u001a\u0003"+
		"\u001a\u011d\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u0126\b\u001b\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0003"+
		"\u001d\u012f\b\u001d\u0001\u001d\u0003\u001d\u0132\b\u001d\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0003\u001e\u0137\b\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0005\u001f\u013e\b\u001f\n\u001f"+
		"\f\u001f\u0141\t\u001f\u0001 \u0001 \u0003 \u0145\b \u0001!\u0001!\u0005"+
		"!\u0149\b!\n!\f!\u014c\t!\u0001\"\u0001\"\u0001\"\u0003\"\u0151\b\"\u0001"+
		"#\u0001#\u0001#\u0001#\u0005#\u0157\b#\n#\f#\u015a\t#\u0001#\u0001#\u0001"+
		"$\u0001$\u0001$\u0001%\u0001%\u0001&\u0001&\u0001&\u0001&\u0001&\u0001"+
		"&\u0001&\u0001&\u0001&\u0001&\u0003&\u016d\b&\u0001&\u0001&\u0001&\u0001"+
		"&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0005&\u017b"+
		"\b&\n&\f&\u017e\t&\u0001\'\u0001\'\u0001(\u0001(\u0001)\u0001)\u0001*"+
		"\u0001*\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0003+\u018e\b+\u0001"+
		",\u0001,\u0001,\u0001,\u0001,\u0001,\u0001,\u0001,\u0001,\u0001-\u0001"+
		"-\u0001-\u0001-\u0001-\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0000"+
		"\u0001L/\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018"+
		"\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\\u0000\u0006\u0002"+
		"\u0000\u001f\u001f..\u0002\u0000\u001b\u001d//\u0002\u0000\u001e\u001e"+
		"-.\u0001\u000005\u0002\u0000\u0001\u0001\u0003\u0003\u0001\u0000\u000f"+
		"\u0010\u01aa\u0000^\u0001\u0000\u0000\u0000\u0002h\u0001\u0000\u0000\u0000"+
		"\u0004q\u0001\u0000\u0000\u0000\u0006s\u0001\u0000\u0000\u0000\b~\u0001"+
		"\u0000\u0000\u0000\n\u0087\u0001\u0000\u0000\u0000\f\u0089\u0001\u0000"+
		"\u0000\u0000\u000e\u008b\u0001\u0000\u0000\u0000\u0010\u009a\u0001\u0000"+
		"\u0000\u0000\u0012\u009e\u0001\u0000\u0000\u0000\u0014\u00a2\u0001\u0000"+
		"\u0000\u0000\u0016\u00a6\u0001\u0000\u0000\u0000\u0018\u00a9\u0001\u0000"+
		"\u0000\u0000\u001a\u00b2\u0001\u0000\u0000\u0000\u001c\u00b6\u0001\u0000"+
		"\u0000\u0000\u001e\u00be\u0001\u0000\u0000\u0000 \u00c1\u0001\u0000\u0000"+
		"\u0000\"\u00c6\u0001\u0000\u0000\u0000$\u00ce\u0001\u0000\u0000\u0000"+
		"&\u00df\u0001\u0000\u0000\u0000(\u00ec\u0001\u0000\u0000\u0000*\u00f5"+
		"\u0001\u0000\u0000\u0000,\u0100\u0001\u0000\u0000\u0000.\u0108\u0001\u0000"+
		"\u0000\u00000\u010a\u0001\u0000\u0000\u00002\u010d\u0001\u0000\u0000\u0000"+
		"4\u0113\u0001\u0000\u0000\u00006\u0125\u0001\u0000\u0000\u00008\u0127"+
		"\u0001\u0000\u0000\u0000:\u012b\u0001\u0000\u0000\u0000<\u0133\u0001\u0000"+
		"\u0000\u0000>\u013a\u0001\u0000\u0000\u0000@\u0144\u0001\u0000\u0000\u0000"+
		"B\u0146\u0001\u0000\u0000\u0000D\u0150\u0001\u0000\u0000\u0000F\u0152"+
		"\u0001\u0000\u0000\u0000H\u015d\u0001\u0000\u0000\u0000J\u0160\u0001\u0000"+
		"\u0000\u0000L\u016c\u0001\u0000\u0000\u0000N\u017f\u0001\u0000\u0000\u0000"+
		"P\u0181\u0001\u0000\u0000\u0000R\u0183\u0001\u0000\u0000\u0000T\u0185"+
		"\u0001\u0000\u0000\u0000V\u0187\u0001\u0000\u0000\u0000X\u018f\u0001\u0000"+
		"\u0000\u0000Z\u0198\u0001\u0000\u0000\u0000\\\u019d\u0001\u0000\u0000"+
		"\u0000^b\u0003\u0002\u0001\u0000_a\u0003\u0004\u0002\u0000`_\u0001\u0000"+
		"\u0000\u0000ad\u0001\u0000\u0000\u0000b`\u0001\u0000\u0000\u0000bc\u0001"+
		"\u0000\u0000\u0000ce\u0001\u0000\u0000\u0000db\u0001\u0000\u0000\u0000"+
		"ef\u00030\u0018\u0000fg\u0005\u0000\u0000\u0001g\u0001\u0001\u0000\u0000"+
		"\u0000hi\u0005\u0004\u0000\u0000ij\u0005 \u0000\u0000jk\u0005%\u0000\u0000"+
		"k\u0003\u0001\u0000\u0000\u0000lr\u0003\u0006\u0003\u0000mr\u0003\u0018"+
		"\f\u0000nr\u0003\u001c\u000e\u0000or\u0003$\u0012\u0000pr\u0003&\u0013"+
		"\u0000ql\u0001\u0000\u0000\u0000qm\u0001\u0000\u0000\u0000qn\u0001\u0000"+
		"\u0000\u0000qo\u0001\u0000\u0000\u0000qp\u0001\u0000\u0000\u0000r\u0005"+
		"\u0001\u0000\u0000\u0000st\u0005\u0016\u0000\u0000tu\u0003\b\u0004\u0000"+
		"u{\u0005%\u0000\u0000vw\u0003\b\u0004\u0000wx\u0005%\u0000\u0000xz\u0001"+
		"\u0000\u0000\u0000yv\u0001\u0000\u0000\u0000z}\u0001\u0000\u0000\u0000"+
		"{y\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000\u0000|\u0007\u0001\u0000"+
		"\u0000\u0000}{\u0001\u0000\u0000\u0000~\u007f\u0005 \u0000\u0000\u007f"+
		"\u0080\u00050\u0000\u0000\u0080\u0081\u0003\n\u0005\u0000\u0081\t\u0001"+
		"\u0000\u0000\u0000\u0082\u0088\u0003\f\u0006\u0000\u0083\u0088\u0003\u000e"+
		"\u0007\u0000\u0084\u0088\u0003\u0012\t\u0000\u0085\u0088\u0003\u0014\n"+
		"\u0000\u0086\u0088\u0003\u0016\u000b\u0000\u0087\u0082\u0001\u0000\u0000"+
		"\u0000\u0087\u0083\u0001\u0000\u0000\u0000\u0087\u0084\u0001\u0000\u0000"+
		"\u0000\u0087\u0085\u0001\u0000\u0000\u0000\u0087\u0086\u0001\u0000\u0000"+
		"\u0000\u0088\u000b\u0001\u0000\u0000\u0000\u0089\u008a\u0005 \u0000\u0000"+
		"\u008a\r\u0001\u0000\u0000\u0000\u008b\u008c\u0005\u0018\u0000\u0000\u008c"+
		"\u008d\u0005#\u0000\u0000\u008d\u0092\u0003\u0010\b\u0000\u008e\u008f"+
		"\u0005(\u0000\u0000\u008f\u0091\u0003\u0010\b\u0000\u0090\u008e\u0001"+
		"\u0000\u0000\u0000\u0091\u0094\u0001\u0000\u0000\u0000\u0092\u0090\u0001"+
		"\u0000\u0000\u0000\u0092\u0093\u0001\u0000\u0000\u0000\u0093\u0095\u0001"+
		"\u0000\u0000\u0000\u0094\u0092\u0001\u0000\u0000\u0000\u0095\u0098\u0005"+
		"$\u0000\u0000\u0096\u0097\u0005\u001a\u0000\u0000\u0097\u0099\u0003\n"+
		"\u0005\u0000\u0098\u0096\u0001\u0000\u0000\u0000\u0098\u0099\u0001\u0000"+
		"\u0000\u0000\u0099\u000f\u0001\u0000\u0000\u0000\u009a\u009b\u0003L&\u0000"+
		"\u009b\u009c\u0005*\u0000\u0000\u009c\u009d\u0003L&\u0000\u009d\u0011"+
		"\u0001\u0000\u0000\u0000\u009e\u009f\u0005\u0019\u0000\u0000\u009f\u00a0"+
		"\u0003\u001e\u000f\u0000\u00a0\u00a1\u0005\u0006\u0000\u0000\u00a1\u0013"+
		"\u0001\u0000\u0000\u0000\u00a2\u00a3\u0005!\u0000\u0000\u00a3\u00a4\u0003"+
		"\"\u0011\u0000\u00a4\u00a5\u0005\"\u0000\u0000\u00a5\u0015\u0001\u0000"+
		"\u0000\u0000\u00a6\u00a7\u0005,\u0000\u0000\u00a7\u00a8\u0003\f\u0006"+
		"\u0000\u00a8\u0017\u0001\u0000\u0000\u0000\u00a9\u00af\u0005\u0015\u0000"+
		"\u0000\u00aa\u00ab\u0003\u001a\r\u0000\u00ab\u00ac\u0005%\u0000\u0000"+
		"\u00ac\u00ae\u0001\u0000\u0000\u0000\u00ad\u00aa\u0001\u0000\u0000\u0000"+
		"\u00ae\u00b1\u0001\u0000\u0000\u0000\u00af\u00ad\u0001\u0000\u0000\u0000"+
		"\u00af\u00b0\u0001\u0000\u0000\u0000\u00b0\u0019\u0001\u0000\u0000\u0000"+
		"\u00b1\u00af\u0001\u0000\u0000\u0000\u00b2\u00b3\u0005 \u0000\u0000\u00b3"+
		"\u00b4\u00050\u0000\u0000\u00b4\u00b5\u0003L&\u0000\u00b5\u001b\u0001"+
		"\u0000\u0000\u0000\u00b6\u00b7\u0005\u0017\u0000\u0000\u00b7\u00b8\u0003"+
		"\u001e\u000f\u0000\u00b8\u001d\u0001\u0000\u0000\u0000\u00b9\u00ba\u0003"+
		" \u0010\u0000\u00ba\u00bb\u0005%\u0000\u0000\u00bb\u00bd\u0001\u0000\u0000"+
		"\u0000\u00bc\u00b9\u0001\u0000\u0000\u0000\u00bd\u00c0\u0001\u0000\u0000"+
		"\u0000\u00be\u00bc\u0001\u0000\u0000\u0000\u00be\u00bf\u0001\u0000\u0000"+
		"\u0000\u00bf\u001f\u0001\u0000\u0000\u0000\u00c0\u00be\u0001\u0000\u0000"+
		"\u0000\u00c1\u00c4\u0003\"\u0011\u0000\u00c2\u00c3\u0005\'\u0000\u0000"+
		"\u00c3\u00c5\u0003\n\u0005\u0000\u00c4\u00c2\u0001\u0000\u0000\u0000\u00c4"+
		"\u00c5\u0001\u0000\u0000\u0000\u00c5!\u0001\u0000\u0000\u0000\u00c6\u00cb"+
		"\u0005 \u0000\u0000\u00c7\u00c8\u0005(\u0000\u0000\u00c8\u00ca\u0005 "+
		"\u0000\u0000\u00c9\u00c7\u0001\u0000\u0000\u0000\u00ca\u00cd\u0001\u0000"+
		"\u0000\u0000\u00cb\u00c9\u0001\u0000\u0000\u0000\u00cb\u00cc\u0001\u0000"+
		"\u0000\u0000\u00cc#\u0001\u0000\u0000\u0000\u00cd\u00cb\u0001\u0000\u0000"+
		"\u0000\u00ce\u00cf\u0005\u0007\u0000\u0000\u00cf\u00d1\u0005 \u0000\u0000"+
		"\u00d0\u00d2\u0003(\u0014\u0000\u00d1\u00d0\u0001\u0000\u0000\u0000\u00d1"+
		"\u00d2\u0001\u0000\u0000\u0000\u00d2\u00d5\u0001\u0000\u0000\u0000\u00d3"+
		"\u00d4\u0005\'\u0000\u0000\u00d4\u00d6\u0003\n\u0005\u0000\u00d5\u00d3"+
		"\u0001\u0000\u0000\u0000\u00d5\u00d6\u0001\u0000\u0000\u0000\u00d6\u00d7"+
		"\u0001\u0000\u0000\u0000\u00d7\u00db\u0005%\u0000\u0000\u00d8\u00dc\u0005"+
		"\t\u0000\u0000\u00d9\u00dc\u0005\n\u0000\u0000\u00da\u00dc\u0003,\u0016"+
		"\u0000\u00db\u00d8\u0001\u0000\u0000\u0000\u00db\u00d9\u0001\u0000\u0000"+
		"\u0000\u00db\u00da\u0001\u0000\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000"+
		"\u0000\u00dd\u00de\u0005%\u0000\u0000\u00de%\u0001\u0000\u0000\u0000\u00df"+
		"\u00e0\u0005\b\u0000\u0000\u00e0\u00e2\u0005 \u0000\u0000\u00e1\u00e3"+
		"\u0003(\u0014\u0000\u00e2\u00e1\u0001\u0000\u0000\u0000\u00e2\u00e3\u0001"+
		"\u0000\u0000\u0000\u00e3\u00e4\u0001\u0000\u0000\u0000\u00e4\u00e8\u0005"+
		"%\u0000\u0000\u00e5\u00e9\u0005\t\u0000\u0000\u00e6\u00e9\u0005\n\u0000"+
		"\u0000\u00e7\u00e9\u0003,\u0016\u0000\u00e8\u00e5\u0001\u0000\u0000\u0000"+
		"\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e8\u00e7\u0001\u0000\u0000\u0000"+
		"\u00e9\u00ea\u0001\u0000\u0000\u0000\u00ea\u00eb\u0005%\u0000\u0000\u00eb"+
		"\'\u0001\u0000\u0000\u0000\u00ec\u00f1\u0005!\u0000\u0000\u00ed\u00f2"+
		"\u0005)\u0000\u0000\u00ee\u00f0\u0003*\u0015\u0000\u00ef\u00ee\u0001\u0000"+
		"\u0000\u0000\u00ef\u00f0\u0001\u0000\u0000\u0000\u00f0\u00f2\u0001\u0000"+
		"\u0000\u0000\u00f1\u00ed\u0001\u0000\u0000\u0000\u00f1\u00ef\u0001\u0000"+
		"\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000\u00f3\u00f4\u0005\"\u0000"+
		"\u0000\u00f4)\u0001\u0000\u0000\u0000\u00f5\u00fa\u0003 \u0010\u0000\u00f6"+
		"\u00f7\u0005%\u0000\u0000\u00f7\u00f9\u0003 \u0010\u0000\u00f8\u00f6\u0001"+
		"\u0000\u0000\u0000\u00f9\u00fc\u0001\u0000\u0000\u0000\u00fa\u00f8\u0001"+
		"\u0000\u0000\u0000\u00fa\u00fb\u0001\u0000\u0000\u0000\u00fb+\u0001\u0000"+
		"\u0000\u0000\u00fc\u00fa\u0001\u0000\u0000\u0000\u00fd\u00ff\u0003.\u0017"+
		"\u0000\u00fe\u00fd\u0001\u0000\u0000\u0000\u00ff\u0102\u0001\u0000\u0000"+
		"\u0000\u0100\u00fe\u0001\u0000\u0000\u0000\u0100\u0101\u0001\u0000\u0000"+
		"\u0000\u0101\u0103\u0001\u0000\u0000\u0000\u0102\u0100\u0001\u0000\u0000"+
		"\u0000\u0103\u0104\u00032\u0019\u0000\u0104-\u0001\u0000\u0000\u0000\u0105"+
		"\u0109\u0003\u0006\u0003\u0000\u0106\u0109\u0003\u0018\f\u0000\u0107\u0109"+
		"\u0003\u001c\u000e\u0000\u0108\u0105\u0001\u0000\u0000\u0000\u0108\u0106"+
		"\u0001\u0000\u0000\u0000\u0108\u0107\u0001\u0000\u0000\u0000\u0109/\u0001"+
		"\u0000\u0000\u0000\u010a\u010b\u00032\u0019\u0000\u010b\u010c\u0005+\u0000"+
		"\u0000\u010c1\u0001\u0000\u0000\u0000\u010d\u010f\u0005\u0005\u0000\u0000"+
		"\u010e\u0110\u00034\u001a\u0000\u010f\u010e\u0001\u0000\u0000\u0000\u010f"+
		"\u0110\u0001\u0000\u0000\u0000\u0110\u0111\u0001\u0000\u0000\u0000\u0111"+
		"\u0112\u0005\u0006\u0000\u0000\u01123\u0001\u0000\u0000\u0000\u0113\u0118"+
		"\u00036\u001b\u0000\u0114\u0115\u0005%\u0000\u0000\u0115\u0117\u00036"+
		"\u001b\u0000\u0116\u0114\u0001\u0000\u0000\u0000\u0117\u011a\u0001\u0000"+
		"\u0000\u0000\u0118\u0116\u0001\u0000\u0000\u0000\u0118\u0119\u0001\u0000"+
		"\u0000\u0000\u0119\u011c\u0001\u0000\u0000\u0000\u011a\u0118\u0001\u0000"+
		"\u0000\u0000\u011b\u011d\u0005%\u0000\u0000\u011c\u011b\u0001\u0000\u0000"+
		"\u0000\u011c\u011d\u0001\u0000\u0000\u0000\u011d5\u0001\u0000\u0000\u0000"+
		"\u011e\u0126\u00038\u001c\u0000\u011f\u0126\u0003:\u001d\u0000\u0120\u0126"+
		"\u0003V+\u0000\u0121\u0126\u0003X,\u0000\u0122\u0126\u0003Z-\u0000\u0123"+
		"\u0126\u0003\\.\u0000\u0124\u0126\u00032\u0019\u0000\u0125\u011e\u0001"+
		"\u0000\u0000\u0000\u0125\u011f\u0001\u0000\u0000\u0000\u0125\u0120\u0001"+
		"\u0000\u0000\u0000\u0125\u0121\u0001\u0000\u0000\u0000\u0125\u0122\u0001"+
		"\u0000\u0000\u0000\u0125\u0123\u0001\u0000\u0000\u0000\u0125\u0124\u0001"+
		"\u0000\u0000\u0000\u01267\u0001\u0000\u0000\u0000\u0127\u0128\u0003B!"+
		"\u0000\u0128\u0129\u0005&\u0000\u0000\u0129\u012a\u0003L&\u0000\u012a"+
		"9\u0001\u0000\u0000\u0000\u012b\u0131\u0005 \u0000\u0000\u012c\u012e\u0005"+
		"!\u0000\u0000\u012d\u012f\u0003>\u001f\u0000\u012e\u012d\u0001\u0000\u0000"+
		"\u0000\u012e\u012f\u0001\u0000\u0000\u0000\u012f\u0130\u0001\u0000\u0000"+
		"\u0000\u0130\u0132\u0005\"\u0000\u0000\u0131\u012c\u0001\u0000\u0000\u0000"+
		"\u0131\u0132\u0001\u0000\u0000\u0000\u0132;\u0001\u0000\u0000\u0000\u0133"+
		"\u0134\u0005 \u0000\u0000\u0134\u0136\u0005!\u0000\u0000\u0135\u0137\u0003"+
		">\u001f\u0000\u0136\u0135\u0001\u0000\u0000\u0000\u0136\u0137\u0001\u0000"+
		"\u0000\u0000\u0137\u0138\u0001\u0000\u0000\u0000\u0138\u0139\u0005\"\u0000"+
		"\u0000\u0139=\u0001\u0000\u0000\u0000\u013a\u013f\u0003@ \u0000\u013b"+
		"\u013c\u0005(\u0000\u0000\u013c\u013e\u0003@ \u0000\u013d\u013b\u0001"+
		"\u0000\u0000\u0000\u013e\u0141\u0001\u0000\u0000\u0000\u013f\u013d\u0001"+
		"\u0000\u0000\u0000\u013f\u0140\u0001\u0000\u0000\u0000\u0140?\u0001\u0000"+
		"\u0000\u0000\u0141\u013f\u0001\u0000\u0000\u0000\u0142\u0145\u0003L&\u0000"+
		"\u0143\u0145\u0005\u0002\u0000\u0000\u0144\u0142\u0001\u0000\u0000\u0000"+
		"\u0144\u0143\u0001\u0000\u0000\u0000\u0145A\u0001\u0000\u0000\u0000\u0146"+
		"\u014a\u0005 \u0000\u0000\u0147\u0149\u0003D\"\u0000\u0148\u0147\u0001"+
		"\u0000\u0000\u0000\u0149\u014c\u0001\u0000\u0000\u0000\u014a\u0148\u0001"+
		"\u0000\u0000\u0000\u014a\u014b\u0001\u0000\u0000\u0000\u014bC\u0001\u0000"+
		"\u0000\u0000\u014c\u014a\u0001\u0000\u0000\u0000\u014d\u0151\u0003F#\u0000"+
		"\u014e\u0151\u0003H$\u0000\u014f\u0151\u0003J%\u0000\u0150\u014d\u0001"+
		"\u0000\u0000\u0000\u0150\u014e\u0001\u0000\u0000\u0000\u0150\u014f\u0001"+
		"\u0000\u0000\u0000\u0151E\u0001\u0000\u0000\u0000\u0152\u0153\u0005#\u0000"+
		"\u0000\u0153\u0158\u0003L&\u0000\u0154\u0155\u0005(\u0000\u0000\u0155"+
		"\u0157\u0003L&\u0000\u0156\u0154\u0001\u0000\u0000\u0000\u0157\u015a\u0001"+
		"\u0000\u0000\u0000\u0158\u0156\u0001\u0000\u0000\u0000\u0158\u0159\u0001"+
		"\u0000\u0000\u0000\u0159\u015b\u0001\u0000\u0000\u0000\u015a\u0158\u0001"+
		"\u0000\u0000\u0000\u015b\u015c\u0005$\u0000\u0000\u015cG\u0001\u0000\u0000"+
		"\u0000\u015d\u015e\u0005+\u0000\u0000\u015e\u015f\u0005 \u0000\u0000\u015f"+
		"I\u0001\u0000\u0000\u0000\u0160\u0161\u0005,\u0000\u0000\u0161K\u0001"+
		"\u0000\u0000\u0000\u0162\u0163\u0006&\uffff\uffff\u0000\u0163\u016d\u0003"+
		"B!\u0000\u0164\u016d\u0003<\u001e\u0000\u0165\u016d\u0003T*\u0000\u0166"+
		"\u0167\u0007\u0000\u0000\u0000\u0167\u016d\u0003L&\u0005\u0168\u0169\u0005"+
		"!\u0000\u0000\u0169\u016a\u0003L&\u0000\u016a\u016b\u0005\"\u0000\u0000"+
		"\u016b\u016d\u0001\u0000\u0000\u0000\u016c\u0162\u0001\u0000\u0000\u0000"+
		"\u016c\u0164\u0001\u0000\u0000\u0000\u016c\u0165\u0001\u0000\u0000\u0000"+
		"\u016c\u0166\u0001\u0000\u0000\u0000\u016c\u0168\u0001\u0000\u0000\u0000"+
		"\u016d\u017c\u0001\u0000\u0000\u0000\u016e\u016f\n\u0004\u0000\u0000\u016f"+
		"\u0170\u0003N\'\u0000\u0170\u0171\u0003L&\u0005\u0171\u017b\u0001\u0000"+
		"\u0000\u0000\u0172\u0173\n\u0003\u0000\u0000\u0173\u0174\u0003P(\u0000"+
		"\u0174\u0175\u0003L&\u0004\u0175\u017b\u0001\u0000\u0000\u0000\u0176\u0177"+
		"\n\u0002\u0000\u0000\u0177\u0178\u0003R)\u0000\u0178\u0179\u0003L&\u0003"+
		"\u0179\u017b\u0001\u0000\u0000\u0000\u017a\u016e\u0001\u0000\u0000\u0000"+
		"\u017a\u0172\u0001\u0000\u0000\u0000\u017a\u0176\u0001\u0000\u0000\u0000"+
		"\u017b\u017e\u0001\u0000\u0000\u0000\u017c\u017a\u0001\u0000\u0000\u0000"+
		"\u017c\u017d\u0001\u0000\u0000\u0000\u017dM\u0001\u0000\u0000\u0000\u017e"+
		"\u017c\u0001\u0000\u0000\u0000\u017f\u0180\u0007\u0001\u0000\u0000\u0180"+
		"O\u0001\u0000\u0000\u0000\u0181\u0182\u0007\u0002\u0000\u0000\u0182Q\u0001"+
		"\u0000\u0000\u0000\u0183\u0184\u0007\u0003\u0000\u0000\u0184S\u0001\u0000"+
		"\u0000\u0000\u0185\u0186\u0007\u0004\u0000\u0000\u0186U\u0001\u0000\u0000"+
		"\u0000\u0187\u0188\u0005\u000b\u0000\u0000\u0188\u0189\u0003L&\u0000\u0189"+
		"\u018a\u0005\f\u0000\u0000\u018a\u018d\u00036\u001b\u0000\u018b\u018c"+
		"\u0005\r\u0000\u0000\u018c\u018e\u00036\u001b\u0000\u018d\u018b\u0001"+
		"\u0000\u0000\u0000\u018d\u018e\u0001\u0000\u0000\u0000\u018eW\u0001\u0000"+
		"\u0000\u0000\u018f\u0190\u0005\u000e\u0000\u0000\u0190\u0191\u0005 \u0000"+
		"\u0000\u0191\u0192\u0005&\u0000\u0000\u0192\u0193\u0003L&\u0000\u0193"+
		"\u0194\u0007\u0005\u0000\u0000\u0194\u0195\u0003L&\u0000\u0195\u0196\u0005"+
		"\u0014\u0000\u0000\u0196\u0197\u00036\u001b\u0000\u0197Y\u0001\u0000\u0000"+
		"\u0000\u0198\u0199\u0005\u0011\u0000\u0000\u0199\u019a\u0003L&\u0000\u019a"+
		"\u019b\u0005\u0014\u0000\u0000\u019b\u019c\u00036\u001b\u0000\u019c[\u0001"+
		"\u0000\u0000\u0000\u019d\u019e\u0005\u0012\u0000\u0000\u019e\u019f\u0003"+
		"4\u001a\u0000\u019f\u01a0\u0005\u0013\u0000\u0000\u01a0\u01a1\u0003L&"+
		"\u0000\u01a1]\u0001\u0000\u0000\u0000$bq{\u0087\u0092\u0098\u00af\u00be"+
		"\u00c4\u00cb\u00d1\u00d5\u00db\u00e2\u00e8\u00ef\u00f1\u00fa\u0100\u0108"+
		"\u010f\u0118\u011c\u0125\u012e\u0131\u0136\u013f\u0144\u014a\u0150\u0158"+
		"\u016c\u017a\u017c\u018d";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}