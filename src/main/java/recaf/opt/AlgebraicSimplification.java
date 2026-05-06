package recaf.opt;

import recaf.general.*;
import recaf.cfg.*;
import recaf.utils.NumUtils;

import java.util.LinkedList;
import java.util.Queue;

public class AlgebraicSimplification extends SSATransformation {

    public AlgebraicSimplification(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
    }

    @Override
    public boolean apply() {
        boolean changed = false;
        for (CFGBasicBlock block : method.getBlocks()) {
            Queue<CFGInstruction> workList = new LinkedList<>(block.getInstructions().stream().toList());
            while (!workList.isEmpty()) {
                CFGInstruction inst = workList.poll();
                if (inst instanceof CFGBinaryImmediateInstruction bim) {
                    if (bim.operator() == BinaryOperator.PLUS) {
                        if (new NumUtils(bim.right()).isZero()) {
                            block.getInstructions().replace(inst, new CFGCopyInstruction(ctx, bim.address(), bim.left()));
                            changed = true;
                        } else if (!ctx.isGlobalVar(bim.left()) && data.getDefinition(bim.left()) instanceof CFGBinaryImmediateInstruction bim2) {
                            if (bim2.operator() == BinaryOperator.PLUS) {
                                block.getInstructions().replace(inst, new CFGBinaryImmediateInstruction(ctx, bim.address(),
                                        BinaryOperator.PLUS, bim2.left(), OptUtils.compute(BinaryOperator.PLUS, bim.right(), bim2.right())));
                                changed = true;
                            } else if (bim2.operator() == BinaryOperator.MINUS) {
                                block.getInstructions().replace(inst, new CFGBinaryImmediateInstruction(ctx, bim.address(),
                                        BinaryOperator.PLUS, bim2.left(), OptUtils.compute(BinaryOperator.MINUS, bim.right(), bim2.right())));
                                changed = true;
                            }
                        }
                    } else if (bim.operator() == BinaryOperator.MINUS) {
                        if (new NumUtils(bim.right()).isZero()) {
                            block.getInstructions().replace(inst, new CFGCopyInstruction(ctx, bim.address(), bim.left()));
                            changed = true;
                        } else if (!ctx.isGlobalVar(bim.left()) && data.getDefinition(bim.left()) instanceof CFGBinaryImmediateInstruction bim2) {
                            if (bim2.operator() == BinaryOperator.PLUS) {
                                block.getInstructions().replace(inst, new CFGBinaryImmediateInstruction(ctx, bim.address(),
                                        BinaryOperator.PLUS, bim2.left(), OptUtils.compute(BinaryOperator.MINUS, bim2.right(), bim.right())));
                                changed = true;
                            } else if (bim2.operator() == BinaryOperator.MINUS) {
                                block.getInstructions().replace(inst, new CFGBinaryImmediateInstruction(ctx, bim.address(),
                                        BinaryOperator.MINUS, bim2.left(), OptUtils.compute(BinaryOperator.PLUS, bim.right(), bim2.right())));
                                changed = true;
                            }
                        }
                    } else if (bim.operator() == BinaryOperator.TIMES) {
                        if (new NumUtils(bim.right()).isZero()) {
                            block.getInstructions().replace(inst, new CFGLiteralInstruction(ctx, bim.address(), bim.right()));
                            changed = true;
                        } else if (new NumUtils(bim.right()).asLong() == 1) {
                            block.getInstructions().replace(inst, new CFGCopyInstruction(ctx, bim.address(), bim.left()));
                            changed = true;
                        } else if (new NumUtils(bim.right()).asLong() == -1) {
                            block.getInstructions().replace(inst, new CFGUnaryInstruction(ctx, bim.address(), UnaryOperator.MINUS, bim.left()));
                            changed = true;
                        } else if (!ctx.isGlobalVar(bim.left()) && data.getDefinition(bim.left()) instanceof CFGBinaryImmediateInstruction bim2
                                && bim2.operator() == BinaryOperator.TIMES) {
                            block.getInstructions().replace(inst, new CFGBinaryImmediateInstruction(ctx, bim.address(),
                                    BinaryOperator.TIMES, bim2.left(), OptUtils.compute(BinaryOperator.TIMES, bim.right(), bim2.right())));
                            changed = true;
                        } else if (new NumUtils(bim.right()).asLong() == 2) {
                            block.getInstructions().replace(inst, new CFGBinaryInstruction(ctx, bim.address(), BinaryOperator.PLUS, bim.left(), bim.left()));
                            changed = true;
                        }
                    } else if (bim.operator() == BinaryOperator.DIVIDES) {
                        if (new NumUtils(bim.right()).asLong() == 1) {
                            block.getInstructions().replace(inst, new CFGCopyInstruction(ctx, bim.address(), bim.left()));
                            changed = true;
                        } else if (new NumUtils(bim.right()).asLong() == -1) {
                            block.getInstructions().replace(inst, new CFGUnaryInstruction(ctx, bim.address(), UnaryOperator.MINUS, bim.left()));
                            changed = true;
                        }
                    } else if (bim.operator() == BinaryOperator.MOD) {
                        if (new NumUtils(bim.right()).isUnit()) {
                            block.getInstructions().replace(inst, new CFGLiteralInstruction(ctx, bim.address(),
                                    ctx.getType(bim.address()) == Type.LONG ? new LongLiteral(0) : new IntLiteral(0)));
                            changed = true;
                        }
                    } else if (bim.operator() == BinaryOperator.EQ && bim.right() instanceof BoolLiteral boolLit) {
                        block.getInstructions().replace(inst, boolLit.value() ?
                                new CFGCopyInstruction(ctx, bim.address(), bim.left())
                                : new CFGUnaryInstruction(ctx, bim.address(), UnaryOperator.NOT, bim.left()));
                        changed = true;
                    } else if (bim.operator() == BinaryOperator.NEQ && bim.right() instanceof BoolLiteral boolLit) {
                        block.getInstructions().replace(inst, !boolLit.value() ?
                                new CFGCopyInstruction(ctx, bim.address(), bim.left())
                                : new CFGUnaryInstruction(ctx, bim.address(), UnaryOperator.NOT, bim.left()));
                        changed = true;
                    }
                } else if (inst instanceof CFGBinaryInstruction bin) {
                    if (bin.operator() == BinaryOperator.PLUS) {
                        if (!ctx.isGlobalVar(bin.right()) && data.getDefinition(bin.right()) instanceof CFGUnaryInstruction un) {
                            assert(un.operator() == UnaryOperator.MINUS);
                            block.getInstructions().replace(inst, new CFGBinaryInstruction(ctx, bin.address(),
                                    BinaryOperator.MINUS, bin.left(), un.operand()));
                            changed = true;
                        }
                    } else if (bin.operator() == BinaryOperator.MINUS) {
                        if (bin.left().equals(bin.right())) {
                            block.getInstructions().replace(inst, new CFGLiteralInstruction(ctx, bin.address(),
                                    ctx.getType(bin.address()) == Type.LONG ? new LongLiteral(0) : new IntLiteral(0)));
                            changed = true;
                        } else if (!ctx.isGlobalVar(bin.right()) && data.getDefinition(bin.right()) instanceof CFGUnaryInstruction un) {
                            assert(un.operator() == UnaryOperator.MINUS);
                            block.getInstructions().replace(inst, new CFGBinaryInstruction(ctx, bin.address(),
                                    BinaryOperator.PLUS, bin.left(), un.operand()));
                            changed = true;
                        }
                    } else if (bin.operator() == BinaryOperator.EQ || bin.operator() == BinaryOperator.LEQ || bin.operator() == BinaryOperator.GEQ) {
                        if (bin.left().equals(bin.right())) {
                            block.getInstructions().replace(inst, new CFGLiteralInstruction(ctx, bin.address(), new BoolLiteral(true)));
                            changed = true;
                        }
                    } else if (bin.operator() == BinaryOperator.NEQ || bin.operator() == BinaryOperator.LT || bin.operator() == BinaryOperator.GT) {
                        if (bin.left().equals(bin.right())) {
                            block.getInstructions().replace(inst, new CFGLiteralInstruction(ctx, bin.address(), new BoolLiteral(false)));
                            changed = true;
                        }
                    }
                } else if (inst instanceof CFGUnaryInstruction un) {
                    if (un.operator() == UnaryOperator.MINUS) {
                        CFGInstruction unDef = data.getDefinition(un.operand());
                        if (!ctx.isGlobalVar(un.operand()) && unDef instanceof CFGBinaryInstruction bin
                                && bin.operator() == BinaryOperator.MINUS) {
                            block.getInstructions().replace(inst, new CFGBinaryInstruction(ctx, un.address(),
                                    BinaryOperator.MINUS, bin.right(), bin.left()));
                            changed = true;
                        }
                    } else if (un.operator() == UnaryOperator.NOT) {
                        CFGInstruction unDef = data.getDefinition(un.operand());
                        if (!ctx.isGlobalVar(un.operand()) && unDef instanceof CFGUnaryInstruction un2) {
                            assert(un.operator() == UnaryOperator.NOT);
                            block.getInstructions().replace(inst, new CFGCopyInstruction(ctx, un.address(), un2.operand()));
                            changed = true;
                        } else if (!ctx.isGlobalVar(un.operand()) && unDef instanceof CFGBinaryInstruction bin) {
                            switch (bin.operator()) {
                                case EQ: block.getInstructions().replace(inst,
                                        new CFGBinaryInstruction(ctx, un.address(), BinaryOperator.NEQ, bin.left(), bin.right()));
                                    break;
                                case NEQ: block.getInstructions().replace(inst,
                                        new CFGBinaryInstruction(ctx, un.address(), BinaryOperator.EQ, bin.left(), bin.right()));
                                    break;
                                case LT: block.getInstructions().replace(inst,
                                        new CFGBinaryInstruction(ctx, un.address(), BinaryOperator.GEQ, bin.left(), bin.right()));
                                    break;
                                case GT: block.getInstructions().replace(inst,
                                        new CFGBinaryInstruction(ctx, un.address(), BinaryOperator.LEQ, bin.left(), bin.right()));
                                    break;
                                case LEQ: block.getInstructions().replace(inst,
                                        new CFGBinaryInstruction(ctx, un.address(), BinaryOperator.GT, bin.left(), bin.right()));
                                    break;
                                case GEQ: block.getInstructions().replace(inst,
                                        new CFGBinaryInstruction(ctx, un.address(), BinaryOperator.LT, bin.left(), bin.right()));
                                    break;
                            }
                        } else if (!ctx.isGlobalVar(un.operand()) && unDef instanceof CFGBinaryImmediateInstruction bim) {
                            switch (bim.operator()) {
                                case EQ: block.getInstructions().replace(inst,
                                        new CFGBinaryImmediateInstruction(ctx, un.address(), BinaryOperator.NEQ, bim.left(), bim.right()));
                                    break;
                                case NEQ: block.getInstructions().replace(inst,
                                        new CFGBinaryImmediateInstruction(ctx, un.address(), BinaryOperator.EQ, bim.left(), bim.right()));
                                    break;
                                case LT: block.getInstructions().replace(inst,
                                        new CFGBinaryImmediateInstruction(ctx, un.address(), BinaryOperator.GEQ, bim.left(), bim.right()));
                                    break;
                                case GT: block.getInstructions().replace(inst,
                                        new CFGBinaryImmediateInstruction(ctx, un.address(), BinaryOperator.LEQ, bim.left(), bim.right()));
                                    break;
                                case LEQ: block.getInstructions().replace(inst,
                                        new CFGBinaryImmediateInstruction(ctx, un.address(), BinaryOperator.GT, bim.left(), bim.right()));
                                    break;
                                case GEQ: block.getInstructions().replace(inst,
                                        new CFGBinaryImmediateInstruction(ctx, un.address(), BinaryOperator.LT, bim.left(), bim.right()));
                                    break;
                            }
                        }
                    }
                }
            }
        }
        return changed;
    }

}
