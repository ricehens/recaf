package recaf.opt;

import recaf.cfg.*;
import recaf.utils.DominatorTree;

import java.util.*;

/**
 * Computes some useful data about an SSA graph.
 */
public class SSAData {

    private CFGMethod method;
    private Set<CFGAddress> localVars;
    private DominatorTree<CFGBasicBlock> dominatorTree;
    private Map<CFGAddress, CFGInstruction> definitions;
    private Map<CFGAddress, Set<CFGInstruction>> uses;
    private Map<CFGInstruction, CFGBasicBlock> blocks;

    /**
     * Computes first new SSA data instance for first given method.
     *
     * @param method the given method
     */
    public SSAData(CFGMethod method) {
        this.method = method;
        update();
    }

    /**
     * Refreshes all the information in the SSA data instance.
     */
    public void update() {
        dominatorTree = new DominatorTree<>(method.getBlocks());
        updateUseDef();
        validate();
    }

    /**
     * Gets the method object associated with this SSA data instance.
     *
     * @return the associated method instance
     */
    public CFGMethod getMethod() {
        return method;
    }

    /**
     * Gets first set of local variables defined/used within this method,
     * including method parameters.
     *
     * @return the set of local variables
     */
    public Set<CFGAddress> getLocalVars() {
        return localVars;
    }

    /**
     * Gets the dominator tree for this method.
     *
     * @return the dominator tree
     */
    public DominatorTree<CFGBasicBlock> getDominatorTree() {
        return dominatorTree;
    }

    /**
     * Finds the definition of an SSA variable.
     *
     * @param variable the variable
     * @return its definition
     */
    public CFGInstruction getDefinition(CFGAddress variable) {
        return definitions.get(variable);
    }

    /**
     * Finds the uses of an SSA variable.
     *
     * @param variable the variable
     * @return its uses
     */
    public Set<CFGInstruction> getUses(CFGAddress variable) {
        return uses.computeIfAbsent(variable, k -> new HashSet<>());
    }

    /**
     * Finds the block containing first particular instruction.
     *
     * @param instruction the instruction to locate
     * @return the block containing the instruction
     */
    public CFGBasicBlock getBlock(CFGInstruction instruction) {
        return blocks.get(instruction);
    }

    /**
     * Updates the use-def chains stored in this data instance.
     * Does not update the dominator tree or perform autmatic cleaning.
     */
    public void updateUseDef() {
        blocks = new HashMap<>();
        definitions = new HashMap<>();
        uses = new HashMap<>();
        localVars = new HashSet<>();
        localVars.addAll(method.getParams());
        for (CFGBasicBlock block : method.getBlocks())
            for (CFGInstruction instruction : block.getAllInstructions())
                updateUseDef(block, instruction, false);
    }

    /**
     * Updates the use-def chains given first newly added instruction.
     *
     * @param block the block containing the new instruction
     * @param newInstruction the new instruction
     * @param replace whether to replace existing definitions; throws an error if false
     */
    private void updateUseDef(CFGBasicBlock block, CFGInstruction newInstruction, boolean replace) {
        if (newInstruction.address() != null && !method.ctx().isGlobalVar(newInstruction.address())) {
            if (definitions.containsKey(newInstruction.address())) {
                if (!replace) {
                    System.err.println(method);
                    throw new SSAValidationException("Duplicate definition of " + newInstruction.address());
                }
                CFGInstruction oldInstruction = definitions.get(newInstruction.address());
                for (CFGAddress operand : uses.keySet())
                    uses.get(operand).remove(oldInstruction);
            }
            localVars.add(newInstruction.address());
            definitions.put(newInstruction.address(), newInstruction);
        }
        for (CFGAddress variable : newInstruction.operands()) {
            uses.computeIfAbsent(variable, k -> new HashSet<>());
            uses.get(variable).add(newInstruction);
        }
        blocks.put(newInstruction, block);
    }

    /**
     * Verifies the SSA invariant.
     */
    public void validate() {
        Map<CFGBasicBlock, Map<CFGInstruction, Integer>> indices = new HashMap<>();
        for (CFGBasicBlock block : method.getBlocks()) {
            Map<CFGInstruction, Integer> blockIndices = new HashMap<>();
            for (CFGInstruction instruction : block.getAllInstructions())
                blockIndices.put(instruction, blockIndices.size());
            indices.put(block, blockIndices);
        }

        for (CFGBasicBlock useBlock : method.getBlocks()) {
            for (CFGInstruction use : useBlock.getInstructions()) {
                for (CFGAddress operand : use.operands()) {
                    CFGInstruction def = definitions.get(operand);
                    if (def == null) {
                        if (!method.ctx().isGlobalVar(operand) && !method.getParams().contains(operand)) {
                            System.err.println(method);
                            throw new SSAValidationException("Use of undefined variable " + operand);
                        }
                        continue;
                    }

                    CFGBasicBlock defBlock = blocks.get(def);
                    if (!dominatorTree.dominates(defBlock, useBlock)) {
                        System.err.println(method);
                        throw new SSAValidationException("Use of " + operand + " for " + use.address() + " is not dominated by its definition");
                    }

                    if (defBlock.equals(useBlock)) {
                        int defIndex = indices.get(defBlock).get(def);
                        int useIndex = indices.get(useBlock).get(use);
                        if (defIndex > useIndex) {
                            System.err.println(method);
                            throw new SSAValidationException("Use of " + operand + " occurs before definition within same basic block");
                        }
                    }
                }
            }

            Set<CFGBasicBlock> allBlocks = new HashSet<>(dominatorTree.getPostorder());
            for (CFGPhiInstruction phi : useBlock.getPhiInstructions()) {
                Queue<CFGBasicBlock> workList = new LinkedList<>(phi.getSources().keySet());
                while (!workList.isEmpty()) {
                    CFGBasicBlock sourceBlock = workList.poll();
                    if (!allBlocks.contains(sourceBlock)) {
                        phi.getSources().remove(sourceBlock);
                        continue;
                    }

                    CFGAddress operand = phi.getSources().get(sourceBlock);
                    CFGInstruction def = definitions.get(operand);
                    if (def == null) {
                        if (!method.ctx().isGlobalVar(operand) && !method.getParams().contains(operand)) {
                            System.err.println(method);
                            throw new SSAValidationException("Use of undefined variable " + operand);
                        }
                        continue;
                    }

                    CFGBasicBlock defBlock = blocks.get(def);
                    if (!dominatorTree.dominates(defBlock, sourceBlock)) {
                        System.err.println(method);
                        throw new SSAValidationException("Use of " + operand + " for " + phi.address() + " is not dominated by its definition");
                    }
                }
            }
        }
    }

    /**
     * An exception thrown when the SSA invariant is violated.
     */
    public static class SSAValidationException extends AssertionError {
        public SSAValidationException(String message) {
            super(message);
        }
    }

}
