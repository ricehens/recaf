package recaf.opt;

import recaf.cfg.*;
import recaf.utils.ParallelCopyGroup;

import java.util.*;

/**
 * An optimization for a method of a SSA CFG that eliminates useless control flow.
 */
public class DeadBlockElimination extends MethodTransformation {

   /**
     * Creates a new cleaner for a method
    *
     * @param ctx the CFG context
     * @param method the method to optimize
     */
    public DeadBlockElimination(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
    }

    private Map<CFGBasicBlock, Set<CFGBasicBlock>> predecessors;
    private Map<CFGBasicBlock, Set<CFGBasicBlock>> successors;

    /**
     * Runs clean on a method of a SSA CFG
     *
     * @return whether any changes were made
     */
    @Override
    public boolean apply() {
        boolean changed = false;
        computePredecessors();
        while (clean()) {
// System.out.println("Hello");
            changed = true;
            computePredecessors();
        }
        return changed;
    }

    /**
     * Attempts a single pass of dead code elimination
     *
     * @return whether any changes were made
     */
    private boolean clean() {
        Set<CFGBasicBlock> visited = new HashSet<>();
        return clean(method.getBlocks().peekFirst(), visited);
    }

    /**
     * Performs cleaning with respect to a given block
     *
     * @param i the block
     * @param visited the set of visited blocks
     * @return whether any changes were made
     */
    private boolean clean(CFGBasicBlock i, Set<CFGBasicBlock> visited) {
        if (visited.contains(i)) return false;
        visited.add(i);

// System.out.println("Processing block " + i.address());
        boolean changed = false;
        for (CFGBasicBlock succ : successors.get(i)) {
            changed |= clean(succ, visited);
        }

        // if i ends in conditional branch and targets are identical, replace with jump
        if (i.getLastInstruction() instanceof CFGBranchInstruction branch) {
            if (branch.thenAddr().equals(branch.elseAddr())) {
// System.out.println("Case 1: Changing last instruction of " + i.address() + " to jump to " + branch.thenAddr());
                i.setLastInstruction(new CFGJumpInstruction(ctx, branch.thenAddr()));
                changed = true;
            }
        }

        if (i.getLastInstruction() == null || i.getLastInstruction() instanceof CFGJumpInstruction) {
            CFGBasicBlock j = i.getLastInstruction() == null
                    ? method.getBlocks().next(i)
                    : ctx.getSymbolTable().getBlock(((CFGJumpInstruction) i.getLastInstruction()).jumpAddr());

            // if i ends in a jump to j and j is empty, replace transfers to i with transfers to j
            if (i.getInstructions().isEmpty() && i.getPhiInstructions().isEmpty()
                    && !i.equals(method.getBlocks().peekFirst())) {
                boolean abort = false;
                for (CFGPhiInstruction phi : j.getPhiInstructions())
                    for (CFGBasicBlock p : predecessors.get(i))
                        if (phi.getSources().containsKey(p) && !phi.getSources().get(p).equals(phi.getSources().get(i)))
                            abort = true;
                if (!abort) {
// System.out.println("Case 2: replacing transfers to " + i.address() + " with " + j.address());
                    for (CFGPhiInstruction phi : j.getPhiInstructions()) {
                        if (phi.getSources().containsKey(i)) {
                            CFGAddress val = phi.getSources().get(i);
                            predecessors.get(i).forEach(p -> phi.add(p, val));
                            // phi.getSources().remove(i);
// System.out.printf("Case 2 %s %s: changed phi statement %s%n", i.address(), j.address(), phi);
                            changed = true;
                        }
                        for (CFGBasicBlock p : predecessors.get(i)) {
                            if (p.getLastInstruction() instanceof CFGJumpInstruction jump2) {
                                if (jump2.jumpAddr().equals(i.address())) {
                                    p.setLastInstruction(new CFGJumpInstruction(ctx, j.address()));
// System.out.printf("Case 2 %s %s: changed jump statement %s in block %s%n", i.address(), j.address(), jump2, p.address());
                                    changed = true;
                                }
                            }
                            if (p.getLastInstruction() instanceof CFGBranchInstruction branch) {
                                CFGAddress thenAddr = branch.thenAddr();
                                CFGAddress elseAddr = branch.elseAddr();
                                if (thenAddr.equals(i.address())) {
                                    thenAddr = j.address();
// System.out.printf("Case 2 %s %s: changed branch statement %s in block %s%n", i.address(), j.address(), branch, p.address());
                                    changed = true;
                                }
                                if (elseAddr.equals(i.address())) {
                                    elseAddr = j.address();
// System.out.printf("Case 2 %s %s: changed branch statement %s in block %s%n", i.address(), j.address(), branch, p.address());
                                    changed = true;
                                }
                                p.setLastInstruction(new CFGBranchInstruction(ctx, branch.boolAddr(), thenAddr, elseAddr));
                            }
                        }
                    }
                }
            }

            // if j has only one predecessor, then combine i and j
            if (predecessors.get(j).size() == 1) {
// System.out.println("Case 3: Combining " + i.address() + " with " + j.address());
                // copy phi instructions just in case
                ParallelCopyGroup<CFGAddress> group = new ParallelCopyGroup<>(ctx.getSymbolTable());
                for (CFGPhiInstruction phi : j.getPhiInstructions()) {
                    if (phi.getSources().containsKey(i)) {
                        group.addEdge(new CFGCopyInstruction(ctx, phi.address(), phi.getSources().get(i)));
                        changed = true;
                    }
                }
                OptUtils.sequentialize(ctx, group).forEach(i.getInstructions()::offerLast);

                // pipe instructions
// System.out.printf("== block %s ==%n%s%n== block %s ==%n%s%n", i.address(), i, j.address(), j);
                if (!j.getInstructions().isEmpty()) {
                    j.getInstructions().forEach(i.getInstructions()::offerLast);
                    changed = true;
                }
                if (i.getLastInstruction() != j.getLastInstruction()) {
                    i.setLastInstruction(j.getLastInstruction().copy());
                    changed = true;
                }

                for (CFGBasicBlock p : successors.get(j)) {
                    for (CFGPhiInstruction phi : p.getPhiInstructions()) {
                        if (phi.getSources().containsKey(j)) {
                            CFGAddress val = phi.getSources().get(j);
                            phi.add(i, val);
                            // phi.getSources().remove(j);
                            changed = true;
                        }
                    }
                }

            }

            // if j is empty and ends in a conditional branch, then overwrite i's jump with a copy of j's branch
            if (j.getInstructions().isEmpty()) {
                boolean localChange = false;
                boolean abort = false;
// System.out.println("Case 4: Replacing jump in " + i.address() + " with last instruction of " + j.address());
                Map<CFGAddress, CFGAddress> phiTranslation = new HashMap<>();
                if (!j.getPhiInstructions().isEmpty()) {
                    // we must be in SSA form
                    SSAData data = new SSAData(method);
                    for (CFGPhiInstruction phi : j.getPhiInstructions()) {
                        if (phi.getSources().containsKey(i))
                            phiTranslation.put(phi.address(), phi.getSources().get(i));
                        for (CFGInstruction use : data.getUses(phi.address())) {
                            if (!data.getBlock(use).equals(j)) {
                                abort = true;
                                break;
                            }
                        }
                    }
                }

                if (!abort) {
                    CFGLastInstruction last = j.getLastInstruction();
                    if (last instanceof CFGJumpInstruction jlast) {
                        CFGJumpInstruction translation = (CFGJumpInstruction) jlast.copy();
                        if (phiTranslation.containsKey(translation.jumpAddr()))
                            translation = new CFGJumpInstruction(ctx, phiTranslation.get(translation.jumpAddr()));
                        if (!(i.getLastInstruction() instanceof CFGJumpInstruction jorig)
                                || !jorig.jumpAddr().equals(translation.jumpAddr())) {
                            i.setLastInstruction(translation);
                            localChange = true;
                        }
                    } else if (last instanceof CFGBranchInstruction blast) {
                        CFGBranchInstruction translation = (CFGBranchInstruction) blast.copy();
                        if (phiTranslation.containsKey(translation.boolAddr()))
                            translation = new CFGBranchInstruction(ctx, phiTranslation.get(translation.boolAddr()), translation.thenAddr(), translation.elseAddr());
                        if (phiTranslation.containsKey(translation.thenAddr()))
                            translation = new CFGBranchInstruction(ctx, translation.boolAddr(), phiTranslation.get(translation.thenAddr()), translation.elseAddr());
                        if (phiTranslation.containsKey(translation.elseAddr()))
                            translation = new CFGBranchInstruction(ctx, translation.boolAddr(), translation.thenAddr(), phiTranslation.get(translation.elseAddr()));
                        if (!(i.getLastInstruction() instanceof CFGBranchInstruction borig)
                                || !borig.boolAddr().equals(translation.boolAddr())
                                || !borig.thenAddr().equals(translation.thenAddr())
                                || !borig.elseAddr().equals(translation.elseAddr())) {
                            i.setLastInstruction(translation);
                            localChange = true;
                        }
                    } else if (last instanceof CFGReturnInstruction) {
                        CFGReturnInstruction translation = (CFGReturnInstruction) last.copy();
                        if (phiTranslation.containsKey(translation.returnAddress()))
                            translation = new CFGReturnInstruction(ctx, phiTranslation.get(translation.returnAddress()));
                        if (!(i.getLastInstruction() instanceof CFGReturnInstruction rorig)
                                || rorig.returnAddress() != null
                                && !rorig.returnAddress().equals(translation.returnAddress())) {
                            i.setLastInstruction(translation);
                            localChange = true;
                        }
                    } else if (last instanceof CFGExceptionInstruction) {
                        CFGExceptionInstruction translation = (CFGExceptionInstruction) last.copy();
                        if (!(i.getLastInstruction() instanceof CFGExceptionInstruction forig)
                                || !forig.msg().equals(translation.msg())) {
                            i.setLastInstruction(translation);
                            localChange = true;
                        }
                    }

                    if (localChange) {
                        changed = true;
                        for (CFGBasicBlock p : successors.get(j)) {
                            for (CFGPhiInstruction phi : p.getPhiInstructions()) {
                                if (phi.getSources().containsKey(j)) {
                                    CFGAddress val = phi.getSources().get(j);
                                    if (phiTranslation.containsKey(val))
                                        val = phiTranslation.get(val);
                                    phi.add(i, val);
                                }
                            }
                        }
                    }
                }
// System.out.println(method);
            }

        }
// System.out.printf("changed = %second%n", changed);
        computePredecessors();
        return changed;
    }

    /**
     * Precomputes the predecessor and successor information.
     */
    private void computePredecessors() {
        predecessors = new HashMap<>();
        successors = new HashMap<>();
        Set<CFGBasicBlock> visited = new HashSet<>();
        computePredecessors(method.getBlocks().peekFirst(), visited);
    }

    /** Recursive helper for computePredecessors */
    private void computePredecessors(CFGBasicBlock i, Set<CFGBasicBlock> visited) {
        if (visited.contains(i)) return;
        visited.add(i);
        successors.put(i, new HashSet<>(i.successors()));
        predecessors.computeIfAbsent(i, k -> new HashSet<>());
        i.successors().forEach(succ -> predecessors.computeIfAbsent(succ, k -> new HashSet<>()).add(i));
        i.successors().forEach(succ -> computePredecessors(succ, visited));
    }

}
