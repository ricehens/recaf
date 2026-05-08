package recaf.ast;

import recaf.ast.nodes.*;
import recaf.cfg.*;
import recaf.general.IntLiteral;
import recaf.general.Type;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class Linearizer {

    private final CFGContext ctx;
    private final CFGSymbolTable symbolTable;

    private CFGBuilder cfg;

    private final Map<String, Optional<ASTType>> methods;
    private final Map<String, ASTType> globalTypes;
    private final Map<String, ASTVarDecl> globalVars;

    private final Map<ASTVarDecl, CFGVariable> symbols;

    private boolean local;
    private Map<String, ASTType> localTypes;
    private Map<String, ASTVarDecl> localVars;
    private String currentMethod;

    public Linearizer(SemanticChecker sc) {
        symbolTable = new CFGSymbolTable();
        ctx = new CFGContext(null, symbolTable);

        methods = new HashMap<>();
        globalTypes = new HashMap<>();
        globalVars = new HashMap<>();

        symbols = new IdentityHashMap<>();

        local = false;
        currentMethod = null;
    }

    public CFGProgram linearize(ASTProgram ast) {
        for (ASTDeclaration decl : ast.decls()) {
            switch (decl) {
                case ASTVarDecl vd -> {
                    linearize(vd);
                    ctx.addGlobalVar(symbols.get(vd).getAddress());
                }
                case ASTMethodDecl md -> {
                    // preprocess methods first
                    methods.put(md.id().text(), md.returnType());
                    if (md.external()) symbolTable.addExternalMethod(md.id().text());
                }
                case ASTTypeDecl td -> linearize(td);
                default -> {} // ignore const decls
            }
        }

        return ctx.setProgram(new CFGProgram(ctx,
                ast.decls().stream()
                        .filter(ASTMethodDecl.class::isInstance)
                        .map(ASTMethodDecl.class::cast)
                        .map(this::linearize)
                        .flatMap(Optional::stream)
                        .toList()));
    }

    private void linearize(ASTVarDecl ast) {
        CFGVariable variable = switch (ast.type()) {
            case ASTArrayType _, ASTRecordType _ ->
                new CFGVariable(symbolTable, ast.id().text(), Type.RECORD, sizeof(ast.type()));
            case ASTEnumType _ ->
                    new CFGVariable(symbolTable, ast.id().text(), Type.INT);
            case ASTPointerType _ ->
                    new CFGVariable(symbolTable, ast.id().text(), Type.POINTER);
            case ASTPrimitiveType pr ->
                new CFGVariable(symbolTable, ast.id().text(), pr.type());
            // already reduced
            default -> throw new AssertionError("This should never happen.");
        };

        (local ? localVars : globalVars).put(ast.id().text(), ast);
        symbols.put(ast, variable);
    }

    private void linearize(ASTTypeDecl ast) {
        (local ? localTypes : globalTypes).put(ast.id().text(), ast.type());
    }

    private Optional<CFGMethod> linearize(ASTMethodDecl ast) {
        if (ast.block().isEmpty()) return Optional.empty();

        currentMethod = ast.id().text();
        local = true;
        localTypes = new HashMap<>();
        localVars = new HashMap<>();

        for (ASTDeclaration decl : ast.decls()) {
            if (decl instanceof ASTVarDecl vd) linearize(vd);
            else if (decl instanceof ASTTypeDecl td) linearize(td);
            // ignore const decls
        }

        cfg = new CFGBuilder(ctx);
        cfg.newBlock();
        linearize(ast.block().get());

        local = false;
        return Optional.of(new CFGMethod(ctx,
                ast.returnType().map(this::reduceType).orElse(Type.VOID),
                ast.id().text(),
                ast.params().orElseThrow().stream()
                        .map(symbols::get)
                        .map(CFGVariable::getAddress)
                        .toList(),
                cfg));
    }

    private void linearize(ASTBlock ast) {
        ast.statements().forEach(this::dispatch);
    }

    private void dispatch(ASTStatement ast) {
        // TODO
    }

    private ASTType getType(ASTIdentifier id) {
        String key = id.text();
        if (local)
            if (localTypes.containsKey(key)) return localTypes.get(key);
        return globalTypes.get(key);
    }

    private Type reduceType(ASTType type) {
        return switch (type) {
            case ASTPrimitiveType pt -> pt.type();
            case ASTEnumType _ -> Type.INT;
            case ASTPointerType _ -> Type.POINTER;
            case ASTArrayType _, ASTRecordType _ -> Type.RECORD;
            default -> throw new AssertionError("This should never happen.");
        };
    }

    private int sizeof(ASTType type) {
        return switch (type) {
            case ASTEnumType _ -> 4;
            case ASTPointerType _ -> 8;
            case ASTBaseType bt -> sizeof(getType(bt.id()));

            case ASTPrimitiveType pr -> switch (pr.type()) {
                case INT -> 4;
                case LONG -> 8;
                case BOOL -> 1;
                default -> throw new AssertionError("This should never happen.");
            };

            case ASTArrayType at -> {
                int size = sizeof(at.type());
                for (ASTArrayRange range : at.ranges()) {
                    if (!(range.lower() instanceof ASTLiteral lower)
                            || !(range.upper() instanceof ASTLiteral upper)
                            || !(lower.literal() instanceof IntLiteral(int l))
                            || !(upper.literal() instanceof IntLiteral(int u)))
                        throw new AssertionError("This should never happen.");
                    size *= u - l;
                }
                yield size;
            }

            case ASTRecordType rt -> fieldToIndex(rt, "");

            default -> throw new AssertionError("This should never happen.");
        };
    }

    // in bytes
    private int fieldToIndex(ASTRecordType rt, String key) {
        int offset = 0;
        int maxAlign = 1;
        for (ASTVarDecl field : rt.fields()) {
            int a = align(field.type());
            maxAlign = Math.max(maxAlign, a);
            offset = roundUp(offset, a);
            if (field.id().text().equals(key))
                return offset;
            offset += sizeof(field.type());
        }
        return roundUp(offset, maxAlign);
    }

    private int align(ASTType type) {
        return switch (type) {
            case ASTEnumType _ -> 4;
            case ASTPointerType _ -> 8;
            case ASTBaseType bt -> align(getType(bt.id()));

            case ASTPrimitiveType pr -> switch (pr.type()) {
                case INT -> 4;
                case LONG -> 8;
                case BOOL -> 1;
                default -> throw new AssertionError("This should never happen.");
            };

            case ASTArrayType at -> align(at.type());

            case ASTRecordType rt -> {
                int align = 1;
                for (ASTVarDecl field : rt.fields())
                    align = Math.max(align, align(field.type()));
                yield align;
            }

            default -> throw new AssertionError("This should never happen.");
        };
    }

    private int roundUp(int a, int m) {
        return (a + m - 1) / m * m;
    }

}
