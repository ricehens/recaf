package recaf.ast;

import recaf.ast.nodes.*;
import recaf.cfg.*;
import recaf.general.*;

import java.util.*;

import static recaf.ast.ASTUtils.*;

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
    private String currentMethod;

    private CFGAddress breakAddress;
    private CFGAddress continueAddress;

    public Linearizer(SemanticChecker sc) {
        this.sc = sc;
        symbolTable = new CFGSymbolTable();
        ctx = new CFGContext(null, symbolTable);

        methods = new HashMap<>();
        globalTypes = new HashMap<>();
        globalVars = new HashMap<>();

        symbols = new IdentityHashMap<>();

        local = false;
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
        linearize(null, new ASTMethodCall(md.ctx(), new ASTIdentifier(md.ctx(), EXIT), List.of()));

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
        switch (statement) {
            case ASTAssignment as -> linearize(as);
            case ASTMethodCall mc -> linearize(mc);
            case ASTIfElse ie -> linearize(ie);
            case ASTForLoop fl -> linearize(fl);
            case ASTWhileLoop wl -> linearize(wl);
            case ASTRepeatLoop rl -> linearize(rl);
            case ASTBlock blk -> linearize(blk);
            default -> throw new AssertionError("This should never happen.");
        };
    }

    private void linearize(ASTAssignment as) {
        ASTLocation loc = as.location();
        ASTExpression expr = as.expr();
        ASTType type = sc.exprType(loc);

        if (type instanceof ASTArrayType || type instanceof ASTRecordType) {
            ASTLocation rhs = (ASTLocation) expr;
            // TODO emit memcpy

            return;
        }

        CFGAddress addr = linearize(expr);
        write(loc, addr);
    }

    private void linearize(ASTIfElse ifElse) {
        CFGAddress boolAddr = linearize(ifElse.cond());
        CFGAddress thenAddr = new CFGAddress();
        CFGAddress elseAddr = new CFGAddress();
        CFGAddress postAddr = new CFGAddress();

        cfg.offer(new CFGBranchInstruction(ctx, boolAddr, thenAddr,
                ifElse.elseBlock().isPresent() ? elseAddr : postAddr));
        thenAddr.set(cfg.newBlock().address());
        dispatch(ifElse.thenBlock());
        cfg.offer(new CFGJumpInstruction(ctx, postAddr));

        if (ifElse.elseBlock().isPresent()) {
            elseAddr.set(cfg.newBlock().address());
            dispatch(ifElse.elseBlock().get());
            cfg.offer(new CFGJumpInstruction(ctx, postAddr));
        }
        postAddr.set(cfg.newBlock().address());
    }

    private void linearize(ASTForLoop fl) {
        CFGAddress dummyAddr = symbols.get(getVar(fl.dummy().id())).getAddress();
        CFGAddress startAddr = linearize(fl.start());
        CFGAddress endAddr = linearize(fl.end());
        cfg.offer(new CFGCopyInstruction(ctx, dummyAddr, startAddr));

        CFGAddress loopAddr = new CFGAddress();
        CFGAddress incrementAddr = new CFGAddress();
        CFGAddress exitAddr = new CFGAddress();

        CFGAddress prevBreakAddr = breakAddress;
        CFGAddress prevContinueAddr = continueAddress;
        breakAddress = exitAddr;
        continueAddress = incrementAddr;

        // inverted loop logic
        CFGAddress boolAddr = ctx.newAddress(Type.BOOL);
        cfg.offer(new CFGBinaryInstruction(ctx, boolAddr,
                fl.descending() ? BinaryOperator.GEQ : BinaryOperator.LEQ,
                dummyAddr, endAddr));
        cfg.offer(new CFGBranchInstruction(ctx, boolAddr, loopAddr, exitAddr));

        loopAddr.set(cfg.newBlock().address());
        dispatch(fl.body());

        incrementAddr.set(cfg.newBlock().address());
        cfg.offer(new CFGBinaryInstruction(ctx, dummyAddr,
                fl.descending() ? BinaryOperator.MINUS : BinaryOperator.PLUS,
                dummyAddr,
                ctx.getType(dummyAddr) == Type.LONG
                        ? makeLongLiteral(1) : makeIntLiteral(1)));

        CFGAddress boolAddr2 = ctx.newAddress(Type.BOOL);
        cfg.offer(new CFGBinaryInstruction(ctx, boolAddr2,
                fl.descending() ? BinaryOperator.GEQ : BinaryOperator.LEQ,
                dummyAddr, endAddr));
        cfg.offer(new CFGBranchInstruction(ctx, boolAddr2, loopAddr, exitAddr));
        exitAddr.set(cfg.newBlock().address());

        breakAddress = prevBreakAddr;
        continueAddress = prevContinueAddr;
    }

    private void linearize(ASTWhileLoop wl) {
        CFGAddress condAddr = new CFGAddress();
        CFGAddress loopAddr = new CFGAddress();
        CFGAddress exitAddr = new CFGAddress();

        CFGAddress prevBreakAddr = breakAddress;
        CFGAddress prevContinueAddr = continueAddress;
        breakAddress = exitAddr;
        continueAddress = condAddr;

        // inverted loop logic
        CFGAddress boolAddr = linearize(wl.cond());
        cfg.offer(new CFGBranchInstruction(ctx, boolAddr, loopAddr, exitAddr));
        loopAddr.set(cfg.newBlock().address());
        dispatch(wl.body());
        condAddr.set(cfg.newBlock().address());
        CFGAddress boolAddr2 = linearize(wl.cond());
        cfg.offer(new CFGBranchInstruction(ctx, boolAddr2, loopAddr, exitAddr));
        exitAddr.set(cfg.newBlock().address());

        breakAddress = prevBreakAddr;
        continueAddress = prevContinueAddr;
    }

    private void linearize(ASTRepeatLoop rl) {
        CFGAddress loopAddr = new CFGAddress();
        CFGAddress condAddr = new CFGAddress();
        CFGAddress exitAddr = new CFGAddress();

        CFGAddress prevBreakAddr = breakAddress;
        CFGAddress prevContinueAddr = continueAddress;
        breakAddress = exitAddr;
        continueAddress = condAddr;

        loopAddr.set(cfg.currentBlock().address());
        dispatch(rl.body());
        condAddr.set(cfg.newBlock().address());
        CFGAddress cond = linearize(rl.cond());
        cfg.offer(new CFGBranchInstruction(ctx, cond, exitAddr, loopAddr));
        exitAddr.set(cfg.newBlock().address());

        breakAddress = prevBreakAddr;
        continueAddress = prevContinueAddr;
    }

    private CFGAddress linearize(ASTExpression expr) {
        if (expr instanceof ASTLocation loc && loc.accesses().isEmpty())
            return symbols.get(getVar(loc.id())).getAddress();
        CFGAddress addr = ctx.newAddress(reduceType(sc.exprType(expr)));
        dispatch(addr, expr);
        return addr;
    }

    private void dispatch(CFGAddress dest, ASTExpression expr) {
        switch (expr) {
            case ASTLocation loc -> read(dest, loc);
            case ASTMethodCall mc -> linearize(dest, mc);
            case ASTLiteral lit -> linearize(dest, lit);
            case ASTBinaryExpression be -> linearize(dest, be);
            case ASTUnaryExpression ue -> linearize(dest, ue);
            default -> throw new AssertionError("This should never happen.");
        }
    }

    private void linearize(CFGAddress dest, ASTMethodCall mc) {
        switch (mc.id().text()) {
            case BREAK -> {
                cfg.offer(new CFGJumpInstruction(ctx, breakAddress));
                cfg.newBlock();
            }

            case CONTINUE -> {
                cfg.offer(new CFGJumpInstruction(ctx, continueAddress));
                cfg.newBlock();
            }

            case EXIT -> {
                if (methods.get(currentMethod).isPresent()) {
                    cfg.offer(new CFGReturnInstruction(ctx, symbols.get(localVars.get(currentMethod)).getAddress()));
                    cfg.newBlock();
                } else if (currentMethod.equals("main")) {
                    CFGAddress exitCode = makeIntLiteral(0);
                    cfg.offer(new CFGReturnInstruction(ctx, exitCode));
                    cfg.newBlock();
                } else cfg.offer(new CFGReturnInstruction(ctx));
            }

            case INTEGER ->
                    cfg.offer(new CFGCastInstruction(ctx, dest, Type.INT, linearize(mc.args().getFirst())));
            case INT64 ->
                    cfg.offer(new CFGCastInstruction(ctx, dest, Type.LONG, linearize(mc.args().getFirst())));

            case NEW -> {
                symbolTable.addExternalMethod(MALLOC);
                ASTLocation loc = (ASTLocation) mc.args().getFirst();
                ASTPointerType type = (ASTPointerType) sc.exprType(loc);

                CFGAddress tmp = ctx.newAddress(Type.POINTER);
                CFGAddress size = makeIntLiteral(sizeof(type.type()));
                cfg.offer(new CFGMethodCallInstruction(ctx, tmp, MALLOC, List.of(size)));

                write(loc, tmp);
            }

            case DISPOSE -> {
                symbolTable.addExternalMethod(FREE);
                CFGAddress ptr = linearize(mc.args().getFirst());
                cfg.offer(new CFGMethodCallInstruction(ctx, null, FREE, List.of(ptr)));
            }

            case WRITE, WRITELN -> {
                for (ASTExpression arg : mc.args()) {
                    CFGAddress addr = linearize(arg);
                    if (ctx.getType(addr) == Type.BOOL) {
                        CFGAddress trueAddr = new CFGAddress();
                        CFGAddress falseAddr = new CFGAddress();
                        CFGAddress postAddr = new CFGAddress();

                        cfg.offer(new CFGBranchInstruction(ctx, addr, trueAddr, falseAddr));

                        trueAddr.set(cfg.newBlock().address());
                        CFGAddress trueStr = makeStringLiteral("TRUE");
                        cfg.offer(new CFGMethodCallInstruction(ctx, null, PRINTF, List.of(trueStr)));
                        cfg.offer(new CFGJumpInstruction(ctx, postAddr));

                        falseAddr.set(cfg.newBlock().address());
                        CFGAddress falseStr = makeStringLiteral("FALSE");
                        cfg.offer(new CFGMethodCallInstruction(ctx, null, PRINTF, List.of(falseStr)));
                        cfg.offer(new CFGJumpInstruction(ctx, postAddr));

                        postAddr.set(cfg.newBlock().address());
                    } else {
                        CFGAddress fmt = makeStringLiteral(
                                switch (ctx.getType(addr)) {
                                    case INT -> "%d";
                                    case LONG -> "%lld";
                                    case STRING -> "%s";
                                    default -> throw new AssertionError("This should never happen.");
                                }
                        );
                        cfg.offer(new CFGMethodCallInstruction(ctx, null, PRINTF,
                                List.of(fmt, addr)));
                    }
                }
                CFGAddress newline = makeStringLiteral("\\n");
                if (WRITELN.equals(mc.id().text()))
                    cfg.offer(new CFGMethodCallInstruction(ctx, null, PRINTF, List.of(newline)));
            }

            default -> {
                Optional<ASTType> returnType = methods.get(mc.id().text());
                cfg.offer(new CFGMethodCallInstruction(ctx,
                        returnType.isEmpty() ? null : dest,
                        mc.id().text(),
                        mc.args().stream().map(this::linearize).toList()));
            }
        }
    }

    private void linearize(CFGAddress dest, ASTLiteral lit) {
        cfg.offer(new CFGLiteralInstruction(ctx, dest, lit.literal()));
    }

    private void linearize(CFGAddress dest, ASTUnaryExpression unary) {
        cfg.offer(new CFGUnaryInstruction(ctx, dest, unary.op(), linearize(unary.expr())));
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

    private void read(CFGAddress addr, ASTLocation loc) {
        if (loc.accesses().isEmpty()) {
            CFGAddress src = linearize(loc);
            cfg.offer(new CFGCopyInstruction(ctx, addr, src));
            return;
        }

        // TODO emit CFG read instruction(s)
    }

    // write scalar value to loc
    private void write(ASTLocation loc, CFGAddress addr) {
        ASTType type = sc.exprType(loc);

        if (loc.accesses().isEmpty()) {
            CFGAddress dest = linearize(loc);
            cfg.offer(new CFGCopyInstruction(ctx, dest, addr));
            return;
        }

        // TODO emit CFG write instruction (+ read instructions if deref?)
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

    private CFGAddress makeIntLiteral(int value) {
        CFGAddress addr = ctx.newAddress(Type.INT);
        cfg.offer(new CFGLiteralInstruction(ctx, addr, new IntLiteral(value)));
        return addr;
    }

    private CFGAddress makeLongLiteral(long value) {
        CFGAddress addr = ctx.newAddress(Type.LONG);
        cfg.offer(new CFGLiteralInstruction(ctx, addr, new LongLiteral(value)));
        return addr;
    }

    private CFGAddress makeStringLiteral(String value) {
        CFGAddress addr = ctx.newAddress(Type.STRING);
        cfg.offer(new CFGLiteralInstruction(ctx, addr, new StringLiteral(value)));
        return addr;
    }


}
