package recaf.ast;

import recaf.ast.nodes.*;
import recaf.ast.nodes.ASTType;
import recaf.general.Type;
import recaf.general.*;

import java.util.*;

import static recaf.ast.ASTUtils.*;

public class SemanticChecker {

    private final Map<String, String> externalCalls;
    private final Map<String, ASTMethodDecl> methods;

    private final Set<String> globalMisc;
    private final Map<String, ASTType> globalTypes;
    private final Map<String, ASTType> globalVariables;
    private final Map<String, ASTLiteral> globalConstants;
    private final Set<ASTBaseType> promisedTypes;

    private boolean local;
    private Map<String, ASTType> localTypes;
    private Map<String, ASTType> localVariables;
    private Map<String, ASTLiteral> localConstants;

    private int loopDepth;

    private final Map<ASTType, ASTType> resolvedTypes;
    private final Map<ASTExpression, ASTType> exprTypes;

    public SemanticChecker() {
        externalCalls = new HashMap<>();
        methods = new HashMap<>();
        globalMisc = new HashSet<>();
        globalTypes = new HashMap<>();
        globalVariables = new HashMap<>();
        globalConstants = new HashMap<>();
        promisedTypes = new HashSet<>();
        resolvedTypes = new IdentityHashMap<>();
        exprTypes = new IdentityHashMap<>();
        local = false;
        loopDepth = 0;
    }

    public ASTProgram check(ASTProgram ast) {
        ASTIdentifier id = check(ast.id());
        globalMisc.add(declareId(id));
        return new ASTProgram(ast.ctx(), id,
                ast.decls().stream().map(this::dispatch).toList());
    }

    private ASTIdentifier check(ASTIdentifier ast) {
        return new ASTIdentifier(ast.ctx(), key(ast));
    }

    private ASTDeclaration dispatch(ASTDeclaration ast) {
        return switch (ast) {
            case ASTTypeDecl td -> check(td);
            case ASTVarDecl vd -> {
                ASTVarDecl ret = check(vd);
                registerVar(ret.id(), ret.type());
                yield ret;
            }
            case ASTConstDecl cd -> check(cd);
            case ASTMethodDecl md -> check(md);
            default -> throw new AssertionError("This should never happen.");
        };
    }

    private ASTTypeDecl check(ASTTypeDecl ast) {
        ASTIdentifier id = check(ast.id());
        ASTType type = dispatch(ast.type());
        registerType(id, type);
        return new ASTTypeDecl(ast.ctx(), id, type);
    }

    private ASTType dispatch(ASTType ast) {
        return switch (ast) {
            case ASTBaseType bt -> check(bt);
            case ASTArrayType at -> check(at);
            case ASTEnumType et -> check(et);
            case ASTPointerType pt -> check(pt);
            case ASTRecordType rt -> check(rt);
            case ASTPrimitiveType pr -> check(pr);
            default -> throw new AssertionError("This should never happen.");
        };
    }

    private ASTBaseType check(ASTBaseType ast) {
        if (!(globalTypes.containsKey(key(ast.id()))
                || local && localTypes.containsKey(key(ast.id())))) {
            ast.ctx().error("type " + key(ast.id()) + " not found");
        }
        return ast;
    }

    private ASTArrayType check(ASTArrayType ast) {
        ASTType type = dispatch(ast.type());
        return new ASTArrayType(ast.ctx(), type,
                ast.ranges().stream().map(this::check).toList());
    }

    private ASTArrayRange check(ASTArrayRange ast) {
        Literal lower = evalConstant(ast.lower());
        Literal upper = evalConstant(ast.upper());
        if (lower instanceof IntLiteral(int l) && upper instanceof IntLiteral(int r)) {
            if (l > r) ast.ctx().error("array range lower bound cannot exceed upper bound");
        } else ast.ctx().error("array indices must be integers");
        return new ASTArrayRange(ast.ctx(),
                new ASTLiteral(ast.lower().ctx(), lower),
                new ASTLiteral(ast.upper().ctx(), upper));
    }

    private ASTEnumType check(ASTEnumType ast) {
        List<ASTIdentifier> newIds = new ArrayList<>();
        ASTEnumType ret = new ASTEnumType(ast.ctx(), newIds);
        for (int i = 0; i < ast.members().size(); i++) {
            ASTIdentifier id = check(ast.members().get(i));
            registerVar(id, ret);
            newIds.add(id);
        }
        return ret;
    }

    private ASTPointerType check(ASTPointerType ast) {
        // TODO check promises (at beginning of every routine, say)
        promisedTypes.add(ast.type());
        return new ASTPointerType(ast.ctx(), ast.type());
    }

    private void flushPromises() {
        for (ASTBaseType promise : promisedTypes)
            check(promise);
    }

    private ASTRecordType check(ASTRecordType ast) {
        return new ASTRecordType(ast.ctx(),
                ast.fields().stream().map(this::check).toList());
    }

    private ASTPrimitiveType check(ASTPrimitiveType ast) {
        return ast;
    }

    private ASTVarDecl check(ASTVarDecl ast) {
        ASTIdentifier id = check(ast.id());
        ASTType type = dispatch(ast.type());
        return new ASTVarDecl(ast.ctx(), type, id);
    }

    private ASTConstDecl check(ASTConstDecl ast) {
        ASTIdentifier id = check(ast.id());
        ASTLiteral lit = new ASTLiteral(ast.expr().ctx(), evalConstant(ast.expr()));
        registerConst(id, lit);
        return new ASTConstDecl(ast.ctx(), id, lit);
    }

    private ASTMethodDecl check(ASTMethodDecl ast) {
        ASTIdentifier id = check(ast.id());
        Optional<ASTType> returnType = ast.returnType().map(this::dispatch);
        Optional<List<ASTVarDecl>> params = ast.params().map(
                x -> x.stream().map(this::check).toList()
        );

        String key = key(id);
        if (ast.external())
            externalCalls.put(key, id.text());
        if (!ast.internal() && existsIdentifier(key)) {
            ASTMethodDecl forward = methods.get(key);
            if (forward == null || !forward.forward())
                id.ctx().error("identifier " + key + " may not be redeclared");
            else if (returnType.isPresent() != forward.returnType().isPresent())
                id.ctx().error("forward signature mismatch for method " + key);
            else if (returnType.isPresent() && !equalTypes(returnType.get(), forward.returnType().get()))
                id.ctx().error("forward signature mismatch for method " + key);
            else if (params.isPresent() != forward.params().isPresent())
                id.ctx().error("forward signature mismatch for method " + key);
            else if (params.isPresent()) {
                if (params.get().size() != forward.params().get().size())
                    id.ctx().error("forward signature mismatch for method " + key);
                for (int i = 0; i < params.get().size(); i++) {
                    if (!equalTypes(params.get().get(i).type(), forward.params().get().get(i).type()))
                        id.ctx().error("forward signature mismatch for method " + key);
                }
            }
        }

        methods.put(key, new ASTMethodDecl(ast.ctx(),
                returnType, id, params, null, null,
                ast.forward(), ast.external(), ast.internal()));

        flushPromises();
        local = true;
        localTypes = new HashMap<>();
        localVariables = new HashMap<>();
        localConstants = new HashMap<>();

        returnType.ifPresent(type -> registerVar(id, type));
        if (params.isPresent()) {
            for (ASTVarDecl vd : params.get()) {
                ASTVarDecl vd2 = check(vd);
                registerVar(vd2.id(), vd2.type());
            }
        }

        List<ASTDeclaration> decls = ast.decls().stream().map(this::dispatch).toList();
        flushPromises();
        Optional<ASTBlock> block = ast.block().map(this::check);

        local = false;
        if (loopDepth != 0) throw new AssertionError("This should never happen.");

        return new ASTMethodDecl(ast.ctx(),
                returnType, id, params, decls, block,
                ast.forward(), ast.external(), ast.internal());
    }

    private boolean equalTypes(ASTType t1, ASTType t2) {
        ASTType r1 = resolveType(t1);
        ASTType r2 = resolveType(t2);
        ASTType unk = primitiveType(Type.UNKNOWN);
        return r1 == r2 || r1 == unk || r2 == unk;
    }

    private boolean isNumeric(ASTType type) {
        return equalTypes(type, primitiveType(Type.INT)) || equalTypes(type, primitiveType(Type.LONG));
    }

    private ASTBlock check(ASTBlock ast) {
        return new ASTBlock(ast.ctx(), ast.statements().stream().map(this::dispatch).toList());
    }

    private ASTStatement dispatch(ASTStatement ast) {
        return switch (ast) {
            case ASTAssignment as -> check(as);
            case ASTMethodCall mc -> check(mc);
            case ASTIfElse ie -> check(ie);
            case ASTForLoop fl -> check(fl);
            case ASTWhileLoop wl -> check(wl);
            case ASTRepeatLoop rl -> check(rl);
            case ASTBlock blk -> check(blk);
            default -> throw new AssertionError("This should never happen.");
        };
    }

    private ASTAssignment check(ASTAssignment ast) {
        ASTLocation left = check(ast.location());
        ASTExpression right = dispatch(ast.expr());

        // intrinsic cast
        if (equalTypes(exprType(left), primitiveType(Type.LONG))
                && equalTypes(exprType(right), primitiveType(Type.INT)))
            return new ASTAssignment(ast.ctx(), left, castLong(right));

        if (!equalTypes(exprType(left), exprType(right))) {
            ast.ctx().error("type mismatch for assignment");
            return ast;
        }

        return new ASTAssignment(ast.ctx(), left, right);
    }

    private ASTMethodCall castLong(ASTExpression expr) {
        return check(new ASTMethodCall(expr.ctx(), new ASTIdentifier(expr.ctx(), INT64), List.of(expr)));
    }

    // in case it is a method call
    private ASTExpression checkLocExpr(ASTLocation ast) {
        ASTIdentifier id = check(ast.id());
        ASTType type = getVar(id);
        if (type == null && ast.accesses().isEmpty()) {
            ASTMethodDecl md = methods.get(key(id));
            if (md != null)
                return check(new ASTMethodCall(
                        ast.ctx(), id, List.of()
                ));
        }
        return check(ast);
    }

    private ASTLocation check(ASTLocation ast) {
        ASTIdentifier id = check(ast.id());
        ASTType type = getVar(id);

        if (type == null) {
            ast.ctx().error("cannot find location " + key(id));
            return ast;
        }

        if (equalTypes(type, primitiveType(Type.UNKNOWN)))
            return ast;

        List<ASTAccessor> accesses = new ArrayList<>();
        for (ASTAccessor access : ast.accesses()) {
            switch (access) {
                case ASTIndexAccess index -> {
                    if (!(type instanceof ASTArrayType arrayType)) {
                        index.ctx().error("cannot index into non-array");
                        return ast;
                    }
                    if (arrayType.ranges().size() != index.indices().size()) {
                        index.ctx().error("expected " + arrayType.ranges().size()
                                + " indices, got " + index.indices().size());
                        return ast;
                    }
                    List<ASTExpression> indices = new ArrayList<>();
                    for (ASTExpression expr : index.indices()) {
                        ASTExpression e2 = dispatch(expr);
                        if (!equalTypes(primitiveType(Type.INT), exprType(e2))) {
                            expr.ctx().error("array indices must be of type integer");
                            return ast;
                        }
                        indices.add(e2);
                    }
                    type = resolveType(arrayType.type());
                    accesses.add(new ASTIndexAccess(index.ctx(), indices));
                }

                case ASTFieldAccess field -> {
                    if (!(type instanceof ASTRecordType recordType)) {
                        field.ctx().error("cannot read field of non-record");
                        return ast;
                    }
                    ASTIdentifier fieldId = check(field.field());

                    ASTVarDecl foundField = null;
                    for (ASTVarDecl vd : recordType.fields()) {
                        if (vd.id().text().equals(fieldId.text())) {
                            foundField = vd;
                            break;
                        }
                    }

                    if (foundField == null) {
                        field.ctx().error("cannot find field " + field.field().text());
                        return ast;
                    }

                    type = resolveType(foundField.type());
                    accesses.add(new ASTFieldAccess(field.ctx(), fieldId));
                }

                case ASTDerefAccess deref -> {
                    if (!(type instanceof ASTPointerType ptrType)) {
                        deref.ctx().error("cannot dereference non-pointer");
                        return ast;
                    }

                    type = resolveType(ptrType.type());
                    accesses.add(new ASTDerefAccess(deref.ctx()));
                }

                default -> throw new AssertionError("This should never happen.");
            }
        }

        ASTLocation ret = new ASTLocation(ast.ctx(), id, accesses);
        exprTypes.put(ret, type);
        return ret;
    }

    private ASTMethodCall check(ASTMethodCall ast) {
        ASTIdentifier id = check(ast.id());
        ASTMethodDecl md = methods.get(key(id));
        if (md == null) {
            ast.ctx().error("cannot find routine " + key(id));
            return ast;
        }

        if (md.internal()) checkNativeCall(ast);

        if (md.params().isEmpty()) {
            ASTMethodCall ret = new ASTMethodCall(ast.ctx(), id,
                    ast.args().stream().map(this::dispatch).toList());
            if (md.returnType().isPresent())
                exprTypes.put(ret, md.returnType().get());
            return ret;
        }

        if (md.params().get().size() != ast.args().size()) {
            ast.ctx().error("expected " + md.params().get().size()
                    + " arguments, got " + ast.args().size());
            return ast;
        }

        List<ASTExpression> args = new ArrayList<>();
        for (int i = 0; i < ast.args().size(); i++) {
            ASTExpression e = dispatch(ast.args().get(i));
            if (!equalTypes(exprType(e), md.params().get().get(i).type())) {
                ast.ctx().error("type mismatch for " + i + "th argument");
                return ast;
            }
            args.add(e);
        }

        ASTMethodCall ret = new ASTMethodCall(ast.ctx(), id, args);
        if (md.returnType().isPresent())
            exprTypes.put(ret, md.returnType().get());
        return ret;
    }

    private void checkNativeCall(ASTMethodCall ast) {
        switch (key(ast.id())) {
            case WRITE, WRITELN -> {
                for (ASTExpression arg : ast.args())
                    // descend into expression twice but whatever
                    if (!(exprType(dispatch(arg)) instanceof ASTBaseType))
                        arg.ctx().error(key(ast.id()) + " expects arguments of type integer/int64/boolean/string");
            }
            case NEW, DISPOSE -> {
                if (ast.args().size() != 1)
                    ast.ctx().error(key(ast.id()) + " expects exactly one argument");
                else if (!(exprType(dispatch(ast.args().getFirst())) instanceof ASTPointerType))
                    ast.args().getFirst().ctx().error(key(ast.id()) + " expects argument of pointer type");
            }
            case INTEGER, INT64 -> {
                if (ast.args().size() != 1)
                    ast.ctx().error(key(ast.id()) + " expects exactly one argument");
                else {
                    if (!isNumeric(exprType(dispatch(ast.args().getFirst()))))
                        ast.ctx().error(key(ast.id()) + " expects argument of type integer or int64");
                }
            }
            case BREAK, CONTINUE -> {
                if (loopDepth <= 0) ast.ctx().error("unexpected " + key(ast.id()) + " outside of loop");
            }
        }
    }

    private ASTIfElse check(ASTIfElse ast) {
        ASTExpression cond = dispatch(ast.cond());
        if (!equalTypes(exprType(cond), primitiveType(Type.BOOL)))
            cond.ctx().error("if condition expects type boolean");

        ASTStatement thenBlock = dispatch(ast.thenBlock());
        Optional<ASTStatement> elseBlock = ast.elseBlock().map(this::dispatch);
        return new ASTIfElse(ast.ctx(), cond, thenBlock, elseBlock);
    }

    private ASTForLoop check(ASTForLoop ast) {
        ASTIdentifier id = ast.dummy();
        ASTType idType = getVar(id);
        if (!isNumeric(idType))
            id.ctx().error("for loop expects dummy variable of type int or long");

        ASTExpression start = dispatch(ast.start());
        ASTExpression end = dispatch(ast.end());

        ASTType startType = exprType(start);
        ASTType endType = exprType(end);

        if (equalTypes(idType, primitiveType(Type.LONG))) {
            if (equalTypes(startType, primitiveType(Type.INT))) {
                start = castLong(start);
                startType = exprType(start);
            }
            if (equalTypes(endType, primitiveType(Type.INT))) {
                end = castLong(end);
                endType = exprType(end);
            }
        }

        if (!equalTypes(idType, startType))
            id.ctx().error("type mismatch for assignment to for loop lower bound");
        if (!equalTypes(idType, endType))
            id.ctx().error("type mismatch for assignment to for loop upper bound");

        loopDepth++;
        ASTStatement body = dispatch(ast.body());
        loopDepth--;

        return new ASTForLoop(ast.ctx(), id, start, end, ast.descending(), body);
    }

    private ASTWhileLoop check(ASTWhileLoop ast) {
        ASTExpression cond = dispatch(ast.cond());
        if (!equalTypes(exprType(cond), primitiveType(Type.BOOL)))
            cond.ctx().error("while condition expects type boolean");

        loopDepth++;
        ASTStatement body = dispatch(ast.body());
        loopDepth--;

        return new ASTWhileLoop(ast.ctx(), cond, body);
    }

    private ASTRepeatLoop check(ASTRepeatLoop ast) {
        loopDepth++;
        ASTStatement body = dispatch(ast.body());
        loopDepth--;

        ASTExpression cond = dispatch(ast.cond());
        if (!equalTypes(exprType(cond), primitiveType(Type.BOOL)))
            cond.ctx().error("until condition expects type boolean");

        return new ASTRepeatLoop(ast.ctx(), body, cond);
    }

    private ASTExpression dispatch(ASTExpression expr) {
        throw new RuntimeException("not implemented");
        // TODO
    }

    private ASTType primitiveType(Type type) {
        return switch (type) {
            case INT -> globalTypes.get(INTEGER);
            case LONG -> globalTypes.get(INT64);
            case BOOL -> globalTypes.get(BOOLEAN);
            case STRING -> globalTypes.get(STRING);
            case UNKNOWN -> globalTypes.get(ERROR);
            default -> throw new AssertionError("This should never happen.");
        };
    }

    // get a type that doesn't contain ASTBaseType
    private ASTType resolveType(ASTType type) {
        if (resolvedTypes.containsKey(type))
            return resolvedTypes.get(type);
        if (type instanceof ASTBaseType bt) return resolveType(getType(bt.id()));
        return type;
    }

    private void registerType(ASTIdentifier id, ASTType type) {
        String key = declareId(id);
        (local ? localTypes : globalTypes).put(key, type);
    }

    private void registerVar(ASTIdentifier id, ASTType type) {
        String key = declareId(id);
        (local ? localVariables : globalVariables).put(key, type);
    }

    private void registerConst(ASTIdentifier id, ASTLiteral lit) {
        String key = declareId(id);
        (local ? localConstants : globalConstants).put(key, lit);
    }

    private ASTType getType(ASTIdentifier id) {
        String key = key(id);
        if (local) {
            if (localTypes.containsKey(key)) return localTypes.get(key);
            if (existsIdentifier(key)) return null;
        }
        return globalTypes.get(key);
    }

    private ASTType getVar(ASTIdentifier id) {
        String key = key(id);
        if (local) {
            if (localVariables.containsKey(key)) return resolveType(localVariables.get(key));
            if (localConstants.containsKey(key))
                return primitiveType(localConstants.get(key).literal().type());
            if (existsIdentifier(key)) return null;
        }
        if (globalVariables.containsKey(key)) return resolveType(globalVariables.get(key));
        if (globalConstants.containsKey(key))
            return primitiveType(globalConstants.get(key).literal().type());
        return null;
    }

    private ASTLiteral getConst(ASTIdentifier id) {
        String key = key(id);
        if (local) {
            if (localConstants.containsKey(key)) return localConstants.get(key);
            if (existsIdentifier(key)) return null;
        }
        return globalConstants.get(key);
    }

    private String key(ASTIdentifier id) {
        return id.text().toLowerCase(Locale.ROOT);
    }

    private String declareId(ASTIdentifier id) {
        String key = key(id);
        if (existsIdentifier(key)) {
            id.ctx().error("identifier " + key + " may not be redeclared");
        }
        if (!local && LIBC_RESERVED.contains(key))
            id.ctx().error("global identifier " + key + " reserved for external routines");
        return key;
    }

    private ASTType exprType(ASTExpression expr) {
        return exprTypes.getOrDefault(expr, primitiveType(Type.UNKNOWN));
    }

    private boolean existsIdentifier(String text) {
        if (local)
            return localTypes.containsKey(text)
                || localVariables.containsKey(text)
                || localConstants.containsKey(text);
        else return globalMisc.contains(text)
                || methods.containsKey(text)
                || globalTypes.containsKey(text)
                || globalVariables.containsKey(text)
                || globalConstants.containsKey(text);
    }

    private Literal evalConstant(ASTExpression expr) {
        return switch (expr) {
            case ASTLiteral lit -> lit.literal();
            case ASTLocation loc -> {
                if (!loc.accesses().isEmpty()) yield null;
                ASTLiteral lit = getConst(loc.id());
                yield lit == null ? null : lit.literal();
            }
            case ASTUnaryExpression unary -> evalUnary(unary.op(), evalConstant(unary.expr()));
            case ASTBinaryExpression binary -> evalBinary(binary.op(),
                    evalConstant(binary.left()), evalConstant(binary.right()));
            default -> null;
        };
    }

    private Literal evalUnary(UnaryOperator op, Literal lit) {
        return switch (op) {
            case MINUS -> {
                if (lit instanceof IntLiteral(int value)) yield new IntLiteral(-value);
                if (lit instanceof LongLiteral(long value)) yield new LongLiteral(-value);
                yield null;
            }
            case NOT -> lit instanceof BoolLiteral(boolean value)

                    ? new BoolLiteral(!value) : null;
        };
    }

    private Literal evalBinary(BinaryOperator op, Literal left, Literal right) {
        if (left instanceof BoolLiteral(boolean b1) && right instanceof BoolLiteral(boolean b2)) {
            return switch (op) {
                case EQ -> new BoolLiteral(b1 == b2);
                case NEQ -> new BoolLiteral(b1 != b2);
                case AND -> new BoolLiteral(b1 && b2);
                case OR -> new BoolLiteral(b1 || b2);
                default -> null;
            };
        }

        if (left instanceof IntLiteral(int l) && right instanceof IntLiteral(int r)) {
            return switch (op) {
                case PLUS -> new IntLiteral(l + r);
                case MINUS -> new IntLiteral(l - r);
                case TIMES -> new IntLiteral(l * r);
                case DIVIDES -> r == 0 ? null : new IntLiteral(l / r);
                case MOD -> r == 0 ? null : new IntLiteral(l % r);
                case LT -> new BoolLiteral(l < r);
                case GT -> new BoolLiteral(l > r);
                case LEQ -> new BoolLiteral(l <= r);
                case GEQ -> new BoolLiteral(l >= r);
                case EQ -> new BoolLiteral(l == r);
                case NEQ -> new BoolLiteral(l != r);
                default -> null;
            };
        }

        Long l = extractLongValue(left);
        Long r = extractLongValue(right);
        if (l == null || r == null) return null;
        return switch (op) {
            case PLUS -> new LongLiteral(l + r);
            case MINUS -> new LongLiteral(l - r);
            case TIMES -> new LongLiteral(l * r);
            case DIVIDES -> r == 0 ? null : new LongLiteral(l / r);
            case MOD -> r == 0 ? null : new LongLiteral(l % r);
            case LT -> new BoolLiteral(l < r);
            case GT -> new BoolLiteral(l > r);
            case LEQ -> new BoolLiteral(l <= r);
            case GEQ -> new BoolLiteral(l >= r);
            case EQ -> new BoolLiteral(l.equals(r));
            case NEQ -> new BoolLiteral(!l.equals(r));
            default -> null;
        };
    }

    private Long extractLongValue(Literal lit) {
        if (lit instanceof IntLiteral(int value)) return (long) value;
        if (lit instanceof LongLiteral(long value)) return value;
        return null;
    }

}
