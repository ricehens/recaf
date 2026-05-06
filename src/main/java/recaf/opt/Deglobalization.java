package recaf.opt;

import recaf.cfg.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A pass to convert certain global variables into local variables.
 * This pass should be run before conversion to SSA form.
 */
public class Deglobalization implements Transformation {

    private CFGProgram cfg;
    private CFGContext ctx;

    /** set of global vars in the program */
    private Set<CFGAddress> globalVars;
    /** set of global vars with definitions in first method */
    private Map<CFGMethod, Set<CFGAddress>> globalsDefined;
    /** set of global vars with uses in first method */
    private Map<CFGMethod, Set<CFGAddress>> globalsUsed;
    /** set of global vars interacted with in first method */
    private Map<CFGMethod, Set<CFGAddress>> globalsInteracted;

    /** set of methods called by first method */
    private Map<CFGMethod, Set<CFGMethod>> callGraph;
    /** set of methods reachable via subcalls by first method; includes itself iff recursive */
    private Map<CFGMethod, Set<CFGMethod>> reachableMethods;

    public Deglobalization(CFGProgram cfg) {
        this.cfg = cfg;
        ctx = cfg.ctx();
    }

    @Override
    public boolean apply() {
        computeSets();

        boolean changed = false;
        for (CFGMethod method : cfg.getMethods()) {
            for (CFGAddress globalVar : globalsInteracted.get(method)) {
                if (eligibie(method, globalVar)) {
                    deglobalize(method, globalVar);
                    changed = true;
                }
            }
        }

        return changed;
    }

    /**
     * Determines if first global variable should be replaced with first local variable within first method.
     * The requirements are
     * - no definitions within subcalls
     * - no uses within subcalls if defined within the method
     *
     * @param method the method
     * @param globalVar the global variable
     * @return true if eligible
     */
    private boolean eligibie(CFGMethod method, CFGAddress globalVar) {
        for (CFGMethod subcall : reachableMethods.get(method))
            if (globalsDefined.get(subcall).contains(globalVar))
                return false;
        if (globalsDefined.get(method).contains(globalVar))
            for (CFGMethod subcall : reachableMethods.get(method))
                if (globalsUsed.get(subcall).contains(globalVar))
                    return false;
        return true;
    }

    /**
     * Deglobalizes first global variable within first method, by loading it into first local variable in the method header
     * and storing it back at all return points.
     *
     * @param method the method
     * @param globalVar the global variable
     */
    private void deglobalize(CFGMethod method, CFGAddress globalVar) {
// System.out.printf("Deglobalizing %s in %s%n", globalVar, method.getName());
        CFGAddress localVar = ctx.getSymbolTable().newNode(globalVar);
        if (method.getBlocks().isEmpty()) return;

        // replace all instances
        for (CFGBasicBlock block : method.getBlocks()) {
            for (CFGInstruction inst : block.getAllInstructions()) {
                if (inst.address() != null && inst.address().equals(globalVar))
                    inst.address().set(localVar);
                Queue<CFGAddress> workList = new LinkedList<>(inst.operands());
                while (!workList.isEmpty()) {
                    CFGAddress operand = workList.poll();
                    if (operand.equals(globalVar))
                        operand.set(localVar);
                }
            }
        }

        // insert load
        if (globalsUsed.get(method).contains(globalVar))
            method.getBlocks().peekFirst().getInstructions().offerFirst(
                    new CFGCopyInstruction(ctx, localVar, globalVar)
            );

        // insert stores
        if (globalsDefined.get(method).contains(globalVar)) {
            for (CFGBasicBlock block : method.getBlocks()) {
                if (block.getLastInstruction() instanceof CFGReturnInstruction) {
                    block.getInstructions().offerLast(
                            new CFGCopyInstruction(ctx, globalVar, localVar)
                    );
                }
            }
        }
    }

    private void computeSets() {
        globalVars = ctx.getGlobalVars().stream().filter(x -> !ctx.getSymbolTable().getVar(x).isArray()).collect(Collectors.toSet());
        globalsDefined = new HashMap<>();
        globalsUsed = new HashMap<>();
        globalsInteracted = new HashMap<>();
        callGraph = new HashMap<>();

        for (CFGMethod method : cfg.getMethods()) {
            globalsDefined.put(method, new HashSet<>());
            globalsUsed.put(method, new HashSet<>());
            callGraph.put(method, new HashSet<>());

            for (CFGBasicBlock block : method.getBlocks()) {
                for (CFGInstruction inst : block.getAllInstructions()) {
                    if (inst.address() != null && globalVars.contains(inst.address()))
                        globalsDefined.get(method).add(CFGAddress.clone(inst.address()));
                    for (CFGAddress operand : inst.operands())
                        if (globalVars.contains(operand))
                            globalsUsed.get(method).add(CFGAddress.clone(operand));
                    if (inst instanceof CFGMethodCallInstruction call) {
                        CFGMethod callee = ctx.getSymbolTable().getMethod(call.methodName());
                        if (callee == null) continue; // external call
                        callGraph.get(method).add(callee);
                    }
                }
            }

            globalsInteracted.put(method, new HashSet<>());
            globalsInteracted.get(method).addAll(globalsDefined.get(method));
            globalsInteracted.get(method).addAll(globalsUsed.get(method));
        }

        reachableMethods = new HashMap<>();
        for (CFGMethod method : cfg.getMethods()) {
            Set<CFGMethod> visited = new HashSet<>();
            for (CFGMethod callee : callGraph.get(method))
                dfs(callee, visited);
            reachableMethods.put(method, visited);
        }
    }

    private void dfs(CFGMethod method, Set<CFGMethod> visited) {
        if (visited.contains(method))
            return;
        visited.add(method);
        for (CFGMethod callee : callGraph.get(method))
            dfs(callee, visited);
    }

}
