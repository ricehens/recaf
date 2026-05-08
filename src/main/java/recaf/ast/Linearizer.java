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

    public Linearizer(SemanticChecker sc) {
        symbolTable = new CFGSymbolTable();
        ctx = new CFGContext(null, symbolTable);

        methods = new HashMap<>();
        globalTypes = new HashMap<>();
        globalVars = new HashMap<>();

        symbols = new IdentityHashMap<>();

        local = false;
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

        // TODO
        return null;
        // TODO set ctx program in each method decl
    }

    private void linearize(ASTVarDecl vd) {
        CFGVariable variable = switch (vd.type()) {
            case ASTArrayType _, ASTRecordType _ ->
                new CFGVariable(symbolTable, vd.id().text(), Type.RECORD, sizeof(vd.type()));
            case ASTEnumType _ ->
                    new CFGVariable(symbolTable, vd.id().text(), Type.INT);
            case ASTPointerType _ ->
                    new CFGVariable(symbolTable, vd.id().text(), Type.POINTER);
            case ASTPrimitiveType pr ->
                new CFGVariable(symbolTable, vd.id().text(), pr.type());
            default -> throw new AssertionError("This should never happen.");
        };

        (local ? localVars : globalVars).put(vd.id().text(), vd);
        symbols.put(vd, variable);
    }

    private void linearize(ASTTypeDecl td) {
        (local ? localTypes : globalTypes).put(td.id().text(), td.type());
    }

    private ASTType getType(ASTIdentifier id) {
        String key = id.text();
        if (local)
            if (localTypes.containsKey(key)) return localTypes.get(key);
        return globalTypes.get(key);
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
