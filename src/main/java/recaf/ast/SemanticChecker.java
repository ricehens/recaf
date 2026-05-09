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
    private final Map<ASTType, ASTType> checkedTypes;

    private boolean local;
    private Map<String, ASTType> localTypes;
    private Map<String, ASTType> localVariables;
    private Map<String, ASTLiteral> localConstants;

    private int loopDepth;

    private final Map<ASTType, ASTType> resolvedTypes;
    private final Map<ASTExpression, ASTType> exprTypes;

    // note: enums values are reduced to integers but their types still exist
    // during linearization, just treat any ASTEnumType as integer
    public SemanticChecker() {
        externalCalls = new HashMap<>();
        methods = new HashMap<>();
        globalMisc = new HashSet<>();
        globalTypes = new HashMap<>();
        globalVariables = new HashMap<>();
        globalConstants = new HashMap<>();
        promisedTypes = new HashSet<>();
        checkedTypes = new IdentityHashMap<>();
        resolvedTypes = new IdentityHashMap<>();
        exprTypes = new IdentityHashMap<>();
        local = false;
        loopDepth = 0;
    }

    public ASTProgram check(ASTProgram ast) {
        ASTIdentifier id = check(ast.id());
        globalMisc.add(declareId(id));

        ASTProgram ret = new ASTProgram(ast.ctx(), id,
                ast.decls().stream().map(this::dispatch).toList());

        for (ASTMethodDecl md : methods.values())
            if (md.forward())
                md.ctx().error("forward definition has no implementation");

        ast.ctx().setSemanticChecker(this);

        return ret;
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
        if (checkedTypes.containsKey(ast))
            return checkedTypes.get(ast);

        ASTType ret = switch (ast) {
            case ASTBaseType bt -> check(bt);
            case ASTArrayType at -> check(at);
            case ASTEnumType et -> check(et);
            case ASTPointerType pt -> check(pt);
            case ASTRecordType rt -> check(rt);
            case ASTPrimitiveType pr -> check(pr);
            default -> throw new AssertionError("This should never happen.");
        };

        checkedTypes.put(ast, ret);
        checkedTypes.put(ret, ret);
        return ret;
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
        if (lower == null)
            ast.ctx().error("array range lower bound must be compile-time evaluable");
        if (upper == null)
            ast.ctx().error("array range upper bound must be compile-time evaluable");
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
        promisedTypes.add(ast.type());
        return new ASTPointerType(ast.ctx(), ast.type());
    }

    private void flushPromises() {
        for (ASTBaseType promise : promisedTypes)
            if (!checkedTypes.containsKey(promise))
                checkedTypes.put(promise, check(promise));
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
        ASTType type = resolveType(dispatch(ast.type()));
        return new ASTVarDecl(ast.ctx(), type, id);
    }

    private ASTConstDecl check(ASTConstDecl ast) {
        ASTIdentifier id = check(ast.id());
        Literal lit = evalConstant(ast.expr());
        if (lit == null)
            ast.ctx().error("constant must be compile-time evaluable");
        ASTLiteral astLit = new ASTLiteral(ast.expr().ctx(), lit);
        registerConst(id, astLit);
        return new ASTConstDecl(ast.ctx(), id, astLit);
    }

    private ASTMethodDecl check(ASTMethodDecl ast) {
        ASTIdentifier id = new ASTIdentifier(ast.ctx(),
                ast.external() ? ast.id().text() : key(ast.id()));
        Optional<ASTType> returnType = ast.returnType().map(this::dispatch);
        Optional<List<ASTVarDecl>> params = ast.params().map(
                x -> x.stream().map(this::check).toList()
        );

        String key = key(id);
        if (ast.external())
            externalCalls.put(key, id.text());
        else if (LIBC_RESERVED.contains(key))
            id.ctx().error("identifier " + key + " is reserved for external routines");
        if (params.isEmpty() && !ast.external() && !ast.internal())
            ast.ctx().error("undeclared parameters are only permitted for external routines");
        if (!ast.internal() && existsIdentifier(key)) {
            ASTMethodDecl forward = methods.get(key);
            if (forward == null || !forward.forward())
                id.ctx().error("identifier " + key + " may not be redeclared");
            else if (returnType.isPresent() != forward.returnType().isPresent())
                id.ctx().error("forward signature mismatch for routine " + key);
            else if (returnType.isPresent() && !equalTypes(returnType.get(), forward.returnType().get()))
                id.ctx().error("forward signature mismatch for routine " + key);
            else if (params.isPresent() != forward.params().isPresent())
                id.ctx().error("forward signature mismatch for routine " + key);
            else if (params.isPresent()) {
                if (params.get().size() != forward.params().get().size())
                    id.ctx().error("forward signature mismatch for routine " + key);
                else for (int i = 0; i < params.get().size(); i++) {
                    if (!equalTypes(params.get().get(i).type(), forward.params().get().get(i).type()))
                        id.ctx().error("forward signature mismatch for routine " + key);
                }
            }
        }

        flushPromises();
        local = true;
        localTypes = new HashMap<>();
        localVariables = new HashMap<>();
        localConstants = new HashMap<>();

        returnType = returnType.map(this::resolveType);
        methods.put(key, new ASTMethodDecl(ast.ctx(),
                returnType, id, params, null, null,
                ast.forward(), ast.external(), ast.internal()));

        if (returnType.isPresent()) {
            if (returnType.get() instanceof ASTArrayType)
                returnType.get().ctx().error("functions may not return arrays");
            if (returnType.get() instanceof ASTRecordType)
                returnType.get().ctx().error("functions may not return records");
            ASTVarDecl rv = check(new ASTVarDecl(ast.ctx(), returnType.get(), id));
            registerVar(rv.id(), rv.type());
        }

        if (params.isPresent()) {
            for (ASTVarDecl vd : params.get()) {
                ASTVarDecl vd2 = check(vd);
                if (vd2.type() instanceof ASTArrayType)
                    vd2.ctx().error("passing arrays to routines is currently unsupported");
                if (vd2.type() instanceof ASTRecordType)
                    vd2.ctx().error("passing records to routines is currently unsupported");
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

    private boolean equalTypesStrict(ASTType t1, ASTType t2) {
        return resolveType(t1) == resolveType(t2);
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
        if (!(check(ast.location()) instanceof ASTLocation left)) {
            ast.ctx().error("cannot assign to rvalue");
            return ast;
        }
        ASTExpression right = dispatch(ast.expr());

        // intrinsic cast
        if (equalTypesStrict(exprType(left), primitiveType(Type.LONG))
                && equalTypesStrict(exprType(right), primitiveType(Type.INT)))
            return new ASTAssignment(ast.ctx(), left, castLong(right));

        if (!equalTypes(exprType(left), exprType(right))) {
            ast.ctx().error("type mismatch for assignment");
// System.out.println(exprType(left));
// System.out.println(exprType(right));
            return ast;
        }

        return new ASTAssignment(ast.ctx(), left, right);
    }

    private ASTMethodCall castLong(ASTExpression expr) {
        return check(new ASTMethodCall(expr.ctx(), new ASTIdentifier(expr.ctx(), INT64), List.of(expr)));
    }

    // checked + placed in exprtypes iff reduced, i.e. iff return type is not ASTLocation
    private ASTExpression reduce(ASTLocation loc) {
        String key = key(loc.id());
        if (local) {
            if (localVariables.containsKey(key)) {
                if (localVariables.get(key) instanceof ASTEnumType et)
                    return reduceEnum(loc, et);
                return loc;
            }

            if (localConstants.containsKey(key))
                return check(new ASTLiteral(loc.ctx(), localConstants.get(key).literal()));
        }

        if (globalVariables.containsKey(key)) {
            if (globalVariables.get(key) instanceof ASTEnumType et)
                return reduceEnum(loc, et);
            return loc;
        }

        if (globalConstants.containsKey(key))
            return check(new ASTLiteral(loc.ctx(), globalConstants.get(key).literal()));

        loc.ctx().error("cannot find location " + key);
        return loc;
    }

    // checked + placed in exprtypes iff reduced, i.e. iff return type is not ASTLocation
    private ASTExpression reduceEnum(ASTLocation loc, ASTEnumType et) {
        for (int i = 0; i < et.members().size(); i++) {
            if (key(loc.id()).equals(key(et.members().get(i)))) {
                ASTLiteral ret = new ASTLiteral(loc.ctx(), new IntLiteral(i));
                exprTypes.put(ret, et);
                return ret;
            }
        }
        return loc;
    }

    // in case it is a method call
    private ASTExpression checkLocationExpression(ASTLocation ast) {
        ASTIdentifier id = check(ast.id());
        ASTType type = getVar(id);
        if (type == null && ast.accesses().isEmpty()) {
            ASTMethodDecl md = methods.get(key(id));
            if (md != null) {
                if (md.returnType().isEmpty())
                    ast.ctx().error("procedure call is not expression");
                return check(new ASTMethodCall(
                        ast.ctx(), id, List.of()
                ));
            }
        }
        return check(ast);
    }

    private ASTExpression check(ASTLocation ast) {
        ASTExpression red = reduce(ast);
        if (!(red instanceof ASTLocation)) {
            if (!ast.accesses().isEmpty())
                ast.ctx().error("cannot access member of rvalue");
            // already placed into exprtypes
            return red;
        }

        ASTIdentifier id = check(ast.id());
        ASTType type = getVar(id);

        if (type == null) {
            ast.ctx().error("cannot find location " + key(id));
            return ast;
        }

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
        ASTIdentifier id = new ASTIdentifier(ast.id().ctx(),
                externalCalls.getOrDefault(key(ast.id()), key(ast.id())));
        ASTMethodDecl md = methods.get(key(id));
        if (md == null) {
            ast.ctx().error("cannot find routine " + key(id));
            return ast;
        }

        if (MAIN.equals(key(id)))
            ast.ctx().error("cannot call main routine");
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
            ASTType type = exprType(e);
            if (equalTypesStrict(md.params().get().get(i).type(), primitiveType(Type.LONG))
                    && equalTypesStrict(type, primitiveType(Type.INT))) {
                e = castLong(e);
                type = exprType(e);
            }
            if (!equalTypes(type, md.params().get().get(i).type())) {
                ast.ctx().error("type mismatch for " + i + "th argument");
                return ast;
            }
            args.add(e);
        }

        ASTMethodCall ret = new ASTMethodCall(ast.ctx(), id, args);
        if (md.returnType().isPresent())
            exprTypes.put(ret, resolveType(md.returnType().get()));
        return ret;
    }

    private void checkNativeCall(ASTMethodCall ast) {
        switch (key(ast.id())) {
            case WRITE, WRITELN -> {
                for (ASTExpression arg : ast.args())
                    // descend into expression twice but whatever
                    if (!(exprType(dispatch(arg)) instanceof ASTPrimitiveType))
                        arg.ctx().error(key(ast.id()) + " expects arguments of type integer/int64/boolean/string");
            }
            case NEW, DISPOSE -> {
                if (ast.args().size() != 1)
                    ast.ctx().error(key(ast.id()) + " expects exactly one argument");
                else {
                    ASTExpression ptr = dispatch(ast.args().getFirst());
                    if (NEW.equals(key(ast.id())) && !(ptr instanceof ASTLocation))
                        ast.args().getFirst().ctx().error(key(ast.id()) + " expects location as argument");
                    if (!(exprType(ptr) instanceof ASTPointerType))
                        ast.args().getFirst().ctx().error(key(ast.id()) + " expects argument of pointer type");
                }
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
        if (!(check(ast.dummy()) instanceof ASTLocation dummy)) {
            ast.dummy().ctx().error("rvalue cannot be for loop dummy");
            return ast;
        }

        if (!dummy.accesses().isEmpty()) {
            ast.dummy().ctx().error("for loop dummy variable must be simple");
        }

        ASTType dummyType = exprType(dummy);
        if (!isNumeric(dummyType))
            dummy.ctx().error("for loop dummy variable must have type int or long");

        ASTExpression start = dispatch(ast.start());
        ASTExpression end = dispatch(ast.end());

        ASTType startType = exprType(start);
        ASTType endType = exprType(end);

        if (equalTypesStrict(dummyType, primitiveType(Type.LONG))) {
            if (equalTypesStrict(startType, primitiveType(Type.INT))) {
                start = castLong(start);
                startType = exprType(start);
            }
            if (equalTypesStrict(endType, primitiveType(Type.INT))) {
                end = castLong(end);
                endType = exprType(end);
            }
        }

        if (!equalTypes(dummyType, startType))
            start.ctx().error("type mismatch for assignment to for loop lower bound");
        if (!equalTypes(dummyType, endType))
            end.ctx().error("type mismatch for assignment to for loop upper bound");

        loopDepth++;
        ASTStatement body = dispatch(ast.body());
        loopDepth--;

        return new ASTForLoop(ast.ctx(), dummy, start, end, ast.descending(), body);
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
        return switch (expr) {
            case ASTLocation loc -> checkLocationExpression(loc);
            case ASTMethodCall mc -> check(mc);
            case ASTLiteral lit -> check(lit);
            case ASTUnaryExpression un -> check(un);
            case ASTBinaryExpression bin -> check(bin);
            default -> throw new AssertionError("This should never happen.");
        };
    }

    private ASTLiteral check(ASTLiteral ast) {
        exprTypes.put(ast, primitiveType(ast.literal().type()));
        return ast;
    }

    private ASTUnaryExpression check(ASTUnaryExpression ast) {
        ASTExpression expr = dispatch(ast.expr());
        ASTType type = exprType(expr);

        switch (ast.op()) {
            case NOT -> {
                if (!equalTypes(type, primitiveType(Type.BOOL)))
                    ast.ctx().error("unary operator `not` expects type boolean");
            }
            case MINUS -> {
                if (!isNumeric(type))
                    ast.ctx().error("unary minus expects type integer or int64");
            }
        }

        ASTUnaryExpression ret = new ASTUnaryExpression(ast.ctx(), ast.op(), expr);
        exprTypes.put(ret, type);
        return ret;
    }

    private ASTBinaryExpression check(ASTBinaryExpression ast) {
        ASTExpression left = dispatch(ast.left());
        ASTExpression right = dispatch(ast.right());
        ASTType leftType = exprType(left);
        ASTType rightType = exprType(right);

        if (equalTypesStrict(leftType, primitiveType(Type.LONG))
                && equalTypesStrict(rightType, primitiveType(Type.INT))) {
            right = castLong(right);
            rightType = exprType(right);
        }

        if (equalTypesStrict(leftType, primitiveType(Type.INT))
                && equalTypesStrict(rightType, primitiveType(Type.LONG))) {
            left = castLong(left);
            leftType = exprType(left);
        }

        if (!equalTypes(leftType, rightType)) {
            ast.ctx().error("left-right type mismatch on binary operator");
            return ast;
        }

        switch (ast.op().getType()) {
            case ARITH_OP -> {
                if (!isNumeric(leftType)) {
                    ast.ctx().error("arithmetic binary operator expected type integer or int64");
                    return ast;
                }
            }
            case REL_OP -> {
                if (!isNumeric(leftType)) {
                    ast.ctx().error("comparison binary operator expected type integer or int64");
                    return ast;
                }
            }
            case COND_OP -> {
                if (!equalTypes(leftType, primitiveType(Type.BOOL))) {
                    ast.ctx().error("logical binary operator expected type boolean");
                    return ast;
                }
            }
            case EQ_OP -> {
                if (leftType instanceof ASTArrayType)
                    ast.ctx().error("cannot compare equality between arrays");
                if (leftType instanceof ASTRecordType)
                    ast.ctx().error("cannot compare equality between records");
            }
        }

        ASTBinaryExpression ret = new ASTBinaryExpression(ast.ctx(), ast.op(), left, right);
        exprTypes.put(ret, ast.op().getType() == BinaryOperator.BinOpType.ARITH_OP
                ? leftType : primitiveType(Type.BOOL));
        return ret;
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

        ASTType ret = type;
        if (type instanceof ASTBaseType bt) {
            ASTType ref = getType(bt.id());
            if (ref == null) {
                // bt.ctx().error("cannot find type " + key(bt.id()));
                ret = primitiveType(Type.UNKNOWN);
            } else ret = resolveType(ref);
        }

        resolvedTypes.put(type, ret);
        return ret;
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
        if (local)
            if (localTypes.containsKey(key)) return localTypes.get(key);
        return globalTypes.get(key);
    }

    private ASTType getVar(ASTIdentifier id) {
        String key = key(id);
        if (local) {
            if (localVariables.containsKey(key)) return resolveType(localVariables.get(key));
            if (localConstants.containsKey(key))
                return primitiveType(localConstants.get(key).literal().type());
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
            if (localVariables.containsKey(key)) return null;
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

    /**
     * Returns the ASTType of the given expression.
     * @param expr an expression in the AST generated by this pass
     * @return the type of the expression
     */
    ASTType exprType(ASTExpression expr) {
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
