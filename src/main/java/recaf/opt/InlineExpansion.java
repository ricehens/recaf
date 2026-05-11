package recaf.opt;

import recaf.cfg.*;
import recaf.general.Type;
import recaf.opt.LoopUtils.Loop;

import java.util.*;

/**
 * A pass to expand inline calls and recursive tail calls.
 */
public class InlineExpansion implements Transformation {

    private CFGProgram cfg;
    private CFGContext ctx;

    private static final int SMALL_FUNC_THRESHOLD = 15;
    private static final int LARGE_FUNC_THRESHOLD = 150;
    private static final int VERY_LARGE_FUNC_THRESHOLD = 300;

    private Map<CFGMethod, Set<CFGMethod>> callGraph;
    private Map<CFGMethod, Integer> staticCallSites;
    private Set<CFGMethod> recursive;

    /**
     * Constructs first new inline / recursive tail-call expander
     *
     * @param cfg the control flow graph
     */
    public InlineExpansion(CFGProgram cfg) {
        this.cfg = cfg;
        ctx = cfg.ctx();
    }

    /**
     * Determines if first method is eligible for inlining
     *
     * @param callee the method to evaluate
     * @return true if eligible
     */
    private boolean eligible(CFGMethod callee, CFGMethod caller, CFGBasicBlock callSite) {
        // external call or recursive
        if (callee == null || recursive.contains(callee))
            return false;

        // number of instructions of callee
        int numInstructions = 0;
        for (CFGBasicBlock block : callee.getBlocks()) {
            numInstructions += block.getInstructions().size();
        }

        // loop depth of call site
        LoopUtils loops = new LoopUtils(caller);
        int loopDepth = 0;
        for (Loop loop : loops.getLoops()) {
            if (loop.blocks.contains(callSite))
                loopDepth++;
        }

        // number of call sites
        int sites = staticCallSites.get(callee);

        // number of loops in callee
        LoopUtils calleeLoops = new LoopUtils(callee);
        int numCalleeLoops = calleeLoops.getLoops().size();

        // tiny helpers --- just do it
        if (numInstructions <= SMALL_FUNC_THRESHOLD && callGraph.get(callee).isEmpty())
            return true;

        // large monsters: never
        // loops: never
        if (numInstructions >= VERY_LARGE_FUNC_THRESHOLD || numCalleeLoops >= 1)
            return false;

        // moderately large: allow only if
        // single call site and buried in loop
        if (numInstructions >= LARGE_FUNC_THRESHOLD)
            return sites == 1 && loopDepth >= 2;

        // medium size: if either
        // single call site or inside first loop
        return sites == 1 || loopDepth >= 1;
    }

    @Override
    public boolean apply() {
        boolean changed = false;

        outer:
        while (true) {
            computeCallGraph();
            computeRecursiveMethods();

            for (CFGMethod method : cfg.getMethods()) {
                for (CFGBasicBlock block : method.getBlocks()) {
                    for (CFGInstruction instruction : block.getAllInstructions()) {
                        if (instruction instanceof CFGMethodCallInstruction call) {
                            CFGMethod callee = ctx.getSymbolTable().getMethod(call.methodName());
                            if (eligible(callee, method, block)) {
                                inline(call, method, block, callee);
                                changed = true;
                                clean();
                                continue outer;
                            }
                        }
                    }
                }
            }

            break;
        }

        changed |= clean();
        return changed;
    }

    /**
     * Inlines first method call
     *
     * @param call the call
     * @param method the source method
     * @param sourceBlock the block within the source method containing the call
     * @param callee the called method
     */
    private void inline(CFGMethodCallInstruction call, CFGMethod method, CFGBasicBlock sourceBlock, CFGMethod callee) {
// System.out.println("Inlining " +  callee.getName());
        if (callee.getBlocks().isEmpty())
            sourceBlock.getInstructions().remove(call);

        Map<CFGBasicBlock, CFGBasicBlock> newBlocks = new HashMap<>();
        Map<CFGAddress, CFGAddress> newNames = new HashMap<>();
        for (CFGBasicBlock block : callee.getBlocks())
            newBlocks.put(block, new CFGBasicBlock(ctx));

        // split source block
        CFGBasicBlock nextBlock = sourceBlock;
        sourceBlock = new CFGBasicBlock(ctx);
        method.getBlocks().insertBefore(nextBlock, sourceBlock);
        sourceBlock.setMethod(method);
        while (nextBlock.getInstructions().prev(call) != null) {
            CFGInstruction prev = nextBlock.getInstructions().prev(call);
            sourceBlock.getInstructions().offerFirst(prev);
            nextBlock.getInstructions().remove(prev);
        }
        nextBlock.getPhiInstructions().forEach(sourceBlock.getPhiInstructions()::offerLast);
        nextBlock.getPhiInstructions().clear();
        nextBlock.getInstructions().remove(call);
        sourceBlock.setLastInstruction(new CFGJumpInstruction(ctx, newBlocks.get(callee.getBlocks().peekFirst()).address()));

        // redirect
        for (CFGBasicBlock block : method.getBlocks()) {
            if (block.getLastInstruction() instanceof CFGJumpInstruction jump) {
                if (jump.jumpAddr().equals(nextBlock.address()))
                    block.setLastInstruction(new CFGJumpInstruction(ctx, sourceBlock.address()));
            } else if (block.getLastInstruction() instanceof CFGBranchInstruction branch) {
                if (branch.thenAddr().equals(nextBlock.address()))
                    block.setLastInstruction(new CFGBranchInstruction(ctx, branch.boolAddr(), sourceBlock.address(), branch.elseAddr()));
                if (branch.elseAddr().equals(nextBlock.address()))
                    block.setLastInstruction(new CFGBranchInstruction(ctx, branch.boolAddr(), branch.thenAddr(), sourceBlock.address()));
            }
        }

        // copy params
        for (int i = 0; i < callee.getParams().size(); i++) {
            CFGAddress arg = call.args().get(i);
            CFGAddress param = callee.getParams().get(i);
            newNames.put(param, ctx.getSymbolTable().newNode(param));
            sourceBlock.getInstructions().offerLast(new CFGCopyInstruction(ctx, newNames.get(param), arg));
        }

        // no new names for global variables
        for (CFGAddress global : ctx.getGlobalVars()) {
            newNames.put(global, global);
        }

        // create destination phi instruction
        CFGPhiInstruction sink = null;
        if (callee.getType() != Type.VOID && !ctx.isGlobalVar(call.address())) {
            sink = new CFGPhiInstruction(ctx, call.address());
            nextBlock.getPhiInstructions().offerLast(sink);
        }

        // copy new blocks
        CFGBasicBlock insertAfter = sourceBlock;
        for (CFGBasicBlock block : callee.getBlocks()) {
            CFGBasicBlock duplicate = newBlocks.get(block);
            duplicate.setMethod(method);
            method.getBlocks().insertAfter(insertAfter, duplicate);
            insertAfter = duplicate;

            for (CFGPhiInstruction phi : block.getPhiInstructions()) {
                CFGPhiInstruction duplicatePhi = phi.copy();
                duplicatePhi.address().set(newNames.computeIfAbsent(phi.address(),
                        k -> ctx.getSymbolTable().newNode(phi.address())));
                Queue<CFGBasicBlock> workList = new LinkedList<>(phi.getSources().keySet());
                while (!workList.isEmpty()) {
                    CFGBasicBlock source = workList.poll();
                    duplicatePhi.add(newBlocks.get(source),
                            newNames.computeIfAbsent(phi.getSources().get(source),
                                    k -> ctx.getSymbolTable().newNode(phi.getSources().get(source))));
                    duplicatePhi.getSources().remove(source);
                }
                duplicate.getPhiInstructions().offerLast(duplicatePhi);
            }

            for (CFGInstruction instruction : block.getInstructions()) {
                CFGInstruction duplicateInstruction = instruction.copy();
                if (instruction.address() != null)
                    duplicateInstruction.address().set(newNames.computeIfAbsent(instruction.address(),
                            _ -> ctx.getSymbolTable().newNode(instruction.address())));
                for (CFGAddress operand : duplicateInstruction.operands()) {
                    operand.set(newNames.computeIfAbsent(operand,
                            _ -> ctx.getSymbolTable().newNode(operand)));
                }
                // replace local array argument
                if (duplicateInstruction instanceof CFGMethodCallInstruction methodCall) {
                    for (CFGAddress arg : methodCall.args()) {
                        if (ctx.getSymbolTable().getVar(arg).isArray()) {
                            arg.set(newNames.computeIfAbsent(arg,
                                    _ -> ctx.getSymbolTable().newNode(arg)));
                        }
                    }
                }

                // direct record bases are not included in operands() and still need remapping
                if (duplicateInstruction instanceof CFGReadInstruction read) {
                    if (ctx.getType(read.recordAddress()) == Type.RECORD) {
                        read.recordAddress().set(newNames.computeIfAbsent(read.recordAddress(),
                                k -> ctx.getSymbolTable().newNode(read.recordAddress())));
                    }
                } else if (duplicateInstruction instanceof CFGWriteInstruction write) {
                    if (ctx.getType(write.recordAddress()) == Type.RECORD) {
                        write.recordAddress().set(newNames.computeIfAbsent(write.recordAddress(),
                                k -> ctx.getSymbolTable().newNode(write.recordAddress())));
                    }
                }

                duplicate.getInstructions().offerLast(duplicateInstruction);
            }

            CFGLastInstruction lastInstruction = block.getLastInstruction();
            if (lastInstruction != null) {
                if (lastInstruction instanceof CFGJumpInstruction jump) {
                    duplicate.setLastInstruction(new CFGJumpInstruction(ctx, newBlocks.get(ctx.getSymbolTable().getBlock(jump.jumpAddr())).address()));
                } else if (lastInstruction instanceof CFGBranchInstruction branch) {
                    duplicate.setLastInstruction(new CFGBranchInstruction(ctx,
                            newNames.computeIfAbsent(branch.boolAddr(), k -> ctx.getSymbolTable().newNode(branch.boolAddr())),
                            newBlocks.get(ctx.getSymbolTable().getBlock(branch.thenAddr())).address(),
                            newBlocks.get(ctx.getSymbolTable().getBlock(branch.elseAddr())).address()));
                } else if (lastInstruction instanceof CFGExceptionInstruction falloff) {
                    duplicate.setLastInstruction(falloff.copy()); // falloff still refers to function that falls off
                } else if (lastInstruction instanceof CFGReturnInstruction ret) {
                    if (ret.returnAddress() != null) {
                        if (sink != null) {
                            sink.add(duplicate, newNames.computeIfAbsent(ret.returnAddress(), k -> ctx.getSymbolTable().newNode(ret.returnAddress())));
                        } else { // global var
                            assert ctx.isGlobalVar(call.address());
                            duplicate.getInstructions().offerLast(new CFGCopyInstruction(ctx, call.address(),
                                    newNames.computeIfAbsent(ret.returnAddress(), k -> ctx.getSymbolTable().newNode(ret.returnAddress()))));
                        }
                    }
                    duplicate.setLastInstruction(new CFGJumpInstruction(ctx, nextBlock.address()));
                }
            }
        }

// System.out.println("INLINE COMPLETE\n" + cfg);
    }

    /**
     * Cleans unreachable methods
     *
     * @return true if any methods were removed
     */
    private boolean clean() {
        computeCallGraph();
        Set<CFGMethod> live = new HashSet<>();
        for (CFGMethod method : cfg.getMethods()) {
            if (method.getName().equals("main")) {
                dfs(method, live);
                break;
            }
        }

        boolean changed = false;
        for (int i = 0; i < cfg.getMethods().size();) {
            CFGMethod method = cfg.getMethods().get(i);
            if (!live.contains(method)) {
                cfg.getMethods().remove(i);
                changed = true;
            } else i++;
        }
// if (changed) System.out.println("CLEAN COMPLETE\n" + cfg);
        return changed;
    }

    /**
     * Precomputes the call graph between procedures
     */
    private void computeCallGraph() {
        callGraph = new HashMap<>();
        staticCallSites = new HashMap<>();

        for (CFGMethod method : cfg.getMethods()) {
            callGraph.put(method, new HashSet<>());
            staticCallSites.put(method, 0);
        }

        for (CFGMethod method : cfg.getMethods()) {
            for (CFGBasicBlock block : method.getBlocks()) {
                for (CFGInstruction instruction : block.getAllInstructions()) {
                    if (instruction instanceof CFGMethodCallInstruction call) {
                        CFGMethod callee = ctx.getSymbolTable().getMethod(call.methodName());
                        if (callee == null) continue; // external call
                        callGraph.get(method).add(callee);
                        staticCallSites.put(callee, staticCallSites.get(callee) + 1);
                    }
                }
            }
        }
    }

    /**
     * Computes which methods are recursive (i.e. can reach themselves within the call graph)
     */
    private void computeRecursiveMethods() {
        recursive = new HashSet<>();
        outer:
        for (CFGMethod method : cfg.getMethods()) {
            Set<CFGMethod> visited = new HashSet<>();
            for (CFGMethod callee : callGraph.get(method)) {
                dfs(callee, visited);
            }
            if (visited.contains(method))
                recursive.add(method);
        }
    }

    /**
     * Runs DFS/floodfill.
     *
     * @param method the current method
     * @param visited the set of visited methods
     */
    private void dfs(CFGMethod method, Set<CFGMethod> visited) {
        if (visited.contains(method))
            return;
        visited.add(method);
        for (CFGMethod callee : callGraph.get(method)) {
            dfs(callee, visited);
        }
    }

}
