package recaf.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import recaf.antlr.RecafParser;
import recaf.ast.nodes.*;
import recaf.general.*;
import recaf.main.RecafErrorHandler;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static recaf.ast.ASTUtils.*;

public class ASTBuilder {

    private final ASTInvariant inv;

    public ASTBuilder(String file, RecafErrorHandler errorHandler, int optLevel) {
        inv = new ASTInvariant(file, errorHandler, optLevel);
    }

    public ASTProgram visit(RecafParser.ProgramContext cst) {
        ASTContext ctx = ctx(cst);
        return new ASTProgram(ctx,
                visitIdentifier(cst.program_decl().ID()),
                Stream.concat(
                        Stream.concat(registerInternals(ctx),
                                cst.declaration().stream().flatMap(this::visit)),
                        Stream.of(visit(cst.main_block()))).toList());
    }

    private Stream<ASTDeclaration> registerInternals(ASTContext ctx) {
        ASTPrimitiveType intType = new ASTPrimitiveType(ctx, Type.INT);
        ASTPrimitiveType longType = new ASTPrimitiveType(ctx, Type.LONG);
        ASTPrimitiveType boolType = new ASTPrimitiveType(ctx, Type.BOOL);
        ASTPrimitiveType strType = new ASTPrimitiveType(ctx, Type.STRING);
        ASTPrimitiveType unkType = new ASTPrimitiveType(ctx, Type.UNKNOWN);
        return Stream.of(
                new ASTMethodDecl(ctx,
                        Optional.empty(), new ASTIdentifier(ctx, MAIN), Optional.of(List.of()),
                        List.of(), Optional.empty(), true, false, false),
                new ASTTypeDecl(ctx, new ASTIdentifier(ctx, INTEGER), intType),
                new ASTTypeDecl(ctx, new ASTIdentifier(ctx, INT64), longType),
                new ASTTypeDecl(ctx, new ASTIdentifier(ctx, BOOLEAN), boolType),
                new ASTTypeDecl(ctx, new ASTIdentifier(ctx, STRING), strType),
                new ASTTypeDecl(ctx, new ASTIdentifier(ctx, ERROR), unkType),
                new ASTMethodDecl(ctx,
                        Optional.empty(), new ASTIdentifier(ctx, WRITE), Optional.empty(),
                        List.of(), Optional.empty(), false, false, true),
                new ASTMethodDecl(ctx,
                        Optional.empty(), new ASTIdentifier(ctx, WRITELN), Optional.empty(),
                        List.of(), Optional.empty(), false, false, true),
                new ASTMethodDecl(ctx,
                        Optional.empty(), new ASTIdentifier(ctx, READ), Optional.empty(),
                        List.of(), Optional.empty(), false, false, true),
                new ASTMethodDecl(ctx,
                        Optional.empty(), new ASTIdentifier(ctx, READLN), Optional.empty(),
                        List.of(), Optional.empty(), false, false, true),
                new ASTMethodDecl(ctx,
                        Optional.empty(), new ASTIdentifier(ctx, BREAK), Optional.of(List.of()),
                        List.of(), Optional.empty(), false, false, true),
                new ASTMethodDecl(ctx,
                        Optional.empty(), new ASTIdentifier(ctx, CONTINUE), Optional.of(List.of()),
                        List.of(), Optional.empty(), false, false, true),
                new ASTMethodDecl(ctx,
                        Optional.empty(), new ASTIdentifier(ctx, EXIT), Optional.of(List.of()),
                        List.of(), Optional.empty(), false, false, true),
                new ASTMethodDecl(ctx,
                        Optional.empty(), new ASTIdentifier(ctx, NEW), Optional.empty(),
                        List.of(), Optional.empty(), false, false, true),
                new ASTMethodDecl(ctx,
                        Optional.empty(), new ASTIdentifier(ctx, DISPOSE), Optional.empty(),
                        List.of(), Optional.empty(), false, false, true),
                new ASTMethodDecl(ctx,
                        Optional.of(intType), new ASTIdentifier(ctx, INTEGER), Optional.empty(),
                        List.of(), Optional.empty(), false, false, true),
                new ASTMethodDecl(ctx,
                        Optional.of(longType), new ASTIdentifier(ctx, INT64), Optional.empty(),
                        List.of(), Optional.empty(), false, false, true),
                new ASTMethodDecl(ctx,
                        Optional.of(boolType), new ASTIdentifier(ctx, EOF), Optional.of(List.of()),
                        List.of(), Optional.empty(), false, true, false),
                new ASTMethodDecl(ctx,
                        Optional.of(boolType), new ASTIdentifier(ctx, EOL), Optional.of(List.of()),
                        List.of(), Optional.empty(), false, true, false)
        );
    }

    private Stream<ASTDeclaration> visit(RecafParser.DeclarationContext cst) {
        if (cst.type_section() != null)
            return visit(cst.type_section()).map(x -> x);
        if (cst.const_section() != null)
            return visit(cst.const_section()).map(x -> x);
        if (cst.var_section() != null)
            return visit(cst.var_section()).map(x -> x);
        if (cst.function_decl() != null)
            return Stream.of(visit(cst.function_decl()));
        if (cst.procedure_decl() != null)
            return Stream.of(visit(cst.procedure_decl()));
        throw new AssertionError("This should never happen.");
    }

    private Stream<ASTTypeDecl> visit(RecafParser.Type_sectionContext cst) {
        return cst.type_decl().stream().map(this::visit);
    }

    private ASTTypeDecl visit(RecafParser.Type_declContext cst) {
        ASTContext ctx = ctx(cst);
        return new ASTTypeDecl(ctx,
                visitIdentifier(cst.ID()),
                cst.type_ref() == null ? defaultType(ctx) : visit(cst.type_ref()));
    }

    private ASTType visit(RecafParser.Type_refContext cst) {
        if (cst.base_type() != null)
            return visit(cst.base_type());
        if (cst.array_type() != null)
            return visit(cst.array_type());
        if (cst.record_type() != null)
            return visit(cst.record_type());
        if (cst.enum_type() != null)
            return visit(cst.enum_type());
        if (cst.pointer_type() != null)
            return visit(cst.pointer_type());
        throw new AssertionError("This should never happen.");
    }

    private ASTType defaultType(ASTContext ctx) {
        return new ASTBaseType(ctx, new ASTIdentifier(ctx, "integer"));
    }

    private ASTBaseType visit(RecafParser.Base_typeContext cst) {
        return new ASTBaseType(ctx(cst),
                visitIdentifier(cst.ID()));
    }

    private ASTArrayType visit(RecafParser.Array_typeContext cst) {
        ASTContext ctx = ctx(cst);
        ASTType inner = cst.type_ref() == null ? defaultType(ctx) : visit(cst.type_ref());
        return new ASTArrayType(ctx,
                inner,
                cst.array_index_range().stream().map(this::visit).toList());
    }

    private ASTArrayRange visit(RecafParser.Array_index_rangeContext cst) {
        return new ASTArrayRange(ctx(cst), visit(cst.expr(0)), visit(cst.expr(1)));
    }

    private ASTRecordType visit(RecafParser.Record_typeContext cst) {
        return new ASTRecordType(ctx(cst), visit(cst.var_decls()).toList());
    }

    private ASTEnumType visit(RecafParser.Enum_typeContext cst) {
        return new ASTEnumType(ctx(cst), visit(cst.id_list()).toList());
    }

    private ASTPointerType visit(RecafParser.Pointer_typeContext cst) {
        return new ASTPointerType(ctx(cst), visit(cst.base_type()));
    }

    private Stream<ASTConstDecl> visit(RecafParser.Const_sectionContext cst) {
        return cst.const_decl().stream().map(this::visit);
    }

    private ASTConstDecl visit(RecafParser.Const_declContext cst) {
        return new ASTConstDecl(ctx(cst), visitIdentifier(cst.ID()), visit(cst.expr()));
    }

    private Stream<ASTVarDecl> visit(RecafParser.Var_sectionContext cst) {
        return visit(cst.var_decls());
    }

    private Stream<ASTVarDecl> visit(RecafParser.Var_declsContext cst) {
        return cst.var_decl().stream().flatMap(this::visit);
    }

    private Stream<ASTVarDecl> visit(RecafParser.Var_declContext cst) {
        ASTContext ctx = ctx(cst);
        ASTType type = cst.type_ref() == null ? defaultType(ctx) : visit(cst.type_ref());
        return visit(cst.id_list()).map(id -> new ASTVarDecl(ctx, type, id));
    }

    private ASTMethodDecl visit(RecafParser.Function_declContext cst) {
        ASTContext ctx = ctx(cst);
        return new ASTMethodDecl(ctx,
                Optional.of(cst.type_ref() == null ? defaultType(ctx) : visit(cst.type_ref())),
                visitIdentifier(cst.ID()),
                cst.formal_params() == null ? Optional.of(List.of())
                        : cst.formal_params().ELLIPSIS() != null ? Optional.empty()
                          : cst.formal_params().param_decls() == null ? Optional.of(List.of())
                            : Optional.of(visit(cst.formal_params().param_decls()).toList()),
                cst.routine_block() == null ? List.of()
                        : cst.routine_block().local_declaration().stream()
                          .flatMap(this::visit).toList(),
                cst.routine_block() != null ? Optional.of(visit(cst.routine_block().block())) : Optional.empty(),
                cst.FORWARD() != null, cst.EXTERNAL() != null, false
        );
    }

    private ASTMethodDecl visit(RecafParser.Procedure_declContext cst) {
        return new ASTMethodDecl(ctx(cst),
                Optional.empty(),
                visitIdentifier(cst.ID()),
                cst.formal_params() == null ? Optional.of(List.of())
                        : cst.formal_params().ELLIPSIS() != null ? Optional.empty()
                          : cst.formal_params().param_decls() == null ? Optional.of(List.of())
                            : Optional.of(visit(cst.formal_params().param_decls()).toList()),
                cst.routine_block() == null ? List.of()
                        : cst.routine_block().local_declaration().stream()
                          .flatMap(this::visit).toList(),
                cst.routine_block() != null ? Optional.of(visit(cst.routine_block().block())) : Optional.empty(),
                cst.FORWARD() != null, cst.EXTERNAL() != null, false
        );
    }

    private Stream<ASTVarDecl> visit(RecafParser.Param_declsContext cst) {
        return cst.var_decl().stream().flatMap(this::visit);
    }

    private Stream<ASTDeclaration> visit(RecafParser.Local_declarationContext cst) {
        if (cst.type_section() != null)
            return visit(cst.type_section()).map(x -> x);
        if (cst.const_section() != null)
            return visit(cst.const_section()).map(x -> x);
        if (cst.var_section() != null)
            return visit(cst.var_section()).map(x -> x);
        throw new AssertionError("This should never happen");
    }

    private ASTMethodDecl visit(RecafParser.Main_blockContext cst) {
        ASTContext ctx = ctx(cst);
        return new ASTMethodDecl(ctx,
                Optional.empty(),
                new ASTIdentifier(ctx, MAIN),
                Optional.of(List.of()),
                List.of(),
                Optional.of(visit(cst.block())),
                false, false, false);
    }

    private ASTBlock visit(RecafParser.BlockContext cst) {
        return new ASTBlock(ctx(cst), visit(cst.statement_list()).toList());
    }

    private Stream<ASTStatement> visit(RecafParser.Statement_listContext cst) {
        return cst == null ? Stream.empty() : cst.statement().stream().map(this::visit);
    }

    private ASTStatement visit(RecafParser.StatementContext cst) {
        if (cst.assign_statement() != null)
            return visit(cst.assign_statement());
        if (cst.method_call_statement() != null)
            return visit(cst.method_call_statement());
        if (cst.if_else() != null)
            return visit(cst.if_else());
        if (cst.for_loop() != null)
            return visit(cst.for_loop());
        if (cst.while_loop() != null)
            return visit(cst.while_loop());
        if (cst.repeat_loop() != null)
            return visit(cst.repeat_loop());
        if (cst.block() != null)
            return visit(cst.block());
        throw new AssertionError("This should never happen.");
    }

    private ASTStatement visit(RecafParser.Assign_statementContext cst) {
        return new ASTAssignment(ctx(cst), visit(cst.location()), visit(cst.expr()));
    }

    private ASTLocation visit(RecafParser.LocationContext cst) {
        return new ASTLocation(ctx(cst), visitIdentifier(cst.ID()),
                cst.location_access().stream().map(this::visit).toList());
    }

    private ASTAccessor visit(RecafParser.Location_accessContext cst) {
        if (cst.indexer() != null)
            return visit(cst.indexer());
        if (cst.field_selector() != null)
            return visit(cst.field_selector());
        if (cst.deref_selector() != null)
            return visit(cst.deref_selector());
        throw new AssertionError("This should never happen.");
    }

    private ASTIndexAccess visit(RecafParser.IndexerContext cst) {
        return new ASTIndexAccess(ctx(cst),
                cst.expr().stream().map(this::visit).toList());
    }

    private ASTFieldAccess visit(RecafParser.Field_selectorContext cst) {
        return new ASTFieldAccess(ctx(cst), visitIdentifier(cst.ID()));
    }

    private ASTDerefAccess visit(RecafParser.Deref_selectorContext cst) {
        return new ASTDerefAccess(ctx(cst));
    }

    private ASTMethodCall visit(RecafParser.Method_call_statementContext cst) {
        return new ASTMethodCall(ctx(cst), visitIdentifier(cst.ID()), visit(cst.args()).toList());
    }

    private ASTMethodCall visit(RecafParser.Method_callContext cst) {
        return new ASTMethodCall(ctx(cst), visitIdentifier(cst.ID()), visit(cst.args()).toList());
    }

    private Stream<ASTExpression> visit(RecafParser.ArgsContext cst) {
        return cst == null ? Stream.empty() : cst.expr().stream().map(this::visit);
    }

    private ASTIfElse visit(RecafParser.If_elseContext cst) {
        return new ASTIfElse(ctx(cst),
                visit(cst.expr()),
                visit(cst.statement(0)),
                cst.statement().size() >= 2 ? Optional.of(visit(cst.statement(1)))
                        : Optional.empty()
        );
    }

    private ASTForLoop visit(RecafParser.For_loopContext cst) {
        ASTContext ctx = ctx(cst);
        return new ASTForLoop(ctx,
                new ASTLocation(ctx, visitIdentifier(cst.ID()), List.of()),
                visit(cst.expr(0)),
                visit(cst.expr(1)),
                cst.DOWNTO() != null,
                visit(cst.statement())
        );
    }

    private ASTWhileLoop visit(RecafParser.While_loopContext cst) {
        return new ASTWhileLoop(ctx(cst), visit(cst.expr()), visit(cst.statement()));
    }

    private ASTRepeatLoop visit(RecafParser.Repeat_loopContext cst) {
        return new ASTRepeatLoop(ctx(cst),
                new ASTBlock(ctx(cst.statement_list()), visit(cst.statement_list()).toList()), visit(cst.expr())
        );
    }

    private ASTExpression visit(RecafParser.ExprContext cst) {
        // location
        if (cst.location() != null)
            return visit(cst.location());

        // method call
        if (cst.method_call() != null)
            return visit(cst.method_call());

        // literal
        if (cst.literal() != null)
            return visit(cst.literal());

        // binary operator
        BinaryOperator op = extractBinaryOperator(cst);
        if (op != null)
            return new ASTBinaryExpression(ctx(cst), op,
                    visit(cst.expr(0)), visit(cst.expr(1)));

        // unary operator
        UnaryOperator unOp = extractUnaryOperator(cst);
        if (unOp != null)
            return extractUnaryExpression(cst);

        // parentheses
        if (cst.LPAREN() != null)
            return visit(cst.expr(0));

        throw new AssertionError("This should never happen.");
    }

    private ASTLiteral visit(RecafParser.LiteralContext cst) {
        if (cst.BOOL_LITERAL() != null)
            return new ASTLiteral(ctx(cst),
                    new BoolLiteral(cst.BOOL_LITERAL().getText().equalsIgnoreCase("true")));
        if (cst.INT_LITERAL() != null) {
            ASTContext ctx = ctx(cst);
            return new ASTLiteral(ctx,
                    parseIntegerLiteral(cst.INT_LITERAL().getText(), ctx::error));
        }
        if (cst.STRING_LITERAL() != null) {
            ASTContext ctx = ctx(cst);
            return new ASTLiteral(ctx,
                    new StringLiteral(parseString(cst.STRING_LITERAL().getText(), ctx::error)));
        }
        throw new AssertionError("This should never happen.");
    }

    private ASTExpression extractUnaryExpression(RecafParser.ExprContext cst) {
        RecafParser.LiteralContext lit = cst.expr(0).literal();
        // handle MIN_VALUE
        if (cst.MINUS() != null && lit != null) {
            if (lit.INT_LITERAL() != null) {
                // no issues in hexadecimal
                if (lit.INT_LITERAL().getText().equals("2147483648"))
                    return new ASTLiteral(ctx(cst), new IntLiteral(Integer.MIN_VALUE));
                if (lit.INT_LITERAL().getText().equals("9223372036854775808"))
                    return new ASTLiteral(ctx(cst), new LongLiteral(Long.MIN_VALUE));
            }
        }
        return new ASTUnaryExpression(ctx(cst), extractUnaryOperator(cst), visit(cst.expr(0)));
    }

    private Stream<ASTIdentifier> visit(RecafParser.Id_listContext cst) {
        return cst.ID().stream().map(this::visitIdentifier);
    }

    private ASTIdentifier visitIdentifier(TerminalNode ID) {
        return new ASTIdentifier(new ASTContext(inv, ID), ID.getText());
    }

    private ASTContext ctx(ParserRuleContext cst) { return new ASTContext(inv, cst); }

    private static Literal parseIntegerLiteral(String text, Consumer<String> error) {
        try {
            long value;
            if (text.startsWith("$")) {
                value = hexToLong(text.substring(1));
            } else {
                BigInteger big = new BigInteger(text, 10);
                if (big.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0)
                    throw new NumberFormatException();
                value = big.longValueExact();
            }
            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE)
                return new IntLiteral((int) value);
            return new LongLiteral(value);
        } catch (NumberFormatException e) {
            error.accept("invalid integer literal " + text);
            return new IntLiteral(0);
        }
    }

    private static long hexToLong(String s) {
        BigInteger parse = new BigInteger(s, 16);
        if (parse.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0)
            parse = parse.subtract(BigInteger.ONE.shiftLeft(64));
        if (parse.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
                || parse.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0)
            throw new NumberFormatException();
        return parse.longValue();
    }

    private static String parseString(String text, Consumer<String> error) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\'') {
                i++;
                while (i < text.length()) {
                    char d = text.charAt(i);
                    if (d == '\'') {
                        if (i + 1 < text.length() && text.charAt(i + 1) == '\'') {
                            sb.append('\'');
                            i += 2;
                        } else {
                            i++;
                            break;
                        }
                    } else {
                        sb.append(d);
                        i++;
                    }
                }
                continue;
            }
            if (c == '#') {
                i++;
                int start = i;
                while (i < text.length() && Character.isDigit(text.charAt(i)))
                    i++;
                if (start == i) {
                    error.accept("invalid string escape near '#'");
                    continue;
                }
                int code = Integer.parseInt(text.substring(start, i));
                if (code < 0 || code > 255)
                    error.accept("string escape code out of range: #" + code);
                sb.append((char) (code & 0xFF));
                continue;
            }
            error.accept("invalid string literal segment");
            i++;
        }
        return sb.toString();
    }

    private BinaryOperator extractBinaryOperator(RecafParser.ExprContext cst) {
        if (cst.mul_op() != null) {
            if (cst.mul_op().TIMES() != null)
                return BinaryOperator.TIMES;
            if (cst.mul_op().DIV() != null)
                return BinaryOperator.DIVIDES;
            if (cst.mul_op().MOD() != null)
                return BinaryOperator.MOD;
            if (cst.mul_op().AND() != null)
                return BinaryOperator.AND;
        }

        if (cst.add_op() != null) {
            if (cst.add_op().PLUS() != null)
                return BinaryOperator.PLUS;
            if (cst.add_op().MINUS() != null)
                return BinaryOperator.MINUS;
            if (cst.add_op().OR() != null)
                return BinaryOperator.OR;
        }

        if (cst.rel_op() != null) {
            if (cst.rel_op().EQUALS() != null)
                return BinaryOperator.EQ;
            if (cst.rel_op().NEQ() != null)
                return BinaryOperator.NEQ;
            if (cst.rel_op().LT() != null)
                return BinaryOperator.LT;
            if (cst.rel_op().GT() != null)
                return BinaryOperator.GT;
            if (cst.rel_op().LEQ() != null)
                return BinaryOperator.LEQ;
            if (cst.rel_op().GEQ() != null)
                return BinaryOperator.GEQ;
        }

        return null;
    }

    private UnaryOperator extractUnaryOperator(RecafParser.ExprContext cst) {
        if (cst.NOT() != null)
            return UnaryOperator.NOT;
        if (cst.MINUS() != null)
            return UnaryOperator.MINUS;

        return null;
    }


}
