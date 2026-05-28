package recaf.opt;

import recaf.cfg.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * A pass to eliminate self-recursive tail calls.
 */
public class RecursiveTailCallElimination extends SSATransformation {

    /**
     * Constructs a new inline / recursive tail-call expander
     *
     * @param ctx the context
     * @param method the method
     */
    public RecursiveTailCallElimination(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
    }

    @Override
    public boolean apply() {
        boolean changed = false;
        Map<CFGAddress, CFGPhiInstruction> insertedPhis = new HashMap<>();
        for (CFGBasicBlock block : method.getBlocks()) {
            CFGLastInstruction last = block.getLastInstruction();
            CFGInstruction secondLast = block.getInstructions().peekLast();
            if (last instanceof CFGReturnInstruction ret
                    && secondLast instanceof CFGMethodCallInstruction call
                    && (ret.returnAddress() == null || ret.returnAddress().equals(secondLast.address()))
                    && call.methodName().equals(method.getName()))
            {
                if (!changed) {
                    method.getBlocks().offerFirst(new CFGBasicBlock(ctx));
                    CFGBasicBlock firstBlock = method.getBlocks().peekFirst();
                    CFGBasicBlock secondBlock = method.getBlocks().next(firstBlock);
                    firstBlock.setLastInstruction(new CFGJumpInstruction(ctx, secondBlock.address()));

                    for (int i = 0; i < method.getParams().size(); i++) {
                        CFGAddress param = method.getParams().get(i);
                        CFGPhiInstruction phi = new CFGPhiInstruction(ctx, CFGAddress.clone(param));
                        CFGAddress newName = ctx.getSymbolTable().newNode(param);
                        param.set(newName);
                        phi.add(firstBlock, newName);
                        insertedPhis.put(newName, phi);
                        secondBlock.getPhiInstructions().offerLast(phi);
                    }
                }

                for (int i = 0; i < method.getParams().size(); i++) {
                    CFGPhiInstruction phi = insertedPhis.get(method.getParams().get(i));
                    phi.add(block, call.args().get(i));
                }

                CFGBasicBlock firstBlock = method.getBlocks().peekFirst();
                CFGBasicBlock secondBlock = method.getBlocks().next(firstBlock);
                block.getInstructions().pollLast();
                block.setLastInstruction(new CFGJumpInstruction(ctx, secondBlock.address()));

                changed = true;
            }
        }

        // so phi instructions from the parameters can be built
        if (changed) {
            CFGBasicBlock firstBlock = method.getBlocks().peekFirst();
            CFGBasicBlock newFirstBlock = new CFGBasicBlock(ctx);
            newFirstBlock.setLastInstruction(new CFGJumpInstruction(ctx, firstBlock.address()));
            method.getBlocks().offerFirst(newFirstBlock);

            pruneInvalidPhiSources();
        }

        return changed;
    }

    private void pruneInvalidPhiSources() {
        Map<CFGBasicBlock, Set<CFGBasicBlock>> predecessors = new HashMap<>();
        for (CFGBasicBlock block : method.getBlocks()) {
            predecessors.putIfAbsent(block, new HashSet<>());
        }

        for (CFGBasicBlock block : method.getBlocks()) {
            for (CFGBasicBlock successor : block.successors()) {
                predecessors.computeIfAbsent(successor, _ -> new HashSet<>()).add(block);
            }
        }

        for (CFGBasicBlock block : method.getBlocks()) {
            Set<CFGBasicBlock> preds = predecessors.getOrDefault(block, Set.of());
            for (CFGPhiInstruction phi : block.getPhiInstructions()) {
                Queue<CFGBasicBlock> sources = new LinkedList<>(phi.getSources().keySet());
                while (!sources.isEmpty()) {
                    CFGBasicBlock source = sources.poll();
                    if (!preds.contains(source))
                        phi.getSources().remove(source);
                }
            }
        }
    }

}
