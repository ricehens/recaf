package recaf.opt;

import recaf.cfg.*;
import recaf.utils.DominatorTree;
import recaf.utils.ParallelCopyGroup;

/**
 * A critical edge splitter for first method of first SSA CFG
 */
public class CriticalEdgeSplitting extends SSATransformation {

    private DominatorTree<CFGBasicBlock> dt;

    /**
     * Creates first critical edge splitter
     *
     * @param ctx the CFG context
     * @param method the method
     */
    public CriticalEdgeSplitting(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
        dt = data.getDominatorTree();
    }

    /**
     * Splits the critical edges of the control flow graph within first method
     *
     * @return whether any changes were made
     */
    @Override
    public boolean apply() {
        boolean changed = false;
        changed = method.getBlocks().stream().map(this::splitOut).reduce(changed, Boolean::logicalOr);
        data.update(); dt = data.getDominatorTree();
        changed = method.getBlocks().stream().map(this::killPhi).reduce(changed, Boolean::logicalOr);
        return changed;
    }

    /**
     * Splits any outgoing critical edges of first block
     *
     * @param block the source block
     * @return whether any changes were made
     */
    private boolean splitOut(CFGBasicBlock block) {
        boolean changed = false;

        if (block.getLastInstruction() instanceof CFGBranchInstruction branch) {
            CFGBasicBlock thenBlock = ctx.getSymbolTable().getBlock(branch.thenAddr());
            CFGBasicBlock elseBlock = ctx.getSymbolTable().getBlock(branch.elseAddr());

            if (dt.getPredecessors(thenBlock).size() > 1) {
                thenBlock = split(block, thenBlock);
                changed = true;
            }

            if (dt.getPredecessors(elseBlock).size() > 1) {
                elseBlock = split(block, elseBlock);
                changed = true;
            }

            // update original branch point
            if (changed) {
                block.setLastInstruction(new CFGBranchInstruction(ctx, branch.boolAddr(), thenBlock.address(), elseBlock.address()));
            }
        }

        return changed;
    }

    /**
     * Splits the edge between two blocks
     *
     * @param block the source block
     * @param nextBlock the target block
     * @return the new block
     */
    private CFGBasicBlock split(CFGBasicBlock block, CFGBasicBlock nextBlock) {
        // insert new block
        CFGBasicBlock split = new CFGBasicBlock(ctx);
        split.setMethod(method);
        method.getBlocks().insertAfter(block, split);

        // update phi functions
        for (CFGPhiInstruction phi : nextBlock.getPhiInstructions()) {
            CFGAddress source = phi.getSources().remove(block);
            if (source != null) {
                phi.getSources().put(split, source);
            }
        }

        // add jump instruction to new block
        split.setLastInstruction(new CFGJumpInstruction(ctx, nextBlock.address()));
        return split;
    }

    /**
     * Removes unnecessary phi statements, replacing them with copies if needed
     *
     * @param block the block whose phi statements to kill if unnecessary
     * @return whether any changes were made
     */
    private boolean killPhi(CFGBasicBlock block) {
        boolean changed = false;

        if (dt.getPredecessors(block).isEmpty()) {
            changed = !block.getPhiInstructions().isEmpty();
            block.getPhiInstructions().clear();
            return changed;
        }

        if (dt.getPredecessors(block).size() > 1) return false;

        CFGBasicBlock predecessor = dt.getPredecessors(block).iterator().next();
        ParallelCopyGroup<CFGAddress> copies = new ParallelCopyGroup<>(ctx.getSymbolTable());

        for (CFGPhiInstruction phi : block.getPhiInstructions()) {
            CFGAddress dest = phi.address();
            CFGAddress src = phi.getSources().get(predecessor);
            if (src != null) {
                changed = true;
                copies.addEdge(new CFGCopyInstruction(ctx, dest, src));
            }
        }

        OptUtils.sequentialize(ctx, copies).forEach(predecessor.getInstructions()::offerLast);
        block.getPhiInstructions().clear();
        return changed;
    }

}
