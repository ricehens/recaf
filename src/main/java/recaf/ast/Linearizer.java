package recaf.ast;

import recaf.ast.nodes.*;
import recaf.cfg.*;
import recaf.general.BinaryOperator;
import recaf.general.IntLiteral;
import recaf.general.Type;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class Linearizer {

    private final SemanticChecker sc;

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
    private String currentMethod; // TODO return 0 at end of main

    public Linearizer(SemanticChecker sc) {
        this.sc = sc;
        symbolTable = new CFGSymbolTable();
        ctx = new CFGContext(null, symbolTable);

        methods = new HashMap<>();
        globalTypes = new HashMap<>();
        globalVars = new HashMap<>();

        symbols = new IdentityHashMap<>();

        local = false;
        currentMethod = null;
    }

    public CFGProgram linearize(ASTProgram prog) {
        for (ASTDeclaration decl : prog.decls()) {
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
                prog.decls().stream()
                        .filter(ASTMethodDecl.class::isInstance)
                        .map(ASTMethodDecl.class::cast)
                        .map(this::linearize)
                        .flatMap(Optional::stream)
                        .toList()));
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
            // already reduced
            default -> throw new AssertionError("This should never happen.");
        };

        (local ? localVars : globalVars).put(vd.id().text(), vd);
        symbols.put(vd, variable);
    }

    private void linearize(ASTTypeDecl td) {
        (local ? localTypes : globalTypes).put(td.id().text(), td.type());
    }

    private Optional<CFGMethod> linearize(ASTMethodDecl md) {
        if (md.block().isEmpty()) return Optional.empty();

        currentMethod = md.id().text();
        local = true;
        localTypes = new HashMap<>();
        localVars = new HashMap<>();

        for (ASTDeclaration decl : md.decls()) {
            if (decl instanceof ASTVarDecl vd) linearize(vd);
            else if (decl instanceof ASTTypeDecl td) linearize(td);
            // ignore const decls
        }

        cfg = new CFGBuilder(ctx);
        cfg.newBlock();
        linearize(md.block().get());

        if (md.returnType().isPresent()) {
            cfg.offer(new CFGReturnInstruction(ctx, symbols.get(localVars.get(md.id().text())).getAddress()));
            cfg.newBlock();
        } else if (currentMethod.equals("main")) {
            CFGAddress exitCode = ctx.newAddress(Type.INT);
            cfg.offer(new CFGLiteralInstruction(ctx, exitCode, 0));
            cfg.offer(new CFGReturnInstruction(ctx, exitCode));
            cfg.newBlock();
        }

        local = false;
        return Optional.of(new CFGMethod(ctx,
                md.returnType().map(this::reduceType).orElse(Type.VOID),
                md.id().text(),
                md.params().orElseThrow().stream()
                        .map(symbols::get)
                        .map(CFGVariable::getAddress)
                        .toList(),
                cfg));
    }

    private void linearize(ASTBlock blk) {
        blk.statements().forEach(this::dispatch);
    }

    private void dispatch(ASTStatement statement) {
        // TODO
    }

    private CFGAddress linearize(ASTExpression expr) {
        if (expr instanceof ASTLocation loc && loc.accesses().isEmpty())
            return symbols.get(getVar(loc.id())).getAddress();
        CFGAddress addr = ctx.newAddress(reduceType(sc.exprType(expr)));
        dispatch(addr, expr);
        return addr;
    }

    private void dispatch(CFGAddress dest, ASTExpression expr) {
        // TODO
    }

    private void linearize(CFGAddress dest, ASTBinaryExpression bin) {
        if (bin.op().getType() == BinaryOperator.BinOpType.COND_OP) {
            // short circuit conditionals
            CFGAddress thenAddr = new CFGAddress();
            CFGAddress postAddr = new CFGAddress();

            dispatch(dest, bin.left());
            cfg.offer(bin.op() == BinaryOperator.AND ? new CFGBranchInstruction(ctx, dest, thenAddr, postAddr)
                    : new CFGBranchInstruction(ctx, dest, postAddr, thenAddr));

            thenAddr.set(cfg.newBlock().address());
            dispatch(dest, bin.right());
            cfg.offer(new CFGJumpInstruction(ctx, postAddr));

            postAddr.set(cfg.newBlock().address());
        } else {
            CFGAddress lAddr = linearize(bin.left());
            CFGAddress rAddr = linearize(bin.right());
            cfg.offer(new CFGBinaryInstruction(ctx, dest, bin.op(), lAddr, rAddr));
        }
    }

    private ASTVarDecl getVar(ASTIdentifier id) {
        String key = id.text();
        if (local)
            if (localVars.containsKey(key)) return localVars.get(key);
        return globalVars.get(key);
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
