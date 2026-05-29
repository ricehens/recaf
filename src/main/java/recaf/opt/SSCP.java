package recaf.opt;

import recaf.common.*;
import recaf.cfg.*;

import java.util.*;

/**
 * A sparse simple constant propagator for a method of a SSA CFG.
 * See section 9.3.6 of Cooper et al.
 */
public class SSCP extends SSATransformation {

    /**
     * Creates a new SSCP for a method
     *
     * @param ctx the CFG context
     * @param method the method to optimize
     */
    public SSCP(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
    }

    private final Literal UNKNOWN = new UnknownValue();

    private Queue<CFGAddress> workList;
    private Map<CFGAddress, Literal> value;
    private Set<CFGAddress> cannotBeKnown;
    private Map<CFGAddress, Literal> globalLiterals;

    /**
     * Runs SSCP on a method of a SSA CFG
     *
     * @return whether any changes were made
     */
    @Override
    public boolean apply() {
        workList = new LinkedList<>();
        value = new HashMap<>();
        cannotBeKnown = new HashSet<>();

        // prepare worklist
        computeGlobalLiterals();
        for (CFGAddress global : ctx.getGlobalVars()) {
            if (globalLiterals.containsKey(global))
                value.put(global, globalLiterals.get(global));
            else cannotBeKnown.add(global);
            workList.offer(global);
        }
        for (CFGAddress addr : data.getLocalVars()) {
            CFGInstruction def = data.getDefinition(addr);
            if (def == null) {
                // must be method argument
                cannotBeKnown.add(addr);
                workList.offer(addr);
                continue;
            }

            if (def instanceof CFGLiteralInstruction lit) {
                value.put(addr, lit.literal());
                workList.offer(addr);
                continue;
            }
            if (def instanceof CFGReadInstruction
                    || def instanceof CFGMethodCallInstruction) {
                cannotBeKnown.add(addr);
                workList.offer(addr);
                continue;
            }
            for (CFGAddress operand : def.operands()) {
                if (ctx.isGlobalVar(operand)) {
                    cannotBeKnown.add(addr);
                    workList.offer(addr);
                    break;
                }
            }
            value.put(addr, UNKNOWN);
        }
// System.out.println(value);

        // iterate
        boolean changed = false;
        Map<CFGInstruction, CFGInstruction> simpleReplacements = new HashMap<>();
        while (!workList.isEmpty()) {
            CFGAddress address = workList.poll();
// System.out.println("Processing " + address + " : " + (cannotBeKnown.contains(address) ? "BAD" : value.get(address)));
            mid:
            for (CFGInstruction use : data.getUses(address)) {
                CFGAddress m = use.address();
                if (m == null) continue;
                if (cannotBeKnown.contains(m)) continue;
// System.out.println("Looking at " + m);

                Literal t = value.get(m); // UNKNOWN if not known
                Literal val = UNKNOWN;

                if (use instanceof CFGPhiInstruction phi) {
                    for (CFGAddress source : phi.getSources().values()) {
                        if (cannotBeKnown.contains(source)) {
                            cannotBeKnown.add(m);
                            workList.offer(m);
                            continue mid;
                        }
// System.out.println("Processing source " + source + " of phi instruction + " + use);
                        if (value.containsKey(source) && !value.get(source).equals(UNKNOWN)) {
                            if (val.equals(UNKNOWN)) val = value.get(source);
                            else if (!val.equals(value.get(source))) {
                                cannotBeKnown.add(m);
                                workList.offer(m);
                                continue mid;
                            }
                        }
                    }
                } else if (use instanceof CFGBinaryImmediateInstruction bim) {
                    if (cannotBeKnown.contains(bim.left())) {
                        cannotBeKnown.add(m);
                        workList.offer(m);
                        continue;
                    }
                    if (!value.containsKey(bim.left()) || value.get(bim.left()).equals(UNKNOWN)) {
                        if ((bim.operator() == BinaryOperator.TIMES && (bim.right().equals(new IntLiteral(0)) || bim.right().equals(new LongLiteral(0))))
                                || (bim.operator() == BinaryOperator.AND && bim.right().equals(new BoolLiteral(false)))
                                || (bim.operator() == BinaryOperator.OR && bim.right().equals(new BoolLiteral(true)))) {
                            val = bim.right();
                        }
                    } else val = evaluate(bim.operator(), value.get(bim.left()), bim.right());
                } else if (use instanceof CFGBinaryInstruction bin) {
                        if (bin.operator() == BinaryOperator.TIMES && !cannotBeKnown.contains(bin.right()) && value.containsKey(bin.right())
                                && (value.get(bin.right()).equals(new IntLiteral(0))
                                || value.get(bin.right()).equals(new LongLiteral(0)))) {
                            val = value.get(bin.right());
                        } else if (
                            bin.operator() == BinaryOperator.TIMES && !cannotBeKnown.contains(bin.left()) && value.containsKey(bin.left())
                                    && (value.get(bin.left()).equals(new IntLiteral(0))
                                    || value.get(bin.left()).equals(new LongLiteral(0)))
                        ) {
                            val = value.get(bin.left());
                        } else {
                            if (cannotBeKnown.contains(bin.left())
                                    || cannotBeKnown.contains(bin.right())) {
                                cannotBeKnown.add(m);
                                workList.offer(m);
                                continue;
                            }
                            if (value.containsKey(bin.left()) && value.containsKey(bin.right())
                                    && !value.get(bin.left()).equals(UNKNOWN) && !value.get(bin.right()).equals(UNKNOWN)) {
                                val = evaluate(bin.operator(), value.get(bin.left()), value.get(bin.right()));
                            }
                        }
                } else if (use instanceof CFGCastInstruction cast) {
                    if (cannotBeKnown.contains(cast.operand())) {
                        cannotBeKnown.add(m);
                        workList.offer(m);
                        continue;
                    }
                    Literal lit = value.get(cast.operand());
                    long imm = lit instanceof IntLiteral ? ((IntLiteral) lit).value() : ((LongLiteral) lit).value();
                    val = cast.type() == Type.INT ? new IntLiteral((int) imm) : new LongLiteral(imm);
                } else if (use instanceof CFGCopyInstruction copy) {
                    if (cannotBeKnown.contains(copy.operand())) {
                        cannotBeKnown.add(m);
                        workList.offer(m);
                        continue;
                    }
                    val = value.get(copy.operand());
                } else if (use instanceof CFGUnaryInstruction unary) {
                    if (cannotBeKnown.contains(unary.operand())) {
                        cannotBeKnown.add(m);
                        workList.offer(m);
                        continue;
                    }
                    val = evaluate(unary.operator(), value.get(unary.operand()));
                }

                // note literal instruction should be impossible here

                if (ctx.isGlobalVar(m)) {
                    if (!val.equals(UNKNOWN)) {
                        simpleReplacements.put(use, new CFGLiteralInstruction(ctx, m, val));
                    }
                } else {
                    value.put(m, val);
                    if (!t.equals(val)) {
                        workList.offer(m);
// System.out.println("Changed " + m + " from " + t + " to " + val);
                    }
                }
            }
        }
// System.out.println(value);
// System.out.println(cannotBeKnown);
// System.out.println(getReplacement(new CFGAddress(3, 5)));
        // effect changes
        for (CFGBasicBlock block : method.getBlocks()) {
            Queue<CFGInstruction> instructions = new LinkedList<>(block.getAllInstructions());
            while (!instructions.isEmpty()) {
                CFGInstruction inst = instructions.poll();
                if (simpleReplacements.containsKey(inst)) {
                    changed = true;
                    block.getInstructions().replace(inst, simpleReplacements.get(inst));
                } else if (inst instanceof CFGBinaryImmediateInstruction
                        || inst instanceof CFGCastInstruction
                        || inst instanceof CFGCopyInstruction
                        || inst instanceof CFGUnaryInstruction) {
                    Literal replacement = getReplacement(inst.address());
                    if (replacement != null) {
                        changed = true;
                        block.getInstructions().replace(inst, new CFGLiteralInstruction(ctx, inst.address(), replacement));
                    }
                } else if (inst instanceof CFGPhiInstruction phi) {
                    Literal replacement = getReplacement(inst.address());
                    if (replacement != null) {
                        changed = true;
                        block.getPhiInstructions().remove(phi);
                        block.getInstructions().offerFirst(new CFGLiteralInstruction(ctx, inst.address(), replacement));
                    }
                } else if (inst instanceof CFGBinaryInstruction bin) {
                    Literal replacement;
                    if ((replacement = getReplacement(bin.address())) != null) {
                        changed = true;
                        block.getInstructions().replace(inst, new CFGLiteralInstruction(ctx, bin.address(), replacement));
                    } else if ((replacement = getReplacement(bin.right())) != null) {
                        changed = true;
                        block.getInstructions().replace(inst, new CFGBinaryImmediateInstruction(ctx, bin.address(), bin.operator(), bin.left(), replacement));
                    } else if ((replacement = getReplacement(bin.left())) != null) {
                        if (bin.operator() == BinaryOperator.PLUS || bin.operator() == BinaryOperator.TIMES || bin.operator() == BinaryOperator.EQ || bin.operator() == BinaryOperator.NEQ) {
                            changed = true;
                            block.getInstructions().replace(inst, new CFGBinaryImmediateInstruction(ctx, bin.address(), bin.operator(), bin.right(), replacement));
                        }
                    }
                } else if (inst instanceof CFGBranchInstruction branch) {
                    Literal replacement;
                    if ((replacement = getReplacement(branch.boolAddr())) != null) {
                        changed = true;
                        BoolLiteral boolLit = (BoolLiteral) replacement;
                        block.setLastInstruction(new CFGJumpInstruction(ctx, boolLit.value() ? branch.thenAddr() : branch.elseAddr()));
                    }
                }
            }
        }

        return changed;
    }

    /**
     * Finds the value of a variable
     *
     * @param address the address to the variable
     * @return null if the value cannot be known,
     * UNKNOWN if the value is currently unknown, or the value otherwise
     */
    private Literal getReplacement(CFGAddress address) {
        if (cannotBeKnown.contains(address)) {
            return null;
        }
        Literal known = value.get(address);
        return known == null || known.equals(UNKNOWN) ? null : known;
    }

    /**
     * Evaluates a binary operation between two literals.
     *
     * @param op the operator
     * @param left the left operand
     * @param right the right operand
     * @return the literal result
     */
    private Literal evaluate(BinaryOperator op, Literal left, Literal right) {
        if (left instanceof IntLiteral) {
            int l = ((IntLiteral) left).value();
            int r = ((IntLiteral) right).value();
            return switch (op) {
                case PLUS -> new IntLiteral(l + r);
                case MINUS -> new IntLiteral(l - r);
                case TIMES -> new IntLiteral(l * r);
                case DIVIDES -> new IntLiteral(r == 0 ? 0 : l / r);
                case MOD -> new IntLiteral(r == 0 ? 0 : l % r);
                case EQ -> new BoolLiteral(l == r);
                case NEQ -> new BoolLiteral(l != r);
                case LT -> new BoolLiteral(l < r);
                case GT -> new BoolLiteral(l > r);
                case LEQ -> new BoolLiteral(l <= r);
                case GEQ -> new BoolLiteral(l >= r);
                default -> throw new AssertionError("This should never happen.");
            };
        } else if (left instanceof LongLiteral) {
            long l = ((LongLiteral) left).value();
            long r = ((LongLiteral) right).value();
            return switch (op) {
                case PLUS -> new LongLiteral(l + r);
                case MINUS -> new LongLiteral(l - r);
                case TIMES -> new LongLiteral(l * r);
                case DIVIDES -> new LongLiteral(r == 0L ? 0L : l / r);
                case MOD -> new LongLiteral(r == 0L ? 0L : l % r);
                case EQ -> new BoolLiteral(l == r);
                case NEQ -> new BoolLiteral(l != r);
                case LT -> new BoolLiteral(l < r);
                case GT -> new BoolLiteral(l > r);
                case LEQ -> new BoolLiteral(l <= r);
                case GEQ -> new BoolLiteral(l >= r);
                default -> throw new AssertionError("This should never happen.");
            };
        } else if (left instanceof BoolLiteral) {
            boolean l = ((BoolLiteral) left).value();
            boolean r = ((BoolLiteral) right).value();
            return switch (op) {
                case EQ -> new BoolLiteral(l == r);
                case NEQ -> new BoolLiteral(l != r);
                case AND -> new BoolLiteral(l && r);
                case OR -> new BoolLiteral(l || r);
                default -> throw new AssertionError("This should never happen.");
            };
        }
        throw new AssertionError("This should never happen.");
    }

    /**
     * Evaluates a unary operation on a literal
     *
     * @param op the operator
     * @param operand the operand
     * @return the literal result
     */
    private Literal evaluate(UnaryOperator op, Literal operand) {
        if (operand instanceof IntLiteral) {
            return new IntLiteral(-((IntLiteral) operand).value());
        } else if (operand instanceof LongLiteral) {
            return new LongLiteral(-((LongLiteral) operand).value());
        } else if (operand instanceof BoolLiteral) {
            return new BoolLiteral(!((BoolLiteral) operand).value());
        }
        throw new AssertionError("This should never happen.");
    }

    /**
     * A class representing a value that cannot be known
     */
    private static class UnknownValue implements Literal {

        @Override
        public Type type() {
            return Type.UNKNOWN;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof UnknownValue;
        }

        @Override
        public String toString() {
            return "UNKNOWN";
        }
    }

    private void computeGlobalLiterals() {
        globalLiterals = new HashMap<>();
        Set<CFGAddress> alreadyDefined = new HashSet<>();
        for (CFGMethod method : method.ctx().getProgram().getMethods()) {
            for (CFGBasicBlock block : method.getBlocks()) {
                for (CFGInstruction instruction : block.getInstructions()) {
                    CFGAddress address = instruction.address();
                    if (address == null || !ctx.isGlobalVar(address))
                        continue;

                    if (!(instruction instanceof CFGLiteralInstruction lit)) {
                        globalLiterals.remove(address);
                    } else if (!alreadyDefined.contains(address)) {
                        globalLiterals.put(address, lit.literal());
                    } else if (globalLiterals.containsKey(address)
                            && !lit.literal().equals(globalLiterals.get(address))) {
                        globalLiterals.remove(address);
                    }

                    alreadyDefined.add(address);
                }
            }
        }
    }

}
