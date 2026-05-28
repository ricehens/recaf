package recaf.opt;

import recaf.cfg.*;
import recaf.utils.DominatorTree;

import java.util.*;

/**
 * A CFG-to-SSA converter for a particular method,
 * following Chapter 9 of Cooper et al.'s Engineering a Compiler, 3rd edition
 */
public class CFGtoSSA extends MethodTransformation {

    private DominatorTree<CFGBasicBlock> dominatorTree;

    /**
     * Constructs a new SSAConverter instance
     *
     * @param ctx the CFG context
     * @param method the method to convert
     */
    public CFGtoSSA(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
        dominatorTree = new DominatorTree<>(method.getBlocks());
    }

    /**
     * Performs SSA conversion
     *
     * @return true if successful
     */
    public boolean apply() {
        computeGlobals();
        insertPhiFunctions();
        renameVariables();
        return true;
    }

    /** Local variables that appear in multiple basic blocks */
    private Set<CFGAddress> globals;
    /** Indices of variables that appear in multiple basic blocks */
    private Set<Integer> globalIndices;
    /** Maps each variable to the set of basic blocks in which it appears */
    private Map<CFGAddress, Set<CFGBasicBlock>> blocks;

    /**
     * Computes the set of variables that appear in multiple basic blocks,
     * and the blocks each variable is assigned in.
     */
    private void computeGlobals() {
        globals = new HashSet<>();
        blocks = new HashMap<>();

        // parameters are global for convenience
        for (CFGAddress param : method.getParams()) {
            globals.add(param);
            blocks.computeIfAbsent(param, k -> new HashSet<>());
            blocks.get(param).add(method.getBlocks().peekFirst());
        }

        for (CFGBasicBlock b : method.getBlocks()) {
            Set<CFGAddress> varKill = new HashSet<>();
            for (CFGInstruction inst : b.getAllInstructions()) {
                for (CFGAddress operand : inst.operands())
                    if (inConsideration(operand) && !varKill.contains(operand))
                        globals.add(operand);

                if (inConsideration(inst.address())) {
                    varKill.add(inst.address());
                    blocks.computeIfAbsent(inst.address(), k -> new HashSet<>());
                    blocks.get(inst.address()).add(b);
                }
            }
        }

        // update indices listing
        globalIndices = new HashSet<>();
        for (CFGAddress addr : globals) {
            globalIndices.add(addr.index);
        }
    }

    /**
     * Inserts a blank phi function into each respective basic block wherever needed.
     */
    private void insertPhiFunctions() {
        Map<CFGBasicBlock, Set<CFGAddress>> phi = new HashMap<>();

        // compute necessary phi function locations
        for (CFGAddress x : globals) {
            Set<CFGBasicBlock> visited = new HashSet<>();

            Queue<CFGBasicBlock> workList = new LinkedList<>(blocks.getOrDefault(x, Set.of()));
            while (!workList.isEmpty()) {
                CFGBasicBlock b = workList.poll();
                if (visited.contains(b)) continue;

                for (CFGBasicBlock d : dominatorTree.getDominanceFrontier(b)) {
                    phi.computeIfAbsent(d, k -> new HashSet<>());
                    phi.get(d).add(x);
                    workList.offer(d);
                }

                visited.add(b);
            }
        }

        // insert phi functions
        for (CFGBasicBlock block : phi.keySet()) {
            for (CFGAddress addr : phi.get(block)) {
                block.getPhiInstructions().offerFirst(new CFGPhiInstruction(method.ctx(), addr));
            }
        }
    }

    private Map<Integer, Integer> counter;
    private Map<Integer, Stack<Integer>> stack;
    private Map<Integer, Integer> localCounter;
    private Map<Integer, Stack<Integer>> localStack;

    /**
     * Iterate through basic blocks and assign coindices to the variables.
     */
    private void renameVariables() {
        // We forgo coindices since they change throughout
        counter = new HashMap<>();
        stack = new HashMap<>();
        localCounter = new HashMap<>();
        localStack = new HashMap<>();

        for (CFGAddress global : globals) {
            counter.put(global.index, 0);
            stack.put(global.index, new Stack<>());
        }

        for (CFGAddress param : method.getParams()) {
            param.set(newName(counter, stack, param));
        }

        if (!method.getBlocks().isEmpty())
            rename(method.getBlocks().peekFirst());
    }

    /** Helper for variable rename */
    private CFGAddress newName(Map<Integer, Integer> counter, Map<Integer, Stack<Integer>> stack, CFGAddress n) {
        int i = counter.get(n.index);
        counter.put(n.index, i + 1);
        stack.get(n.index).push(i);

        CFGAddress addr = new CFGAddress(n.index, i);
        ctx.getSymbolTable().registerVariant(addr, n);

        return addr;
    }

    /** Helper for variable rename */
    private boolean inConsideration(CFGAddress addr) {
        if (addr == null) return false;
        addr = new CFGAddress(addr.index, 0);
        return !ctx.isGlobalVar(addr) && !ctx.getSymbolTable().getVar(addr).isArray();
    }

    /** Helper for variable rename */
    private void rename(CFGBasicBlock b) {
        for (CFGPhiInstruction phi : b.getPhiInstructions()) {
            phi.address().set(newName(counter, stack, phi.address()));
        }
        for (CFGInstruction inst : b.getAllInstructions()) {
            if (inst instanceof CFGPhiInstruction) continue;

            for (CFGAddress y : inst.operands()) {
                if (y != null && globalIndices.contains(y.index) && !stack.get(y.index).isEmpty()) {
                    y.set(new CFGAddress(y.index, stack.get(y.index).peek()));
                } else if (inConsideration(y) && localCounter.containsKey(y.index) && !localStack.get(y.index).isEmpty()) {
                    y.set(new CFGAddress(y.index, localStack.get(y.index).peek()));
                }
            }

            CFGAddress x = inst.address();
            if (inConsideration(x)) {
                if (globalIndices.contains(x.index)) {
                    x.set(newName(counter, stack, x));
                } else {
                    if (!localCounter.containsKey(x.index)) {
                        localCounter.put(x.index, 0);
                        localStack.put(x.index, new Stack<>());
                    }
                    x.set(newName(localCounter, localStack, x));
                }
            }
            // we ignore variable declarations as they are undefined behavior anyway
        }

        for (CFGBasicBlock successor : b.successors()) {
            for (CFGPhiInstruction phi : successor.getPhiInstructions()) {
                CFGAddress x = phi.address();
                if (inConsideration(x) && !stack.get(x.index).isEmpty()) {
                    phi.add(b, new CFGAddress(x.index, stack.get(x.index).peek()));
                }
            }
        }

        for (CFGBasicBlock successor : dominatorTree.getImmediateDominatedNodes(b))
            rename(successor);

        for (CFGInstruction inst : b.getAllInstructions()) {
            CFGAddress x = inst.address();

            // if global
            if (x != null && globalIndices.contains(x.index)) {
                stack.get(x.index).pop();
            }
        }
    }

}
