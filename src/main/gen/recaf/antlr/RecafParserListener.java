// Generated from RecafParser.g4 by ANTLR 4.13.2
package recaf.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link RecafParser}.
 */
public interface RecafParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link RecafParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(RecafParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(RecafParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#program_decl}.
	 * @param ctx the parse tree
	 */
	void enterProgram_decl(RecafParser.Program_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#program_decl}.
	 * @param ctx the parse tree
	 */
	void exitProgram_decl(RecafParser.Program_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(RecafParser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(RecafParser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#type_section}.
	 * @param ctx the parse tree
	 */
	void enterType_section(RecafParser.Type_sectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#type_section}.
	 * @param ctx the parse tree
	 */
	void exitType_section(RecafParser.Type_sectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterType_decl(RecafParser.Type_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitType_decl(RecafParser.Type_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#type_ref}.
	 * @param ctx the parse tree
	 */
	void enterType_ref(RecafParser.Type_refContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#type_ref}.
	 * @param ctx the parse tree
	 */
	void exitType_ref(RecafParser.Type_refContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#base_type}.
	 * @param ctx the parse tree
	 */
	void enterBase_type(RecafParser.Base_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#base_type}.
	 * @param ctx the parse tree
	 */
	void exitBase_type(RecafParser.Base_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#array_type}.
	 * @param ctx the parse tree
	 */
	void enterArray_type(RecafParser.Array_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#array_type}.
	 * @param ctx the parse tree
	 */
	void exitArray_type(RecafParser.Array_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#array_index_range}.
	 * @param ctx the parse tree
	 */
	void enterArray_index_range(RecafParser.Array_index_rangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#array_index_range}.
	 * @param ctx the parse tree
	 */
	void exitArray_index_range(RecafParser.Array_index_rangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#record_type}.
	 * @param ctx the parse tree
	 */
	void enterRecord_type(RecafParser.Record_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#record_type}.
	 * @param ctx the parse tree
	 */
	void exitRecord_type(RecafParser.Record_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#enum_type}.
	 * @param ctx the parse tree
	 */
	void enterEnum_type(RecafParser.Enum_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#enum_type}.
	 * @param ctx the parse tree
	 */
	void exitEnum_type(RecafParser.Enum_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#pointer_type}.
	 * @param ctx the parse tree
	 */
	void enterPointer_type(RecafParser.Pointer_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#pointer_type}.
	 * @param ctx the parse tree
	 */
	void exitPointer_type(RecafParser.Pointer_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#const_section}.
	 * @param ctx the parse tree
	 */
	void enterConst_section(RecafParser.Const_sectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#const_section}.
	 * @param ctx the parse tree
	 */
	void exitConst_section(RecafParser.Const_sectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#const_decl}.
	 * @param ctx the parse tree
	 */
	void enterConst_decl(RecafParser.Const_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#const_decl}.
	 * @param ctx the parse tree
	 */
	void exitConst_decl(RecafParser.Const_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#var_section}.
	 * @param ctx the parse tree
	 */
	void enterVar_section(RecafParser.Var_sectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#var_section}.
	 * @param ctx the parse tree
	 */
	void exitVar_section(RecafParser.Var_sectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#var_decls}.
	 * @param ctx the parse tree
	 */
	void enterVar_decls(RecafParser.Var_declsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#var_decls}.
	 * @param ctx the parse tree
	 */
	void exitVar_decls(RecafParser.Var_declsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl(RecafParser.Var_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl(RecafParser.Var_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#id_list}.
	 * @param ctx the parse tree
	 */
	void enterId_list(RecafParser.Id_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#id_list}.
	 * @param ctx the parse tree
	 */
	void exitId_list(RecafParser.Id_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#function_decl}.
	 * @param ctx the parse tree
	 */
	void enterFunction_decl(RecafParser.Function_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#function_decl}.
	 * @param ctx the parse tree
	 */
	void exitFunction_decl(RecafParser.Function_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#procedure_decl}.
	 * @param ctx the parse tree
	 */
	void enterProcedure_decl(RecafParser.Procedure_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#procedure_decl}.
	 * @param ctx the parse tree
	 */
	void exitProcedure_decl(RecafParser.Procedure_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#formal_params}.
	 * @param ctx the parse tree
	 */
	void enterFormal_params(RecafParser.Formal_paramsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#formal_params}.
	 * @param ctx the parse tree
	 */
	void exitFormal_params(RecafParser.Formal_paramsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#param_decls}.
	 * @param ctx the parse tree
	 */
	void enterParam_decls(RecafParser.Param_declsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#param_decls}.
	 * @param ctx the parse tree
	 */
	void exitParam_decls(RecafParser.Param_declsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#routine_block}.
	 * @param ctx the parse tree
	 */
	void enterRoutine_block(RecafParser.Routine_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#routine_block}.
	 * @param ctx the parse tree
	 */
	void exitRoutine_block(RecafParser.Routine_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#local_declaration}.
	 * @param ctx the parse tree
	 */
	void enterLocal_declaration(RecafParser.Local_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#local_declaration}.
	 * @param ctx the parse tree
	 */
	void exitLocal_declaration(RecafParser.Local_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#main_block}.
	 * @param ctx the parse tree
	 */
	void enterMain_block(RecafParser.Main_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#main_block}.
	 * @param ctx the parse tree
	 */
	void exitMain_block(RecafParser.Main_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(RecafParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(RecafParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#statement_list}.
	 * @param ctx the parse tree
	 */
	void enterStatement_list(RecafParser.Statement_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#statement_list}.
	 * @param ctx the parse tree
	 */
	void exitStatement_list(RecafParser.Statement_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(RecafParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(RecafParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#assign_statement}.
	 * @param ctx the parse tree
	 */
	void enterAssign_statement(RecafParser.Assign_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#assign_statement}.
	 * @param ctx the parse tree
	 */
	void exitAssign_statement(RecafParser.Assign_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#method_call_statement}.
	 * @param ctx the parse tree
	 */
	void enterMethod_call_statement(RecafParser.Method_call_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#method_call_statement}.
	 * @param ctx the parse tree
	 */
	void exitMethod_call_statement(RecafParser.Method_call_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#method_call}.
	 * @param ctx the parse tree
	 */
	void enterMethod_call(RecafParser.Method_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#method_call}.
	 * @param ctx the parse tree
	 */
	void exitMethod_call(RecafParser.Method_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#args}.
	 * @param ctx the parse tree
	 */
	void enterArgs(RecafParser.ArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#args}.
	 * @param ctx the parse tree
	 */
	void exitArgs(RecafParser.ArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#arg}.
	 * @param ctx the parse tree
	 */
	void enterArg(RecafParser.ArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#arg}.
	 * @param ctx the parse tree
	 */
	void exitArg(RecafParser.ArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#location}.
	 * @param ctx the parse tree
	 */
	void enterLocation(RecafParser.LocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#location}.
	 * @param ctx the parse tree
	 */
	void exitLocation(RecafParser.LocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#location_access}.
	 * @param ctx the parse tree
	 */
	void enterLocation_access(RecafParser.Location_accessContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#location_access}.
	 * @param ctx the parse tree
	 */
	void exitLocation_access(RecafParser.Location_accessContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#indexer}.
	 * @param ctx the parse tree
	 */
	void enterIndexer(RecafParser.IndexerContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#indexer}.
	 * @param ctx the parse tree
	 */
	void exitIndexer(RecafParser.IndexerContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#field_selector}.
	 * @param ctx the parse tree
	 */
	void enterField_selector(RecafParser.Field_selectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#field_selector}.
	 * @param ctx the parse tree
	 */
	void exitField_selector(RecafParser.Field_selectorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#deref_selector}.
	 * @param ctx the parse tree
	 */
	void enterDeref_selector(RecafParser.Deref_selectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#deref_selector}.
	 * @param ctx the parse tree
	 */
	void exitDeref_selector(RecafParser.Deref_selectorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(RecafParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(RecafParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#mul_op}.
	 * @param ctx the parse tree
	 */
	void enterMul_op(RecafParser.Mul_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#mul_op}.
	 * @param ctx the parse tree
	 */
	void exitMul_op(RecafParser.Mul_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#add_op}.
	 * @param ctx the parse tree
	 */
	void enterAdd_op(RecafParser.Add_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#add_op}.
	 * @param ctx the parse tree
	 */
	void exitAdd_op(RecafParser.Add_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#rel_op}.
	 * @param ctx the parse tree
	 */
	void enterRel_op(RecafParser.Rel_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#rel_op}.
	 * @param ctx the parse tree
	 */
	void exitRel_op(RecafParser.Rel_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(RecafParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(RecafParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#if_else}.
	 * @param ctx the parse tree
	 */
	void enterIf_else(RecafParser.If_elseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#if_else}.
	 * @param ctx the parse tree
	 */
	void exitIf_else(RecafParser.If_elseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#for_loop}.
	 * @param ctx the parse tree
	 */
	void enterFor_loop(RecafParser.For_loopContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#for_loop}.
	 * @param ctx the parse tree
	 */
	void exitFor_loop(RecafParser.For_loopContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#while_loop}.
	 * @param ctx the parse tree
	 */
	void enterWhile_loop(RecafParser.While_loopContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#while_loop}.
	 * @param ctx the parse tree
	 */
	void exitWhile_loop(RecafParser.While_loopContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecafParser#repeat_loop}.
	 * @param ctx the parse tree
	 */
	void enterRepeat_loop(RecafParser.Repeat_loopContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecafParser#repeat_loop}.
	 * @param ctx the parse tree
	 */
	void exitRepeat_loop(RecafParser.Repeat_loopContext ctx);
}