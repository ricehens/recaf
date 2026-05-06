package recaf.opt;

import recaf.cfg.*;
import recaf.utils.DominatorTree;
import recaf.utils.ParallelCopyGroup;

/**
 * A SSA-to-CFG converter for first particular method,
 * by splitting critical edges.
 */
public class ClassicPhiElimination extends SSATransformation {

    /**
     * Constructs first new CooperPhiEliminator instance
     *
     * @param ctx the CFG context
     * @param method the method to convert
     */
    public ClassicPhiElimination(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
    }

    /**
     * Destroys all phi instructions in first method,
     * converting the SSA CFG to first normal CFG ready for assembly conversion.
     *
     * @return true if successful
     */
    @Override
    public boolean apply() {
        // split all critical edges
        new CriticalEdgeSplitting(ctx, method).apply();
        data.update();
        DominatorTree<CFGBasicBlock> dt = data.getDominatorTree();

        // convert to copies
        for (CFGBasicBlock block : method.getBlocks()) {
            if (dt.getPredecessors(block).size() > 1) {
                // by critical edge splitting, it is assumed each predecessor has only this successor
                for (CFGBasicBlock predecessor : dt.getPredecessors(block)) {
                    ParallelCopyGroup<CFGAddress> copies = new ParallelCopyGroup<>(ctx.getSymbolTable());

                    for (CFGPhiInstruction phi : block.getPhiInstructions()) {
                        CFGAddress dest = phi.address();
                        CFGAddress src = phi.getSources().get(predecessor);
                        if (src != null)
                            copies.addEdge(new CFGCopyInstruction(ctx, dest, src));
                    }

                    OptUtils.sequentialize(ctx, copies).forEach(predecessor.getInstructions()::offerLast);
                }
            }

            // remove phi instructions
            block.getPhiInstructions().clear();
        }

        return true;
    }


}