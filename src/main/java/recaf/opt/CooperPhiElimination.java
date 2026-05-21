package recaf.opt;

import recaf.cfg.*;
import recaf.utils.ParallelCopyGroup;

import java.util.*;

/**
 * A SSA-to-CFG converter for a particular method,
 * by making copies as suggested in
 * Chapter 9 of Cooper et al.'s Engineering a Compiler, 3rd edition
 * (as opposed to splitting critical edges).
 */
// @Deprecated
public class CooperPhiElimination extends SSATransformation {

    /**
     * Constructs a new CooperPhiEliminator instance
     *
     * @param method the method to convert
     */
    public CooperPhiElimination(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
    }

    /**
     * Destroys all phi instructions in a method,
     * converting the SSA CFG to a normal CFG ready for assembly conversion.
     *
     * @return true if successful
     */
    @Override
    public boolean apply() {
        isolatePhi();
        eliminatePhi();
        return true;
    }

    /**
     * Isolates each phi function by making copies before and after
     */
    private void isolatePhi() {
        for (CFGBasicBlock block : method.getBlocks()) {
            Map<CFGBasicBlock, ParallelCopyGroup<CFGAddress>> pre = new HashMap<>();
            ParallelCopyGroup<CFGAddress> post = new ParallelCopyGroup<>(ctx.getSymbolTable());

            // for each x1 <- phi(x0, y1)
            for (CFGPhiInstruction phi : block.getPhiInstructions()) {
                // x1 <- x1' at beginning of current block
                CFGAddress oldDest = phi.address();
                CFGAddress newDest = ctx.getSymbolTable().newNode(oldDest);
                post.addEdge(new CFGCopyInstruction(ctx, oldDest, newDest));
                oldDest.set(newDest);

                Map<CFGBasicBlock, CFGAddress> map = phi.getSources();
                for (CFGBasicBlock source : map.keySet()) {
                    pre.computeIfAbsent(source, k -> new ParallelCopyGroup<>(ctx.getSymbolTable()));
                    // x0' <- x0 at end of old block
                    CFGAddress oldSrc = map.get(source);
                    CFGAddress newSrc = ctx.getSymbolTable().newNode(oldSrc);
                    pre.get(source).addEdge(new CFGCopyInstruction(ctx, newSrc, oldSrc));
                    oldSrc.set(newSrc);
                }

                // now is x1' <- phi(x0', y1')
            }

            OptUtils.reverse(OptUtils.sequentialize(ctx, post)).forEach(block.getInstructions()::offerFirst);
            pre.keySet().forEach(source ->
                    OptUtils.sequentialize(ctx, pre.get(source)).forEach(source.getInstructions()::offerLast)
            );
        }
    }

    /**
     * Eliminates each phi function by converting to parallel copy groups in preceding blocks
     */
    private void eliminatePhi() {
        for (CFGBasicBlock block : method.getBlocks()) {
            Map<CFGBasicBlock, ParallelCopyGroup<CFGAddress>> pre = new HashMap<>();
            for (CFGPhiInstruction phi : block.getPhiInstructions()) {
                Map<CFGBasicBlock, CFGAddress> map = phi.getSources();
                CFGAddress dest = phi.address();
                for (CFGBasicBlock source : map.keySet()) {
                    pre.computeIfAbsent(source, k -> new ParallelCopyGroup<>(ctx.getSymbolTable()));
                    CFGAddress src = map.get(source);
                    pre.get(source).addEdge(new CFGCopyInstruction(ctx, dest, src));
                }
            }
            block.getPhiInstructions().clear();

            pre.keySet().forEach(source ->
                    OptUtils.sequentialize(ctx, pre.get(source)).forEach(source.getInstructions()::offerLast)
            );
        }
    }

}
