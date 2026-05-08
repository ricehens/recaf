package recaf.opt;

import recaf.cfg.*;
import recaf.utils.PostDominatorTree;

import java.util.*;

/**
 * A global dead code eliminator for first method of first SSA CFG
 */
public class DeadCodeElimination extends SSATransformation {

   /**
     * Creates first new dead code eliminator for first method
     * @param ctx the CFG context
     * @param method the method to optimize
     */
    public DeadCodeElimination(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
    }

    private PostDominatorTree<CFGBasicBlock> pdt;
    private Queue<CFGInstruction> workList;
    private Set<CFGInstruction> alive = new HashSet<>();

    /**
     * Runs global dead code elimination on first method of first SSA CFG
     *
     * @return whether any changes were made
     */
    @Override
    public boolean apply() {
        pdt = new PostDominatorTree<>(method.getBlocks(), new CFGBasicBlock(ctx));
/*
System.out.println(method);
for (CFGBasicBlock block : method.getBlocks()) {
    System.out.printf("%s -> %s\n", block.address(), pdt.getPostDominanceFrontier(block) == null ? null : pdt.getPostDominanceFrontier(block).stream().map(CFGBasicBlock::address).toList());
}
 */
        workList = new LinkedList<>();
        fillWorkList(data.getDominatorTree().getRoot());

        boolean changed = cleanPhi();
        mark();
        changed |= sweep();
        changed |= cleanPhi();
        return changed;
    }

    /**
     * Removes unused phi sources
     * @return whether any changes were made
     */
    private boolean cleanPhi() {
        boolean changed = false;
        for (CFGBasicBlock block : method.getBlocks()) {
            for (CFGPhiInstruction phi : block.getPhiInstructions()) {
                Queue<CFGBasicBlock> queue = new LinkedList<>(phi.getSources().keySet());
                while (!queue.isEmpty()) {
                    CFGBasicBlock source = queue.poll();
                    if (!data.getDominatorTree().getPredecessors(block).contains(source)) {
                        phi.getSources().remove(source);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    /**
     * Prepares the work list (recursively)
     *
     * @param node the current node
     */
    private void fillWorkList(CFGBasicBlock node) {
        if (node == null) return;

        for (CFGAddress global : ctx.getGlobalVars()) {
            CFGInstruction def = data.getDefinition(global);
            if (def != null)
                workList.offer(def);
        }

        node.getAllInstructions().stream().filter(
                instruction -> ctx.isGlobalVar(instruction.address())
                        || instruction instanceof CFGJumpInstruction
                        || instruction instanceof CFGWriteInstruction
                        || instruction instanceof CFGMethodCallInstruction
                        || instruction instanceof CFGReturnInstruction
                        || instruction instanceof CFGExceptionInstruction
        ).forEach(workList::offer);

        data.getDominatorTree().getImmediateDominatedNodes(node).forEach(this::fillWorkList);
    }

    /**
     * Marks which instructions are live
     */
    private void mark() {
// System.out.println("begin mark");
        // compute live variables
        while (!workList.isEmpty()) {
            CFGInstruction instruction = workList.poll();
            if (alive.contains(instruction)) continue;
            alive.add(instruction);

            instruction.operands().stream().map(data::getDefinition).filter(Objects::nonNull).forEach(workList::offer);

// System.out.println(instruction + " belongs to block " + data.getBlock(instruction).address());
// System.out.println(pdt.getPostDominanceFrontier(data.getBlock(instruction)));
            for (CFGBasicBlock block : pdt.getPostDominanceFrontier(data.getBlock(instruction)))
                if (block.getLastInstruction() != null)
                    workList.offer(block.getLastInstruction());
        }
// System.out.println(alive);
    }

    /**
     * Eliminates dead instructions
     *
     * @return whether any changes were made
     */
    private boolean sweep() {
        boolean changed = false;

        // mark dead variables
        for (CFGBasicBlock block : method.getBlocks()) {
            Queue<CFGPhiInstruction> phiQueue = new LinkedList<>(block.getPhiInstructions().stream().toList());
            while (!phiQueue.isEmpty()) {
                CFGPhiInstruction phi = phiQueue.poll();
                if (!alive.contains(phi)) {
                    block.getPhiInstructions().remove(phi);
                    changed = true;
                }
            }

            Queue<CFGInstruction> queue = new LinkedList<>(block.getInstructions().stream().toList());
            while (!queue.isEmpty()) {
                CFGInstruction instruction = queue.poll();
                if (!alive.contains(instruction)) {
                    block.getInstructions().remove(instruction);
                    changed = true;
                }
            }

            CFGInstruction last = block.getLastInstruction();
            if (last == null || alive.contains(last)) continue;

            if (last instanceof CFGBranchInstruction) {
                changed = true;
                block.setLastInstruction(new CFGJumpInstruction(ctx, pdt.getImmediatePostDominator(block).address()));
            } else if (!(last instanceof CFGJumpInstruction)) {
                changed = true;
                block.setLastInstruction(null);
            }
        }

        return changed;
    }

}
