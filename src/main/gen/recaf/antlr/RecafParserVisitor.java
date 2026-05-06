// Generated from RecafParser.g4 by ANTLR 4.13.2
package recaf.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link RecafParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface RecafParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link RecafParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(RecafParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#program_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram_decl(RecafParser.Program_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration(RecafParser.DeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#type_section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_section(RecafParser.Type_sectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_decl(RecafParser.Type_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#type_ref}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_ref(RecafParser.Type_refContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#base_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBase_type(RecafParser.Base_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#array_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_type(RecafParser.Array_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#array_index_range}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_index_range(RecafParser.Array_index_rangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#record_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecord_type(RecafParser.Record_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#enum_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnum_type(RecafParser.Enum_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#pointer_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPointer_type(RecafParser.Pointer_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#const_section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConst_section(RecafParser.Const_sectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#const_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConst_decl(RecafParser.Const_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#var_section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_section(RecafParser.Var_sectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#var_decls}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decls(RecafParser.Var_declsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl(RecafParser.Var_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#id_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_list(RecafParser.Id_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#function_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_decl(RecafParser.Function_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#procedure_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedure_decl(RecafParser.Procedure_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#formal_params}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormal_params(RecafParser.Formal_paramsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#param_decls}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_decls(RecafParser.Param_declsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#routine_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutine_block(RecafParser.Routine_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#local_declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocal_declaration(RecafParser.Local_declarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#main_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMain_block(RecafParser.Main_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(RecafParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#statement_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement_list(RecafParser.Statement_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(RecafParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#assign_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssign_statement(RecafParser.Assign_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#method_call_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_call_statement(RecafParser.Method_call_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#method_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_call(RecafParser.Method_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgs(RecafParser.ArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArg(RecafParser.ArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#location}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocation(RecafParser.LocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#location_access}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocation_access(RecafParser.Location_accessContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#indexer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexer(RecafParser.IndexerContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#field_selector}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField_selector(RecafParser.Field_selectorContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#deref_selector}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeref_selector(RecafParser.Deref_selectorContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(RecafParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#mul_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMul_op(RecafParser.Mul_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#add_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd_op(RecafParser.Add_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#rel_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRel_op(RecafParser.Rel_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(RecafParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#if_else}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_else(RecafParser.If_elseContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#for_loop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_loop(RecafParser.For_loopContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#while_loop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile_loop(RecafParser.While_loopContext ctx);
	/**
	 * Visit a parse tree produced by {@link RecafParser#repeat_loop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepeat_loop(RecafParser.Repeat_loopContext ctx);
}