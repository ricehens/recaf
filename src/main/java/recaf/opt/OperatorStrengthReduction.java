package recaf.opt;

import recaf.general.*;
import recaf.opt.OptUtils.RegionConstant;
import recaf.cfg.*;

import java.util.*;

/**
 * Implements operator strength reduction, as described in section 10.7.2 of Cooper et al.
 */
public class OperatorStrengthReduction extends SSATransformation {

    private final boolean lftr;

    /**
     * Creates a operator strength reducer for a method
     *
     * @param ctx    the CFG context
     * @param method the method to optimize
     */
    public OperatorStrengthReduction(CFGContext ctx, CFGMethod method, boolean lftr) {
        super(ctx, method);
        this.lftr = lftr;
    }

    private boolean changed;
    private Set<CFGAddress> visited;
    private int nextNum;
    private Map<CFGAddress, Integer> num;
    private Map<CFGAddress, Integer> low;
    private Stack<CFGAddress> stack;
    private Map<CFGAddress, CFGAddress> header;
    private Map<OperationKey, CFGAddress> operationLookup;
    private Map<LitMultOperationKey, CFGAddress> miniOperationLookup;
    private Map<CFGAddress, OperationKey> lftrEdges;

    /**
     * Runs operator strength reduction on a method of a SSA CFG
     *
     * @return whether any changes were made
     */
    @Override
    public boolean apply() {
        changed = false;
        visited = new HashSet<>();
        nextNum = 0;
        num = new HashMap<>();
        low = new HashMap<>();
        stack = new Stack<>();
        header = new HashMap<>();
        operationLookup = new HashMap<>();
        miniOperationLookup = new HashMap<>();
        lftrEdges = new HashMap<>();

        osr();
        if (lftr) lftr();

        return changed;
    }

    /** Runs operator strength reduction */
    private void osr() {
// System.out.println("Welcome to OSR");
        nextNum = 0;

        Queue<CFGAddress> workList = new LinkedList<>(data.getLocalVars());
        while (!workList.isEmpty()) {
            CFGAddress n = workList.poll();
            if (!visited.contains(n)) {
                dfs(n);
            }
        }
// System.out.println("headers: " + header);
    }

    /** Runs linear function test replacement on the method */
    private void lftr() {
        for (CFGBasicBlock block : method.getBlocks()) {
            if (block.getLastInstruction() instanceof CFGBranchInstruction branch) {
                CFGInstruction conditional = data.getDefinition(branch.boolAddr());
                if (conditional != null)
                    lftr(conditional);
            }
        }
    }

    /**
     * Runs LFTR on an eligible conditional
     *
     * @param conditional the conditional
     * */
    private void lftr(CFGInstruction conditional) {
        CFGBasicBlock block = data.getBlock(conditional);
        BinaryOperator op;
        CFGAddress iv;
        RegionConstant rc;

        if (conditional instanceof CFGBinaryInstruction bin) {
            op = bin.operator();

            if (isInductionVariable(bin.left()) && isRegionConstant(bin.right(), bin.left())) {
                iv = bin.left();
                rc = new RegionConstant(bin.right());
            } else if (isInductionVariable(bin.right()) && isRegionConstant(bin.left(), bin.right())) {
                iv = bin.right();
                rc = new RegionConstant(bin.left());
                op = OptUtils.reverseComparison(op);
            } else return;
        } else if (conditional instanceof CFGBinaryImmediateInstruction bim) {
            op = bim.operator();
            iv = bim.left();
            rc = new RegionConstant(bim.right());
            if (!isInductionVariable(iv)) return;
        } else return;

        OperationKey key = lftrEdges.get(iv);
        if (key == null) return;
        // now we want to replace conditional with
        // conditional.address() := key.iv op(or reverse) (rc under key.op key.rc)

        // deal with sign issues
        if (key.op == Op.TIMES) {
            if (!key.rc.isLiteral) return; // can't determine sign
            if (OptUtils.isNegative(key.rc.literal)) op = OptUtils.reverseComparison(op);
        }

        RegionConstant lifted = lift(rc, key);
        CFGInstruction newInst = createBinaryInstruction(op, conditional.address(), key.iv, lifted);
        block.getInstructions().replace(conditional, newInst);
// System.out.println("Lifted " + conditional.address() + " to " + key.iv);
        changed = true;
        data.updateUseDef();
        header.put(newInst.address(), header.get(iv));
        lftr(newInst);
    }

    /**
     * Lifts a region
     * constant through the operation given by an operation key
     *
     * @param rc the region
     *           constant
     * @param key the operation key
     * @return the lifted region
     * constant
     * */
    private RegionConstant lift(RegionConstant rc, OperationKey key) {
        if (key.rc.isNull) {
            // must be long cast
            if (rc.isLiteral) {
                return new RegionConstant(compute(key.op, rc.literal, null));
            }
        } if (key.rc.isLiteral) {
            if (rc.isLiteral) {
                return new RegionConstant(compute(key.op, rc.literal, key.rc.literal));
            }
        } else {
            if (rc.isLiteral) {
                CFGAddress lift = key.op == Op.LONG ? ctx.getSymbolTable().addVar(Type.LONG) : ctx.getSymbolTable().newNode(key.rc.address);
                CFGInstruction newInst = switch (key.op) {
                    case PLUS -> new CFGBinaryImmediateInstruction(ctx, lift, BinaryOperator.PLUS, key.rc.address, rc.literal);
                    case MINUS ->
                            new CFGBinaryImmediateInstruction(ctx, lift, BinaryOperator.PLUS,
                                    multiply(ctx.getType(key.rc.address) == Type.LONG ? new LongLiteral(-1) : new IntLiteral(-1),
                                            key.rc.address), rc.literal);
                    case TIMES -> new CFGBinaryInstruction(ctx, lift, BinaryOperator.TIMES, key.rc.address, rc.address);
                    case LONG -> new CFGCastInstruction(ctx, lift, Type.LONG, rc.address);
                };
                CFGBasicBlock insertionBlock = findDominatedBlock(rc.address, key.rc);

                insertionBlock.offer(newInst);
                changed = true;
                data.updateUseDef();
                return new RegionConstant(lift);
            }
        }

        CFGAddress lift = key.op == Op.LONG ? ctx.getSymbolTable().addVar(Type.LONG) : ctx.getSymbolTable().newNode(rc.address);
        CFGInstruction newInst = createInstruction(key.op, lift, rc.address, key.rc);
        CFGBasicBlock insertionBlock = findDominatedBlock(rc.address, key.rc);

        insertionBlock.offer(newInst);
        changed = true;
        data.updateUseDef();
        return new RegionConstant(lift);
    }

    /**
     * DFS for Tarjan's SCC
     *
     * @param n the current node
     * */
    private void dfs(CFGAddress n) {
        num.put(n, nextNum++);
        visited.add(n);
        low.put(n, num.get(n));
        stack.push(n);

        if (data.getDefinition(n) != null) {
            for (CFGAddress o : data.getDefinition(n).operands()) {
                if (!visited.contains(o)) {
                    dfs(o);
                    low.put(n, Math.min(low.get(n), low.get(o)));
                }
                if (num.get(o) < num.get(n) && stack.contains(o)) {
                    low.put(n, Math.min(low.get(n), num.get(o)));
                }
            }
        }

        // if n is the header of the SCC
        if (low.get(n).equals(num.get(n))) {
            Set<CFGAddress> scc = new HashSet<>();

            // pop all members of the SCC from the stack
            CFGAddress x;
            do {
                x = stack.pop();
                scc.add(x);
            } while (!n.equals(x));

            process(scc);
        }
    }

    /**
     * Processes a strongly connected component N
     *
     * @param N the strongly connected component
     * */
    private void process(Set<CFGAddress> N) {
// System.out.println("Processing SCC " + N);
        if (N.size() == 1) {
            CFGAddress n = N.iterator().next();

            OperationKey candidate = isCandidate(n);
            if (candidate != null) {
// System.out.println("\n*** " + n + " is a candidate");
                replace(n, candidate);
            }
        } else {
            classifyIV(N);
        }
    }

    /**
     * Classify induction variables
     *
     * @param N the strongly connected component
     * */
    private void classifyIV(Set<CFGAddress> N) {
        boolean isIV = true;

        for (CFGAddress n : N) {
            if (!isValidUpdateForIV(n, N)) {
                isIV = false;
                break;
            }
        }

        if (isIV) {
            CFGAddress headerNode = nodeWithMinRPONumber(N);
            for (CFGAddress n : N) {
                header.put(n, headerNode);
            }
        } else {
            for (CFGAddress n : N) {
                OperationKey candidate = isCandidate(n);
                if (candidate != null) {
// System.out.println("**** " + n + " is a candidate");
                    replace(n, candidate);
                }
            }
        }
    }

    /**
     * Replaces a node with its reduced form
     *
     * @param n the node to replace
     * @param key the reduction key
     */
    private void replace(CFGAddress n, OperationKey key) {
// System.out.printf("==== Replacing %s by (%s, %s, %s)%n", n, key.op, key.iv, key.rc);
        CFGAddress result = reduce(key);
// System.out.printf("Found replacement for %s: %s%n", n, result);

        // replace n with a copy from result
        CFGInstruction instruction = data.getDefinition(n);
        CFGBasicBlock block = data.getBlock(instruction);

        CFGCopyInstruction copyInst = new CFGCopyInstruction(ctx, n, result);
        block.getInstructions().replace(instruction, copyInst);
// System.out.printf("Replaced \"%s\" with \"%s\"%n", instruction, copyInst);
        data.updateUseDef();

        header.put(n, header.get(key.iv));

        changed = true;
    }

    /**
     * Creates or returns address to a reduction given by the key
     *
     * @param key the key
     * @return the address to the result
     * */
    private CFGAddress reduce(OperationKey key) {
// System.out.printf("==== Reducing (%s, %s, %s)%n", key.op, key.iv, key.rc);
        CFGAddress result = operationLookup.get(key);
// if (result != null) System.out.printf("Found cached result for (%s, %s, %s): %s%n", key.op, key.iv, key.rc, result);

        if (result == null) {
            result = key.op == Op.LONG ? ctx.getSymbolTable().addVar(Type.LONG) : ctx.getSymbolTable().newNode(key.iv);
            operationLookup.put(key, result);
            lftrEdges.put(CFGAddress.clone(key.iv), new OperationKey(key.op, result, key.rc));

            // newDef <- Clone(iv, result)
            CFGInstruction ivInstruction = data.getDefinition(key.iv);
// System.out.printf("Cloning definition of %s to %s: %s%n", key.iv, result, ivInstruction);
            CFGInstruction newDef = ivInstruction.copy();
            newDef.address().set(result);
// System.out.println("New definition: " + newDef);

            CFGBasicBlock block = data.getBlock(ivInstruction);
            // insert after current instruction
            if (ivInstruction instanceof CFGPhiInstruction phi)
                block.getPhiInstructions().insertAfter(phi, (CFGPhiInstruction) newDef);
            else block.getInstructions().insertAfter(ivInstruction, newDef);
// System.out.printf("Inserted new definition after \"%s\"%n", ivInstruction);
            data.updateUseDef();

            header.put(result, header.get(key.iv));

            for (CFGAddress o : newDef.operands()) {
                OperationKey newKey = new OperationKey(key.op, o, key.rc);
                if (header.get(o) != null && header.get(o).equals(header.get(key.iv))) {
                    o.set(reduce(newKey));
                } else if (key.op == Op.TIMES || key.op == Op.LONG || newDef instanceof CFGPhiInstruction) {
                    o.set(apply(newKey));
                }
            }

            // deal with "literal" operands
            if (key.op == Op.TIMES && newDef instanceof CFGBinaryImmediateInstruction bim) {
                CFGInstruction newerDef = key.rc.isLiteral
                        ? new CFGBinaryImmediateInstruction(ctx, bim.address(), bim.operator(), bim.left(), compute(key.op, bim.right(), key.rc.literal))
                        : new CFGBinaryInstruction(ctx, bim.address(), bim.operator(), bim.left(), multiply(bim.right(), key.rc.address));
                block.getInstructions().replace(newDef, newerDef);
                data.updateUseDef();
            } else if (key.op == Op.LONG && newDef instanceof CFGBinaryImmediateInstruction bim) {
                CFGInstruction newerDef = new CFGBinaryImmediateInstruction(ctx, bim.address(), bim.operator(), bim.left(), compute(key.op, bim.right(), null));
                block.getInstructions().replace(newDef, newerDef);
            }

// System.out.printf("Transformed definition into %s%n", newDef);
        }

        return result;
    }

    /**
     * Creates or returns address to a application given by the key
     *
     * @param key the key
     * @return the address to the result
     * */
    private CFGAddress apply(OperationKey key) {
// System.out.printf("======== Applying (%s, %s, %s)%n", key.op, key.iv, key.rc);
        CFGAddress result = operationLookup.get(key);
// if (result != null) System.out.printf("Found cached result for (%s, %s, %s): %s%n", key.op, key.iv, key.rc, result);

        if (result == null) {
            if (isInductionVariable(key.iv) && (key.rc.isLiteral || isRegionConstant(key.rc.address, key.iv))) {
                result = reduce(key);
            } /*else if ((key.op == BinaryOperator.PLUS || key.op == BinaryOperator.TIMES) && !key.rc.isLiteral && isInductionVariable(key.rc.address) && isRegionConstant(key.iv, key.rc.address)) {
                result = reduce(new OperationKey(key.op, key.rc.address, new RegionConstant(key.iv)));
            } */else {
                result = key.op == Op.LONG ? ctx.getSymbolTable().addVar(Type.LONG) : ctx.getSymbolTable().newNode(key.iv);
                operationLookup.put(key, result);
                CFGBasicBlock block = findDominatedBlock(key.iv, key.rc);
// System.out.printf("Found dominated block %s for %s and %s%n", block.address(), key.iv, key.rc);
                CFGInstruction newInst = createInstruction(key.op, result, key.iv, key.rc);
                block.offer(newInst);
// System.out.printf("Inserted %s at end of %s%n", newInst, block.address());
                data.updateUseDef();
                header.put(result, null);
            }
        }

        return result;
    }

    // apply but where o1 is a constant
    /**
     * Gets address to product of literal and a variable
     * @param o1 the literal
     * @param o2 the variable
     * @return the address to the product
     * */
    private CFGAddress multiply(Literal o1, CFGAddress o2) {
        CFGAddress result = miniOperationLookup.get(new LitMultOperationKey(o1, o2));

        if (result == null) {
            result = ctx.getSymbolTable().newNode(o2);
            miniOperationLookup.put(new LitMultOperationKey(o1, o2), result);
            CFGBasicBlock block = findDominatedBlock(o2, new RegionConstant(o1));
            CFGInstruction newInst = (o1.equals(new IntLiteral(-1)) || o1.equals(new LongLiteral(-1L)))
                    ? new CFGUnaryInstruction(ctx, result, UnaryOperator.MINUS, o2)
                    : new CFGBinaryImmediateInstruction(ctx, result, BinaryOperator.TIMES, o2, o1);
            block.offer(newInst);
// System.out.printf("Inserted %s at end of %s%n", newInst, block.address());
            data.updateUseDef();
            header.put(result, null);
        }

        return result;
    }

    private Literal compute(Op op, Literal o1, Literal o2) {
        if (op == Op.LONG) {
            if (o1 instanceof IntLiteral i1)
                return new LongLiteral(i1.value());
            if (o1 instanceof LongLiteral l1)
                return l1;
            throw new AssertionError("This should never happen.");
        }

        return OptUtils.compute(switch (op) {
            case PLUS -> BinaryOperator.PLUS;
            case MINUS -> BinaryOperator.MINUS;
            case TIMES -> BinaryOperator.TIMES;
            default -> throw new AssertionError("This should never happen.");
        }, o1, o2);
    }

    /**
     * Creates a binary instruction with the given operands
     *
     * @param op the operator
     * @param result the result address
     * @param o1 the first operand
     * @param o2 the second operand
     *
     */
    private CFGInstruction createBinaryInstruction(BinaryOperator op, CFGAddress result, CFGAddress o1, RegionConstant o2) {
        return o2.isLiteral ? new CFGBinaryImmediateInstruction(ctx, result, op, o1, o2.literal)
                : new CFGBinaryInstruction(ctx, result, op, o1, o2.address);
    }

    /**
     * Creates a instruction with the given operands
     *
     * @param op the operator
     * @param result the result address
     * @param o1 the first operand
     * @param o2 the second operand
     *
     */
    private CFGInstruction createInstruction(Op op, CFGAddress result, CFGAddress o1, RegionConstant o2) {
        return switch (op) {
            case PLUS -> o2.isLiteral ? new CFGBinaryImmediateInstruction(ctx, result, BinaryOperator.PLUS, o1, o2.literal)
                    : new CFGBinaryInstruction(ctx, result, BinaryOperator.PLUS, o1, o2.address);
            case MINUS -> o2.isLiteral ? new CFGBinaryImmediateInstruction(ctx, result, BinaryOperator.MINUS, o1, o2.literal)
                    : new CFGBinaryInstruction(ctx, result, BinaryOperator.MINUS, o1, o2.address);
            case TIMES -> o2.isLiteral ? new CFGBinaryImmediateInstruction(ctx, result, BinaryOperator.TIMES, o1, o2.literal)
                    : new CFGBinaryInstruction(ctx, result, BinaryOperator.TIMES, o1, o2.address);
            case LONG -> new CFGCastInstruction(ctx, result, Type.LONG, o1);
        };
    }

    /**
     * Finds a block that is dominated by the definitions of both operands.
     *
     * @param o1 the first operand
     * @param o2 the second operand (as RegionConstant)
     * @return a suitable block
     */
    private CFGBasicBlock findDominatedBlock(CFGAddress o1, RegionConstant o2) {
        CFGInstruction o1Def = data.getDefinition(o1);
        CFGBasicBlock o1Block = o1Def != null ? data.getBlock(o1Def) : data.getDominatorTree().getRoot();

        if (o2.isLiteral) {
            return o1Block;
        } else {
            CFGInstruction o2Def = data.getDefinition(o2.address);
            CFGBasicBlock o2Block = o2Def != null ? data.getBlock(o2Def) : data.getDominatorTree().getRoot();

            // both operands must have definitions that dominate the header block
            // so one must dominate the other
            if (o1Block.equals(data.getDominatorTree().lca(o1Block, o2Block)))
                return o2Block;
            else return o1Block;
        }
    }

   /**
     * Determines if a node is a candidate for strength reduction.
     * A candidate operation is of the form:
     * x <- c * i
     * x <- i * c
     * x <- c + i
     * x <- i + c
     * x <- i - c
     * x <- long(i)
     * where i is an induction variable and c is a region
     * constant.
     *
     * @param n the node to check
     * @return the valid operation key for the candidate, or null
     */
    private OperationKey isCandidate(CFGAddress n) {
        if (ctx.isGlobalVar(n))
            return null;

        CFGInstruction instruction = data.getDefinition(n);
        if (instruction == null)
            return null;

        if (instruction instanceof CFGReadInstruction ||
            instruction instanceof CFGMethodCallInstruction)
            return null;

        if (instruction instanceof CFGBinaryInstruction bin) {
            CFGAddress left = bin.left();
            CFGAddress right = bin.right();

            if (ctx.isGlobalVar(left) || ctx.isGlobalVar(right))
                return null;

            switch (bin.operator()) {
                case PLUS:
                    if ((isInductionVariable(left) && isRegionConstant(right, left)))
                        return new OperationKey(Op.PLUS, left, new RegionConstant(right));
                    if (isInductionVariable(right) && isRegionConstant(left, right))
                        return new OperationKey(Op.PLUS, right, new RegionConstant(left));
                    break;
                case TIMES:
                    if ((isInductionVariable(left) && isRegionConstant(right, left)))
                        return new OperationKey(Op.TIMES, left, new RegionConstant(right));
                    if (isInductionVariable(right) && isRegionConstant(left, right))
                        return new OperationKey(Op.TIMES, right, new RegionConstant(left));
                    break;
                case MINUS:
                    if (isInductionVariable(left) && isRegionConstant(right, left))
                        return new OperationKey(Op.MINUS, left, new RegionConstant(right));
            }
        } else if (instruction instanceof CFGBinaryImmediateInstruction bim) {
            CFGAddress left = bim.left();

            if (ctx.isGlobalVar(left))
                return null;

            if ((bim.operator() == BinaryOperator.PLUS || bim.operator() == BinaryOperator.MINUS || bim.operator() == BinaryOperator.TIMES) && isInductionVariable(left))
                return new OperationKey(switch (bim.operator()) {
                    case PLUS -> Op.PLUS;
                    case MINUS -> Op.MINUS;
                    case TIMES -> Op.TIMES;
                    default -> throw new AssertionError("This should never happen.");
                }, left, new RegionConstant(bim.right()));
        } else if (instruction instanceof CFGUnaryInstruction unary) {
            CFGAddress operand = unary.operand();

            if (ctx.isGlobalVar(operand))
                return null;

            if (unary.operator() == UnaryOperator.MINUS && isInductionVariable(operand))
                return new OperationKey(Op.TIMES, operand,
                        new RegionConstant(ctx.getType(operand) == Type.LONG ? new LongLiteral(-1) : new IntLiteral(-1)));
        } else if (instruction instanceof CFGCastInstruction cast) {
            CFGAddress operand = cast.operand();

            if (ctx.isGlobalVar(operand))
                return null;

            if (cast.type() == Type.LONG && isInductionVariable(operand))
                return new OperationKey(Op.LONG, operand, new RegionConstant());
        }

        return null;
    }

    /**
     * Checks if the given node represents an induction variable.
     * A node is an induction variable if its header field is non-null.
     *
     * @param n the node to check
     * @return true if the node is an induction variable
     */
    private boolean isInductionVariable(CFGAddress n) {
        if (ctx.isGlobalVar(n)) {
            return false;
        }

        return header.containsKey(n) && header.get(n) != null;
    }

    /**
     * Checks if the given node represents a region
     * constant with respect to an induction variable.
     * A region
     * constant is either a literal constant or a loop-invariant value.
     *
     * @param n the node to check
     * @param iv the induction variable
     * @return true if the node is a region
     * constant
     */
    private boolean isRegionConstant(CFGAddress n, CFGAddress iv) {
// System.out.printf("==== isRegionConstant(%s, %s)%n", n, iv);
        if (ctx.isGlobalVar(n)) {
            return false;
        }

        CFGAddress ivHeader = header.get(iv);
// System.out.printf("header(%s) = %s%n", iv, ivHeader);
        if (ivHeader == null)
            return false;

        CFGInstruction ivHeaderInstruction = data.getDefinition(ivHeader);
        if (ivHeaderInstruction == null)
            return false;

        CFGBasicBlock ivHeaderBlock = data.getBlock(ivHeaderInstruction);

        if (method.getParams().contains(n))
            return true;

        CFGInstruction nDef = data.getDefinition(n);
        if (nDef == null)
            return false;

        /*
        if (nDef instanceof CFGMethodCallInstruction || nDef instanceof CFGReadInstruction)
            return false;
         */

        CFGBasicBlock nDefBlock = data.getBlock(nDef);
        return !nDefBlock.equals(ivHeaderBlock) && data.getDominatorTree().dominates(nDefBlock, ivHeaderBlock);
    }

    /**
     * Determines if a node is a valid update for an induction variable.
     * An induction variable update is one of:
     * 1) an induction variable plus a region
     * constant
     * 2) an induction variable minus a region
     * constant
     * 3) a phi-function
     * 4) a register-to-register copy from another induction variable
     *
     * @param n the node to check
     * @return true if the node is a valid update for an induction variable
     */
    private boolean isValidUpdateForIV(CFGAddress n, Set<CFGAddress> scc) {
        if (ctx.isGlobalVar(n)) {
            return false;
        }

        CFGInstruction instruction = data.getDefinition(n);
        if (instruction == null)
            return false;

        if (instruction instanceof CFGReadInstruction ||
            instruction instanceof CFGMethodCallInstruction)
            return false;

        if (instruction instanceof CFGPhiInstruction)
            return true;

        if (instruction instanceof CFGCopyInstruction copy) {
            if (ctx.isGlobalVar(copy.operand()))
                return false;
            return scc.contains(copy.operand());
        }

        if (instruction instanceof CFGBinaryInstruction bin) {
            CFGAddress left = bin.left();
            CFGAddress right = bin.right();

            if (ctx.isGlobalVar(left) || ctx.isGlobalVar(right))
                return false;

            if (bin.operator() == BinaryOperator.PLUS)
                return (scc.contains(left) && isRegionConstant(right, left))
                        || (isRegionConstant(left, right) && scc.contains(right));
            if (bin.operator() == BinaryOperator.MINUS)
                return scc.contains(left) && isRegionConstant(right, left);
        } else if (instruction instanceof CFGBinaryImmediateInstruction bim) {
            CFGAddress left = bim.left();

            if (ctx.isGlobalVar(left))
                return false;

            return (bim.operator() == BinaryOperator.PLUS || bim.operator() == BinaryOperator.MINUS)
                    && scc.contains(left);
        }

        return false;
    }

    /**
     * Determines the node from an SCC with the minimum reverse postorder number.
     * @param nodes the SCC
     * @return the node described above
     */
    private CFGAddress nodeWithMinRPONumber(Set<CFGAddress> nodes) {
        CFGAddress lowestNode = null;
        int highestPostOrder = -1;

        for (CFGAddress node : nodes) {
            CFGBasicBlock block = data.getBlock(data.getDefinition(node));
            Integer postOrder = data.getDominatorTree().getPostorderIndex(block);
            if (postOrder != null && postOrder > highestPostOrder) {
                highestPostOrder = postOrder;
                lowestNode = node;
            }
        }

// System.out.printf("%s(%d) = header of %s%n", lowestNode, highestPostOrder, nodes);
        return lowestNode;
    }

    private enum Op { PLUS, MINUS, TIMES, LONG }

    /**
     * A key representing a transformation between two induction variables
     *
     * @param op the operator
     * @param iv the induction variable
     * @param rc the region
     *           constant
     */
    private record OperationKey(Op op, CFGAddress iv, RegionConstant rc) {}

    /**
     * Represents a simple operation key with a literal
     *
     * @param lit the literal
     * @param rc the region
     */
    private record LitMultOperationKey(Literal lit, CFGAddress rc) {}

}
