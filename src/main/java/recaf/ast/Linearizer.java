package recaf.ast;

import recaf.ast.nodes.*;
import recaf.cfg.*;
import recaf.general.*;

import java.util.*;
import java.util.stream.Stream;

import static recaf.ast.ASTUtils.*;

public class Linearizer {

    private SemanticChecker sc;

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

    private CFGAddress localIntBuffer;
    private CFGAddress localLongBuffer;

    public Linearizer() {
        symbolTable = new CFGSymbolTable();
        ctx = new CFGContext(null, symbolTable);

        methods = new HashMap<>();
        globalTypes = new HashMap<>();
        globalVars = new HashMap<>();

        symbols = new IdentityHashMap<>();

        local = false;
    }

    public CFGProgram linearize(ASTProgram prog) {
        sc = prog.ctx().getSemanticChecker();

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
        localIntBuffer = null;
        localLongBuffer = null;

        md.params().orElseThrow().forEach(this::linearize);
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
                Stream.concat(
                        Stream.of(localIntBuffer, localLongBuffer).flatMap(Stream::ofNullable),
                        localVars.values().stream().map(symbols::get).map(CFGVariable::getAddress)
                ).toList(),
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

        if (sc.exprType(expr) instanceof ASTPrimitiveType pr
                && pr.type() == Type.STRING) {
            // str lit copy --- copy one by one
            CFGAddress base = reduce(locate(loc));
            String str = ((StringLiteral) ((ASTLiteral) expr).literal()).value();
            cfg.offer(new CFGWriteInstruction(ctx, base, 4,
                    makeIntLiteral(0), makeIntLiteral(str.length())));
            for (int i = 0; i < str.length(); i++)
                cfg.offer(new CFGWriteInstruction(ctx, base, 4,
                        makeIntLiteral( i + 1), makeIntLiteral(str.charAt(i))));
        } else if (type instanceof ASTArrayType || type instanceof ASTRecordType) {
            // array copy --- emit memcpy
            symbolTable.addExternalMethod(MEMCPY);
            cfg.offer(new CFGMethodCallInstruction(ctx, null, MEMCPY,
                    List.of(reduce(locate(loc)), reduce(locate((ASTLocation) expr)),
                            makeLongLiteral(sizeof(type)))));
        } else write(locate(loc), linearize(expr));
    }

    private CFGAddress reduce(LocationTarget target) {
        if (target.simple()) return target.base();

        CFGAddress ret = ctx.newAddress(Type.LONG);
        CFGAddress tmp = ctx.newAddress(Type.LONG);
        cfg.offer(new CFGCastInstruction(ctx, tmp, Type.LONG, target.offset));
        cfg.offer(new CFGBinaryInstruction(ctx, tmp, BinaryOperator.TIMES,
                tmp, makeLongLiteral(target.width)));

        CFGAddress base = ctx.newAddress(Type.LONG);
        cfg.offer(new CFGCastInstruction(ctx, base, Type.LONG, target.base));
        cfg.offer(new CFGBinaryInstruction(ctx, ret, BinaryOperator.PLUS,
                tmp, base));
        return ret;
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
        CFGAddress dummy = symbols.get(getVar(fl.dummy().id())).getAddress();
        CFGAddress startAddr = linearize(fl.start());
        CFGAddress endAddr = linearize(fl.end());
        cfg.offer(new CFGCopyInstruction(ctx, dummy, startAddr));

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
                dummy, endAddr));
        cfg.offer(new CFGBranchInstruction(ctx, boolAddr, loopAddr, exitAddr));

        loopAddr.set(cfg.newBlock().address());
        dispatch(fl.body());

        incrementAddr.set(cfg.newBlock().address());
        cfg.offer(new CFGBinaryInstruction(ctx, dummy,
                fl.descending() ? BinaryOperator.MINUS : BinaryOperator.PLUS,
                dummy,
                ctx.getType(dummy) == Type.LONG
                        ? makeLongLiteral(1) : makeIntLiteral(1)));

        CFGAddress boolAddr2 = ctx.newAddress(Type.BOOL);
        cfg.offer(new CFGBinaryInstruction(ctx, boolAddr2,
                fl.descending() ? BinaryOperator.GEQ : BinaryOperator.LEQ,
                dummy, endAddr));
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

        loopAddr.set(cfg.newBlock().address());
        dispatch(rl.body());
        condAddr.set(cfg.newBlock().address());
        CFGAddress cond = linearize(rl.cond());
        cfg.offer(new CFGBranchInstruction(ctx, cond, exitAddr, loopAddr));
        exitAddr.set(cfg.newBlock().address());

        breakAddress = prevBreakAddr;
        continueAddress = prevContinueAddr;
    }

    private CFGAddress linearize(ASTExpression expr) {
        CFGAddress addr = ctx.newAddress(reduceType(sc.exprType(expr)));
        dispatch(addr, expr);
        return addr;
    }

    private void dispatch(CFGAddress dest, ASTExpression expr) {
        switch (expr) {
            case ASTLocation loc -> linearize(dest, loc);
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
                } else {
                    cfg.offer(new CFGReturnInstruction(ctx));
                    cfg.newBlock();
                }
            }

            case INTEGER ->
                    cfg.offer(new CFGCastInstruction(ctx, dest, Type.INT, linearize(mc.args().getFirst())));
            case INT64 ->
                    cfg.offer(new CFGCastInstruction(ctx, dest, Type.LONG, linearize(mc.args().getFirst())));
            case CHAR -> {
                char c = ((StringLiteral) ((ASTLiteral) mc.args().getFirst()).literal()).value().charAt(0);
                cfg.offer(new CFGLiteralInstruction(ctx, dest, new IntLiteral(c)));
            }

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
                symbolTable.addExternalMethod(PRINTF);

                for (ASTExpression arg : mc.args()) {
                    if (sc.exprType(arg) instanceof ASTArrayType) {
                        // case char array
                        CFGAddress addr = reduce(locate((ASTLocation) arg));
                        symbolTable.addExternalMethod(PUTCHAR);
                        CFGAddress dummy = ctx.newAddress(Type.INT);
                        CFGAddress chr = ctx.newAddress(Type.INT);
                        CFGAddress cmp = ctx.newAddress(Type.BOOL);
                        CFGAddress len = ctx.newAddress(Type.INT);

                        cfg.offer(new CFGReadInstruction(ctx, len, addr, 4, makeIntLiteral(0)));
                        cfg.offer(new CFGLiteralInstruction(ctx, dummy, new IntLiteral(1)));
                        CFGAddress loopAddr = new CFGAddress();
                        CFGAddress exitAddr = new CFGAddress();

                        loopAddr.set(cfg.newBlock().address());
                        cfg.offer(new CFGReadInstruction(ctx, chr, addr, 4, dummy));
                        cfg.offer(new CFGMethodCallInstruction(ctx, null, PUTCHAR, List.of(chr)));
                        cfg.offer(new CFGBinaryInstruction(ctx, dummy, BinaryOperator.PLUS, dummy, makeIntLiteral(1)));
                        cfg.offer(new CFGBinaryInstruction(ctx, cmp, BinaryOperator.LEQ, dummy, len));
                        cfg.offer(new CFGBranchInstruction(ctx, cmp, loopAddr, exitAddr));
                        exitAddr.set(cfg.newBlock().address());
                    } else {
                        CFGAddress addr = linearize(arg);
                        if (ctx.getType(addr) == Type.BOOL) {
                            // case boolean
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
                            // all other cases
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
                }
                if (WRITELN.equals(mc.id().text())) {
                    CFGAddress newline = makeStringLiteral("\n");
                    cfg.offer(new CFGMethodCallInstruction(ctx, null, PRINTF, List.of(newline)));
                }
            }

            case READ, READLN -> {
                symbolTable.addExternalMethod(SCANF);
                boolean newlineConsumed = false;
                for (ASTExpression arg : mc.args()) {
                    ASTLocation loc = (ASTLocation) arg;
                    ASTType type = sc.exprType(loc);
                    if (type instanceof ASTPrimitiveType pr && pr.type() == Type.INT) {
                        CFGAddress tmp = ctx.newAddress(Type.INT);
                        if (localIntBuffer == null)
                            localIntBuffer = new CFGVariable(symbolTable, "@intbuf",
                                    Type.RECORD, 4).getAddress();
                        CFGAddress fmt = makeStringLiteral("%d");
                        cfg.offer(new CFGMethodCallInstruction(ctx, null, SCANF,
                                List.of(fmt, localIntBuffer)));
                        cfg.offer(new CFGReadInstruction(ctx, tmp, localIntBuffer, 4, makeIntLiteral(0)));
                        write(loc, tmp);
                    } else if (type instanceof ASTPrimitiveType pr && pr.type() == Type.LONG) {
                        CFGAddress tmp = ctx.newAddress(Type.LONG);
                        if (localLongBuffer == null)
                            localLongBuffer = new CFGVariable(symbolTable, "@longbuf",
                                    Type.RECORD, 8).getAddress();
                        CFGAddress fmt = makeStringLiteral("%lld");
                        cfg.offer(new CFGMethodCallInstruction(ctx, null, SCANF,
                                List.of(fmt, localLongBuffer)));
                        cfg.offer(new CFGReadInstruction(ctx, tmp, localLongBuffer, 8, makeIntLiteral(0)));
                        write(loc, tmp);
                    } else if (type instanceof ASTArrayType at) {
                        // by semantic checking, we know this is the only argument
                        // and may consume until newline
                        CFGAddress arr = reduce(locate(loc));
                        symbolTable.addExternalMethod(GETCHAR);
                        int size = extractInt(at.ranges().getFirst().upper()) - extractInt(at.ranges().getFirst().lower());

                        CFGAddress dummy = ctx.newAddress(Type.INT);
                        CFGAddress chr = ctx.newAddress(Type.INT);
                        CFGAddress check = ctx.newAddress(Type.BOOL);
                        CFGAddress cmp = ctx.newAddress(Type.BOOL);
                        cfg.offer(new CFGLiteralInstruction(ctx, dummy, new IntLiteral(0)));

                        CFGAddress loopAddr = new CFGAddress();
                        CFGAddress incrementAddr = new CFGAddress();
                        CFGAddress cond1Addr = new CFGAddress();
                        CFGAddress cond2Addr = new CFGAddress();
                        CFGAddress exitAddr = new CFGAddress();

                        loopAddr.set(cfg.newBlock().address());
                        cfg.offer(new CFGMethodCallInstruction(ctx, chr, GETCHAR, List.of()));
                        cfg.offer(new CFGBinaryInstruction(ctx, check, BinaryOperator.LT, dummy, makeIntLiteral(size)));
                        cfg.offer(new CFGBranchInstruction(ctx, check, cond1Addr, exitAddr));
                        cond1Addr.set(cfg.newBlock().address());
                        cfg.offer(new CFGBinaryInstruction(ctx, cmp, BinaryOperator.EQ, chr, makeIntLiteral(10)));
                        cfg.offer(new CFGBranchInstruction(ctx, cmp, exitAddr, cond2Addr));
                        cond2Addr.set(cfg.newBlock().address());
                        cfg.offer(new CFGBinaryInstruction(ctx, cmp, BinaryOperator.EQ, chr, makeIntLiteral(-1)));
                        cfg.offer(new CFGBranchInstruction(ctx, cmp, exitAddr, incrementAddr));
                        incrementAddr.set(cfg.newBlock().address());
                        cfg.offer(new CFGBinaryInstruction(ctx, dummy, BinaryOperator.PLUS, dummy, makeIntLiteral(1)));
                        cfg.offer(new CFGWriteInstruction(ctx, arr, 4, dummy, chr));
                        cfg.offer(new CFGJumpInstruction(ctx, loopAddr));
                        exitAddr.set(cfg.newBlock().address());
                        cfg.offer(new CFGWriteInstruction(ctx, arr, 4, makeIntLiteral(0), dummy));

                        newlineConsumed = true;
                    } else throw new AssertionError("This should never happen.");
                }
                if (!newlineConsumed && READLN.equals(mc.id().text())) {
                    symbolTable.addExternalMethod(GETCHAR);
                    CFGAddress chr = ctx.newAddress(Type.INT);
                    CFGAddress cmp = ctx.newAddress(Type.BOOL);

                    // getchar until newline
                    CFGAddress loopAddr = new CFGAddress();
                    CFGAddress condAddr = new CFGAddress();
                    CFGAddress exitAddr = new CFGAddress();

                    loopAddr.set(cfg.newBlock().address());
                    cfg.offer(new CFGMethodCallInstruction(ctx, chr, GETCHAR, List.of()));
                    cfg.offer(new CFGBinaryInstruction(ctx, cmp, BinaryOperator.EQ, chr, makeIntLiteral(10)));
                    cfg.offer(new CFGBranchInstruction(ctx, cmp, exitAddr, condAddr));
                    condAddr.set(cfg.newBlock().address());
                    cfg.offer(new CFGBinaryInstruction(ctx, cmp, BinaryOperator.EQ, chr, makeIntLiteral(-1)));
                    cfg.offer(new CFGBranchInstruction(ctx, cmp, exitAddr, loopAddr));
                    exitAddr.set(cfg.newBlock().address());
                }
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

    private void linearize(CFGAddress addr, ASTLocation loc) {
        read(addr, locate(loc));
    }

    private void write(ASTLocation loc, CFGAddress addr) {
        write(locate(loc), addr);
    }

    private record LocationTarget(CFGAddress base, int width, CFGAddress offset, boolean simple) {}

    private void read(CFGAddress addr, LocationTarget target) {
        if (target.simple()) cfg.offer(new CFGCopyInstruction(ctx, addr, target.base()));
        else cfg.offer(new CFGReadInstruction(ctx, addr, target.base(), target.width(), target.offset()));
    }

    private void write(LocationTarget target, CFGAddress addr) {
        if (target.simple()) cfg.offer(new CFGCopyInstruction(ctx, target.base(), addr));
        else cfg.offer(new CFGWriteInstruction(ctx, target.base(), target.width(), target.offset(), addr));
    }

    private LocationTarget locate(ASTLocation loc) {
        if (loc.accesses().isEmpty())
            return new LocationTarget(symbols.get(getVar(loc.id())).getAddress(),
                    -1, null, true);

        int finalWidth = sizeof(sc.exprType(loc));
        ASTVarDecl root = getVar(loc.id());

        int lastDeref = -1;
        for (int i = loc.accesses().size(); i-- > 0; ) {
            if (loc.accesses().get(i) instanceof ASTDerefAccess) {
                lastDeref = i;
                break;
            }
        }

        CFGAddress base = symbols.get(root).getAddress();
        ASTType type = root.type();
        int width = lastDeref == -1 ? finalWidth : 8;
        CFGAddress offset = makeIntLiteral(0);

        for (int i = 0; i < loc.accesses().size(); i++) {
            while (type instanceof ASTBaseType bt)
                type = getType(bt.id());

            switch (loc.accesses().get(i)) {
                case ASTIndexAccess index -> {
                    ASTArrayType at = (ASTArrayType) type;

                    CFGAddress delta = reduceIndex(at, index);
                    int scale = sizeof(at.type()) / width;
                    cfg.offer(new CFGBinaryInstruction(ctx, delta, BinaryOperator.TIMES,
                            delta, makeIntLiteral(scale)));

                    cfg.offer(new CFGBinaryInstruction(ctx, offset, BinaryOperator.PLUS,
                            offset, delta));
                    type = at.type();
                }

                case ASTFieldAccess field -> {
                    ASTRecordType rt = (ASTRecordType) type;

                    int delta = reduceIndex(rt, field) / width;
                    cfg.offer(new CFGBinaryInstruction(ctx, offset, BinaryOperator.PLUS,
                            offset, makeIntLiteral(delta)));
                    type = fieldType(rt, field);
                }

                case ASTDerefAccess _ -> {
                    ASTPointerType pt = (ASTPointerType) type;

                    if (i > 0) {
                        CFGAddress next = ctx.newAddress(Type.POINTER);
                        cfg.offer(new CFGReadInstruction(ctx, next, base, 8, offset));
                        base = next;
                    }

                    type = pt.type();
                    offset = makeIntLiteral(0);
                    width = i < lastDeref ? 8 : finalWidth;
                }

                default -> throw new AssertionError("This should never happen.");
            }
        }

        return new LocationTarget(base, width, offset, false);
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
                for (ASTArrayRange range : at.ranges())
                    size *= extractInt(range.upper()) - extractInt(range.lower()) + 1;
                yield size;
            }

            case ASTRecordType rt -> reduceIndex(rt, null);

            default -> throw new AssertionError("This should never happen.");
        };
    }

    // in bytes
    private CFGAddress reduceIndex(ASTArrayType at, ASTIndexAccess access) {
        CFGAddress ret = makeIntLiteral(0);
        for (int i = 0; i < at.ranges().size(); i++) {
            int l = extractInt(at.ranges().get(i).lower());
            int u = extractInt(at.ranges().get(i).upper());
            CFGAddress index = linearize(access.indices().get(i));
            cfg.offer(new CFGBinaryInstruction(ctx, index, BinaryOperator.MINUS,
                    index, makeIntLiteral(l)));
            cfg.offer(new CFGBinaryInstruction(ctx, ret, BinaryOperator.TIMES,
                    ret, makeIntLiteral(u - l + 1)));
            cfg.offer(new CFGBinaryInstruction(ctx, ret, BinaryOperator.PLUS,
                    ret, index));
        }
        return ret;
    }

    // in bytes
    private int reduceIndex(ASTRecordType rt, ASTFieldAccess access) {
        String key = access == null ? "" : access.field().text();
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

    private ASTType fieldType(ASTRecordType rt, ASTFieldAccess access) {
        for (ASTVarDecl field : rt.fields())
            if (field.id().text().equals(access.field().text()))
                return field.type();
        throw new AssertionError("This should never happen.");
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

    private int extractInt(ASTExpression expr) {
        if (!(expr instanceof ASTLiteral lit)
                || !(lit.literal() instanceof IntLiteral(int i)))
            throw new AssertionError("This should never happen.");
        return i;
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
