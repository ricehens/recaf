package recaf.opt;

import recaf.general.*;
import recaf.cfg.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * A global copy propagator for a method of a SSA CFG
 */
public class CopyPropagator extends SSATransformation {

    /**
     * Creates a new copy propagator for a method
     *
     * @param ctx the CFG context
     * @param method the method to optimize
     */
    public CopyPropagator(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
    }

    private Queue<CFGInstruction> workList;

    /**
     * Runs global copy propagation on a method of a SSA CFG
     *
     * @return whether any changes were made
     */
    @Override
    public boolean apply() {
        workList = new LinkedList<>();
        fillWorkList(data.getDominatorTree().getRoot());

        Map<CFGAddress, CFGAddress> replacements = new HashMap<>();

        // assign replacements
        outer:
        while (!workList.isEmpty()) {
            CFGInstruction instruction = workList.poll();

            if (instruction instanceof CFGCopyInstruction copyInstruction) {
                CFGAddress destination = copyInstruction.address();
                CFGAddress operand = copyInstruction.operand();
                if (ctx.isGlobalVar(destination)) continue;
                if (ctx.isGlobalVar(operand)) continue;

                CFGAddress target = replacements.getOrDefault(operand, operand);
                if (target.equals(replacements.get(destination))) continue;
                replacements.put(destination, target);
                data.getUses(destination).forEach(workList::offer);
            }

            if (instruction instanceof CFGBinaryImmediateInstruction binaryImmediateInstruction) {
                CFGAddress destination = binaryImmediateInstruction.address();
                BinaryOperator op = binaryImmediateInstruction.operator();
                CFGAddress left = binaryImmediateInstruction.left();
                Literal right = binaryImmediateInstruction.right();
                if (ctx.isGlobalVar(destination)) continue;
                if (ctx.isGlobalVar(left)) continue;

                if ((op == BinaryOperator.PLUS && right.equals(new IntLiteral(0)))
                        || (op == BinaryOperator.MINUS && right.equals(new IntLiteral(0)))
                        || (op == BinaryOperator.PLUS && right.equals(new LongLiteral(0)))
                        || (op == BinaryOperator.MINUS && right.equals(new LongLiteral(0)))
                        || (op == BinaryOperator.TIMES && right.equals(new IntLiteral(1)))
                        || (op == BinaryOperator.TIMES && right.equals(new LongLiteral(1)))
                        || (op == BinaryOperator.DIVIDES && right.equals(new IntLiteral(1)))
                        || (op == BinaryOperator.DIVIDES && right.equals(new LongLiteral(1)))
                        || (op == BinaryOperator.EQ && right.equals(new BoolLiteral(true)))
                        || (op == BinaryOperator.NEQ && right.equals(new BoolLiteral(false))))
                {
                    CFGAddress target = replacements.getOrDefault(left, left);
                    if (target.equals(replacements.get(destination))) continue;
                    replacements.put(destination, target);
                    data.getUses(destination).forEach(workList::offer);
                }
            }

            if (instruction instanceof CFGPhiInstruction phiInstruction) {
                CFGAddress destination = phiInstruction.address();
                if (ctx.isGlobalVar(destination)) continue;
                for (CFGAddress operand : phiInstruction.operands())
                    if (ctx.isGlobalVar(operand)) continue outer;
                CFGAddress target = null;
                for (CFGBasicBlock source : data.getDominatorTree().getPredecessors(data.getBlock(phiInstruction))) {
                    CFGAddress operand = phiInstruction.getSources().get(source);
                    if (operand == null) continue outer;

                    CFGAddress currentTarget = replacements.getOrDefault(operand, operand);
                    if (phiInstruction.address().equals(operand) || phiInstruction.address().equals(currentTarget))
                        continue;

                    if (target == null) target = currentTarget;
                    else if (!target.equals(currentTarget)) {
                        continue outer;
                    }
                }

                if (target == null) continue;
                if (target.equals(replacements.get(destination))) continue;
                replacements.put(destination, target);
                data.getUses(destination).forEach(workList::offer);
            }
        }

        boolean changed = false;

        // put changes into effect
        for (CFGBasicBlock block : method.getBlocks()) {
            for (CFGInstruction instruction : block.getAllInstructions()) {
                for (CFGAddress operand : instruction.operands()) {
                    CFGAddress replacement = replacements.getOrDefault(operand, operand);
                    if (!replacement.equals(operand)) {
                        operand.set(replacement);
                        changed = true;
                    }
                }
            }

        }

        return changed;
    }

    /** DFS to fill worklist in preorder */
    private void fillWorkList(CFGBasicBlock node) {
        if (node == null) return;
        node.getAllInstructions().forEach(workList::offer);
        data.getDominatorTree().getImmediateDominatedNodes(node).forEach(this::fillWorkList);
    }

}
