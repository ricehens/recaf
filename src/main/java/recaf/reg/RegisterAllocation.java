package recaf.reg;

import recaf.asm.*;
import recaf.general.Type;
import recaf.cfg.CFGAddress;
import recaf.utils.DisjointSet;
import recaf.utils.DominatorTree;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds first Chaitin-Briggs-like graph-coloring register allocator acting on pseudoassembly generated
 * by InstructionSelection.
 */
public class RegisterAllocation {

    private AssemblyBuilder ab;
    private ASMContext ctx;
    private ASMProgram asm;

    private Map<ASMMethod, MethodRegisterAllocation> methodAllocators = new HashMap<>();

    private static final List<ASMRegister> physicalRegisters = List.of(
            ASMRegister.R12,
            ASMRegister.R13,
            ASMRegister.R14,
            ASMRegister.R15,
            ASMRegister.RBP,
            ASMRegister.RBX,
            ASMRegister.R10,
            ASMRegister.R11,
            ASMRegister.R9,
            ASMRegister.R8,
            ASMRegister.RSI,
            ASMRegister.RDI,
            ASMRegister.RCX,
            ASMRegister.RDX,
            ASMRegister.RAX
            // no RSP
    );

    private static final List<ASMRegister> leafPhysicalRegisterOrder = List.of(
            ASMRegister.R10,
            ASMRegister.R11,
            ASMRegister.R9,
            ASMRegister.R8,
            ASMRegister.RSI,
            ASMRegister.RDI,
            ASMRegister.RCX,
            ASMRegister.R12,
            ASMRegister.R13,
            ASMRegister.R14,
            ASMRegister.R15,
            ASMRegister.RBP,
            ASMRegister.RBX,
            ASMRegister.RDX,
            ASMRegister.RAX
            // no RSP
    );

    /*
    private static final List<ASMRegister> physicalRegisters = List.of(
            ASMRegister.R12,
            ASMRegister.R13,
            ASMRegister.R10,
            ASMRegister.R11
    );
    private static final List<ASMRegister> leafPhysicalRegisterOrder = List.of(
            ASMRegister.R10,
            ASMRegister.R11,
            ASMRegister.R12,
            ASMRegister.R13
    );
    */

    private static final Set<ASMRegister> calleeSave = Set.of(
            ASMRegister.RBX,
            ASMRegister.R12,
            ASMRegister.R13,
            ASMRegister.R14,
            ASMRegister.R15,
            ASMRegister.RBP
    );

    public RegisterAllocation(AssemblyBuilder ab) {
        this.ab = ab;
        asm = ab.asm();
        ctx = ab.ctx();
    }

    public void apply() {
        for (ASMMethod method : asm.getMethods()) {
            methodAllocators.put(method, new MethodRegisterAllocation(method));
            methodAllocators.get(method).apply();
        }
    }

    // for testing
    public MethodRegisterAllocation getMethodAllocator(ASMMethod method) {
        return methodAllocators.get(method);
    }

    public class MethodRegisterAllocation {

        /** The method being transformed */
        ASMMethod method;

        /** is leaf function? */
        boolean isLeaf;
        /** The dominator tree */
        DominatorTree<ASMBasicBlock> dt;
        /** The live out sets */
        Map<ASMBasicBlock, Set<ASMVirtualRegister>> liveOut;
        /** The live now sets (technically, live before each instruction) */
        Map<ASMInstruction, Set<ASMAbstractRegister>> liveBefore;
        /** The interference graph */
        Map<ASMAbstractRegister, Set<ASMAbstractRegister>> interferenceGraph;
        /** The loop depth --- number of loops first block is contained in */
        Map<ASMBasicBlock, Integer> loopDepth;
        /** The block containing each instruction */
        Map<ASMInstruction, ASMBasicBlock> parentBlock;
        /** The def sites of each register */
        Map<ASMAbstractRegister, Set<ASMInstruction>> defs;
        /** The use sites of each register */
        Map<ASMAbstractRegister, Set<ASMInstruction>> uses;
        /** The spill cost of each register */
        Map<ASMVirtualRegister, Double> spillCosts;
        /** The mapping from virtual registers to physical registers */
        Map<ASMAbstractRegister, ASMRegister> coloring;
        /** The live out sets for physical registers */
        Map<ASMBasicBlock, Set<ASMRegister>> regLiveOut;
        /** The live now sets for physical registers */
        Map<ASMInstruction, Set<ASMRegister>> regLiveAfter;

        public MethodRegisterAllocation(ASMMethod method) {
            this.method = method;
        }

        public void apply() {
            determineIfLeaf();
            dt = new DominatorTree<>(method.getBlocks());

            Set<ASMVirtualRegister> spill = new HashSet<>();
            do {
                spillAll(spill);
                loopAnalysis();

                boolean coalescing = true;
                while (coalescing) {
                    computeLiveness();
                    buildInterferenceGraph();
                    coalescing = copyCoalesce();
                }

                computeUseDef();
                computeSpillCosts();

                spill = color();
            } while (!spill.isEmpty());

            rename();
            computePhysicalLiveness();
            imposeCallingConvention();
            cleanCopies();
            registerScavenge();
        }

        /** determines if this function is first leaf function */
        private void determineIfLeaf() {
            isLeaf = true;
            for (ASMBasicBlock b : method.getBlocks()) {
                for (ASMInstruction inst : b.getInstructions()) {
                    if (inst.op() == ASMOperator.CALL) {
                        isLeaf = false;
                        return;
                    }
                }
            }
        }

        /**
         * Computes the set of variables live in each basic block,
         * storing the results in liveOut.
         */
        private void computeLiveness() {
            liveOut = new HashMap<>();

            Map<ASMBasicBlock, Set<ASMVirtualRegister>> ueVar = new HashMap<>();
            Map<ASMBasicBlock, Set<ASMVirtualRegister>> varKill = new HashMap<>();

            // compute uevar and varkill
            for (ASMBasicBlock b : method.getBlocks()) {
                liveOut.put(b, new HashSet<>());
                varKill.put(b, new HashSet<>());
                ueVar.put(b, new HashSet<>());

                for (ASMInstruction inst : b.getInstructions()) {
                    for (ASMAbstractRegister operand : inst.operandRegisters())
                        if (operand instanceof ASMVirtualRegister vr && !varKill.get(b).contains(vr))
                            ueVar.get(b).add(vr);
                    for (ASMAbstractRegister dest : inst.destinationRegisters())
                        if (dest instanceof ASMVirtualRegister vr)
                            varKill.get(b).add(vr);
                }
            }

            // compute liveOut
            boolean changed = true;
            while (changed) {
                changed = false;

                // LIVEOUT(n) = union(m in succ(n)) UEVAR(m) union (LIVEOUT(m) - VARKILL(m))
                for (ASMBasicBlock n : method.getBlocks()) {
                    Set<ASMVirtualRegister> newLiveOut = new HashSet<>();
                    for (ASMBasicBlock m : n.successors()) {
                        Set<ASMVirtualRegister> liveIn = new HashSet<>(liveOut.get(m));
                        liveIn.removeAll(varKill.get(m));
                        liveIn.addAll(ueVar.get(m));

                        newLiveOut.addAll(liveIn);
                    }

                    if (!newLiveOut.equals(liveOut.get(n))) {
                        changed = true;
                        liveOut.put(n, newLiveOut);
                    }
                }
            }
        }

        /**
         * Builds the interference graph
         */
        private void buildInterferenceGraph() {
            interferenceGraph = new HashMap<>();
            liveBefore = new HashMap<>();

            for (ASMBasicBlock b : method.getBlocks()) {
                Set<ASMAbstractRegister> liveNow = new HashSet<>(liveOut.get(b));

                for (ASMInstruction inst : b.getInstructions().reverse()) {
                    for (ASMAbstractRegister dest : inst.destinationRegisters())
                        liveNow.remove(dest);

                    for (ASMAbstractRegister live : liveNow) {
                        if (isCopy(inst) && live.equals(inst.src()))
                            continue;
                        if (live instanceof ASMRegister && !physicalRegisters.contains(live))
                            continue;

                        for (ASMAbstractRegister dest : inst.destinationRegisters()) {
                            if (dest instanceof ASMRegister && !physicalRegisters.contains(dest))
                                continue;
                            interferenceGraph.computeIfAbsent(live, k -> new HashSet<>()).add(dest);
                            interferenceGraph.computeIfAbsent(dest, k -> new HashSet<>()).add(live);
                        }
                    }

                    liveNow.addAll(inst.operandRegisters());
                    liveBefore.put(inst, new HashSet<>(liveNow));
                }
            }
        }

        /**
         * Performs copy coalescing
         */
        private boolean copyCoalesce() {
            // TODO, prioritize copies inside loops?; see Cooper section 13.4.3
// System.out.println(interferenceGraph);
            boolean changed = false;
            DisjointSet<ASMVirtualRegister> ds = new DisjointSet<>();

            List<ASMInstruction> copies = new ArrayList<>();
            Map<ASMInstruction, ASMBasicBlock> parentBlock = new HashMap<>();
            for (ASMBasicBlock b : method.getBlocks()) {
                for (ASMInstruction inst : b.getInstructions()) {
                    if (isCopy(inst)) {
                        copies.add(inst);
                        parentBlock.put(inst, b);
                    }
                }
            }
            copies.sort(Comparator.comparingInt(inst -> -loopDepth.get(parentBlock.get(inst))));

            // compute possible coalesces
            for (ASMInstruction inst : copies) {
                    /*
                    if (inst.src() instanceof ASMAbstractRegister src
                            && inst.dest() instanceof ASMAbstractRegister dest) {
                     */
                if (inst.src() instanceof ASMVirtualRegister src
                        && inst.dest() instanceof ASMVirtualRegister dest) {

                    src = ds.find(src);
                    dest = ds.find(dest);

                        /*
                        if (src instanceof ASMRegister || dest instanceof ASMRegister)
                            continue;
                        if (src instanceof ASMRegister pr) {
                            src = pr.toType(Type.LONG);
                            if (!physicalRegisters.contains(src))
                                continue;
                        }
                        if (dest instanceof ASMRegister pr) {
                            dest = pr.toType(Type.LONG);
                            if (!physicalRegisters.contains(dest))
                                continue;
                        }
                         */

                    if (interferenceGraph.containsKey(src) && interferenceGraph.get(src).contains(dest))
                        continue;

                    // we can coalesce
// System.out.printf("Coalescing %s with %s%n", src, dest);
                        /*
                        if (src instanceof ASMRegister)
                            ds.subsume(src, dest);
                        else if (dest instanceof ASMRegister)
                            ds.subsume(dest, src);
                        else */
                    ds.union(src, dest);

                    ASMAbstractRegister root = ds.find(src);

                    if (!src.equals(root)) {
                        changed = true;
                        for (ASMAbstractRegister x : interferenceGraph.getOrDefault(src, new HashSet<>())) {
                            interferenceGraph.computeIfAbsent(root, k -> new HashSet<>()).add(x);
                            interferenceGraph.computeIfAbsent(x, k -> new HashSet<>()).add(root);
                        }
                    }
                    if (!dest.equals(root)) {
                        changed = true;
                        for (ASMAbstractRegister x : interferenceGraph.getOrDefault(dest, new HashSet<>())) {
                            interferenceGraph.computeIfAbsent(root, k -> new HashSet<>()).add(x);
                            interferenceGraph.computeIfAbsent(x, k -> new HashSet<>()).add(root);
                        }
                    }
                }
            }

            // effect changes
            for (ASMBasicBlock b : method.getBlocks()) {
                for (ASMInstruction inst : b.getInstructions()) {
                    if (inst.src() != null && inst.src() instanceof ASMVirtualRegister src) {
                        // ASMAbstractRegister newSrc = ds.find(src);
                        ASMVirtualRegister newSrc = ds.find(src);
                        /*
                        if (newSrc instanceof ASMRegister pr) {
                            if (inst.op().src != null && inst.op().src != Type.UNKNOWN)
                                newSrc = pr.toType(inst.op().src);
                            else newSrc = pr.toType(getType(src));
                        }
                         */
// if (newSrc instanceof ASMRegister pr) System.out.println(getType(src));
                        if (!src.equals(newSrc)) {
// System.out.printf("Replacing %s with %s%n", inst, newSrc);
                            b.getInstructions().replace(inst,
                                    inst = new ASMInstruction(inst.ctx(), inst.op(), newSrc, inst.dest(), inst.specificCtx()));
                            // ^see java language specification 15.7.4 on evaluation order :P
                            changed = true;
                        }
                    }
                    if (inst.dest() != null && inst.dest() instanceof ASMVirtualRegister dest) {
                        // ASMAbstractRegister newDest = ds.find(dest);
                        ASMVirtualRegister newDest = ds.find(dest);
                        /*
                        if (newDest instanceof ASMRegister pr) {
                            if (inst.op().dest != null && inst.op().dest != Type.UNKNOWN)
                                newDest = pr.toType(inst.op().dest);
                            else newDest = pr.toType(getType(dest));
                        }
                         */
                        if (!dest.equals(newDest)) {
                            b.getInstructions().replace(inst,
                                    new ASMInstruction(inst.ctx(), inst.op(), inst.src(), newDest, inst.specificCtx()));
                            changed = true;
                        }
                    }
                }
            }

            // remove useless copy instructions
            cleanCopies();

            return changed;
        }

        /** Remove useless copy instructions */
        private void cleanCopies() {
            for (ASMBasicBlock b : method.getBlocks()) {
                Queue<ASMInstruction> workList = new LinkedList<>(b.getInstructions().stream().toList());
                while (!workList.isEmpty()) {
                    ASMInstruction inst = workList.poll();
                    if (!isCopy(inst)) continue;
                    if (!(inst.src() instanceof ASMAbstractRegister)
                            || !(inst.dest() instanceof ASMAbstractRegister))
                        continue;
                    if (inst.src().equals(inst.dest()))
                        b.getInstructions().remove(inst);
                }
            }
        }

        private Type getType(ASMAbstractRegister reg) {
            if (reg instanceof ASMRegister pr)
                return pr.getType();
            else if (reg instanceof ASMVirtualRegister vr)
                return ctx.getCfgCtx().getType(vr.address());
            else throw new RuntimeException("This should never happen.");
        }

        private boolean isCopy(ASMInstruction inst) {
            return inst.op() == ASMOperator.MOVQ || inst.op() == ASMOperator.MOVL || inst.op() == ASMOperator.MOVB;
        }

        private boolean isLoad(ASMInstruction inst) {
            return isCopy(inst) && inst.src() instanceof ASMMemoryLocation && inst.dest() instanceof ASMAbstractRegister;
        }

        private boolean isStore(ASMInstruction inst) {
            return isCopy(inst) && inst.src() instanceof ASMAbstractRegister && inst.dest() instanceof ASMMemoryLocation;
        }

        /**
         * Computes the loop depth of each basic block
         */
        private void loopAnalysis() {
            loopDepth = new HashMap<>();
            for (ASMBasicBlock b : method.getBlocks()) {
                loopDepth.put(b, 0);
            }

            for (ASMBasicBlock b : method.getBlocks()) {
                for (ASMBasicBlock succ : b.successors()) {
                    if (!dt.dominates(succ, b)) continue;
                    // identified back edge

                    // compute all blocks in loop
                    Set<ASMBasicBlock> blocks = new HashSet<>();
                    Queue<ASMBasicBlock> workList = new LinkedList<>();
                    blocks.add(succ);
                    workList.offer(b);

                    while (!workList.isEmpty()) {
                        ASMBasicBlock block = workList.poll();
                        if (blocks.contains(block)) continue;
                        blocks.add(block);
                        workList.addAll(dt.getPredecessors(block));
                    }

                    // update loop depths
                    for (ASMBasicBlock block : blocks) {
                        loopDepth.put(block, loopDepth.get(block) + 1);
                    }
                }
            }
        }

        /**
         * Computes the use and def sets for each register
         */
        private void computeUseDef() {
            parentBlock = new HashMap<>();
            defs = new HashMap<>();
            uses = new HashMap<>();
            for (ASMBasicBlock b : method.getBlocks()) {
                for (ASMInstruction inst : b.getInstructions()) {
                    parentBlock.put(inst, b);
                    for (ASMAbstractRegister operand : inst.operandRegisters()) {
                        uses.computeIfAbsent(operand, k -> new HashSet<>()).add(inst);
                        defs.putIfAbsent(operand, new HashSet<>());
                        interferenceGraph.computeIfAbsent(operand, k -> new HashSet<>());
                    }
                    for (ASMAbstractRegister dest : inst.destinationRegisters()) {
                        defs.computeIfAbsent(dest, k -> new HashSet<>()).add(inst);
                        uses.putIfAbsent(dest, new HashSet<>());
                        interferenceGraph.computeIfAbsent(dest, k -> new HashSet<>());
                    }
                }
            }
        }

        /**
         * Computes first spill cost heuristic for each virtual register
         */
        private void computeSpillCosts() {
            spillCosts = new HashMap<>();

            for (ASMAbstractRegister reg : defs.keySet()) {
                if (!(reg instanceof ASMVirtualRegister vr)) continue;

                boolean rematerializable = rematerializable(vr) != null;

                double cost = 0;
                for (ASMInstruction inst : defs.get(vr)) {
                    ASMBasicBlock b = parentBlock.get(inst);
                    cost += rematerializable ? 0 : 2 * Math.pow(10, loopDepth.get(b));
                }
                Set<ASMBasicBlock> defBlocks = defs.get(vr).stream().map(parentBlock::get).collect(Collectors.toSet());
                for (ASMInstruction inst : uses.get(vr)) {
                    ASMBasicBlock b = parentBlock.get(inst);
                    //boolean couldBeScavenged = !defBlocks.contains(second);
                    cost += 3 * Math.pow(10, loopDepth.get(b)) * (rematerializable ? 0.5 : 1.0) /** (couldBeScavenged ? 0.5 : 1) */;
                }

                // negative spill cost
                ASMInstruction use;
                if (uses.get(vr).size() == 1 && isStore(use = uses.get(vr).iterator().next())) {
                    ASMInstruction def;
                    if (defs.get(vr).size() == 1 && isLoad(def = defs.get(vr).iterator().next())
                            && def.src().equals(use.src())) {
                        cost = -1;
                    }
                }

                // infinite spill cost
                if (infiniteSpillCost(vr)) {
                    cost = Double.POSITIVE_INFINITY;
                }

                spillCosts.put(vr, cost);
            }
        }

        /** Helper to determine if spill cost should be infinite */
        private boolean infiniteSpillCost(ASMVirtualRegister vr) {
            Set<ASMBasicBlock> activeBlocks = new HashSet<>();

            for (ASMInstruction inst : defs.get(vr)) {
                ASMBasicBlock b = parentBlock.get(inst);
                activeBlocks.add(b);
            }

            for (ASMInstruction inst : uses.get(vr)) {
                ASMBasicBlock b = parentBlock.get(inst);
                activeBlocks.add(b);
            }

            // don't consider live ranges in multiple blocks
            if (activeBlocks.size() != 1)
                return false;

            ASMBasicBlock block = activeBlocks.iterator().next();
            ASMInstruction firstDef = null;
            ASMInstruction lastUse = null;

            for (ASMInstruction inst : block.getInstructions()) {
                if (defs.get(vr).contains(inst)) {
                    if (firstDef == null)
                        firstDef = inst;
                }
                if (uses.get(vr).contains(inst)) {
                    lastUse = inst;
                    if (firstDef == null)
                        return false; // wtf?
                }
            }

            if (firstDef == null || lastUse == null)
                return false;

            Set<ASMAbstractRegister> liveNow = new HashSet<>(liveOut.get(block));

            Set<ASMAbstractRegister> liveAtDef = null;
            Set<ASMAbstractRegister> liveAtUse = null;
            for (ASMInstruction inst : block.getInstructions().reverse()) {
                if (inst.equals(lastUse))
                    liveAtUse = new HashSet<>(liveNow);

                for (ASMAbstractRegister dest : inst.destinationRegisters())
                    if (dest instanceof ASMVirtualRegister vdest)
                        liveNow.remove(vdest);

                for (ASMAbstractRegister live : liveNow) {
                    if (isCopy(inst) && live.equals(inst.src()))
                        continue;

                    for (ASMAbstractRegister dest : inst.destinationRegisters()) {
                        interferenceGraph.computeIfAbsent(live, k -> new HashSet<>()).add(dest);
                        interferenceGraph.computeIfAbsent(dest, k -> new HashSet<>()).add(live);
                    }
                }

                inst.operandRegisters().stream().filter(r -> r instanceof ASMVirtualRegister)
                        .map(r -> (ASMVirtualRegister) r).forEach(liveNow::add);

                if (inst.equals(firstDef))
                    liveAtDef = new HashSet<>(liveNow);
            }

            if (liveAtDef == null || liveAtUse == null)
                throw new RuntimeException("This should never happen.");

// System.out.printf("liveAtDef[%s] = %s%nliveAtUse[%s] = %s%n", vr, liveAtDef, vr, liveAtUse);
            liveAtDef.remove(vr);
            liveAtDef.removeAll(liveAtUse);
            return liveAtDef.isEmpty();
        }

        /**
         * Computes first coloring for each register to first physical register.
         * @return the set of uncolored registers
         */
        private Set<ASMVirtualRegister> color() {
            int K = physicalRegisters.size();
            Set<ASMVirtualRegister> spills = new HashSet<>();
            Stack<ASMVirtualRegister> stack = new Stack<>();

            Map<ASMVirtualRegister, Set<ASMAbstractRegister>>
                    interferenceGraphCopy = new HashMap<>();
            for (ASMAbstractRegister key : interferenceGraph.keySet())
                if (key instanceof ASMVirtualRegister vr)
                    interferenceGraphCopy.put(vr, new HashSet<>(interferenceGraph.get(key)));

            // simplify --- choose coloring order
            while (!interferenceGraphCopy.isEmpty()) {
                ASMVirtualRegister n = null;
                for (ASMVirtualRegister vr : interferenceGraphCopy.keySet()) {
                    if (interferenceGraphCopy.get(vr).size() < K) {
                        n = vr;
                        break;
                    }
                }

                if (n == null) {
                    double minSpill = Double.POSITIVE_INFINITY;
                    for (ASMVirtualRegister vr : interferenceGraphCopy.keySet()) {
                        if (spillCosts.get(vr) < minSpill) {
                            minSpill = spillCosts.get(vr);
                            n = vr;
                        }
                    }
                }

                // I think this means we need to spill an infinite-spill-cost register
                if (n == null)
                    n = interferenceGraphCopy.keySet().iterator().next();

                stack.push(n);
                for (ASMAbstractRegister dest : interferenceGraphCopy.get(n))
                    if (dest instanceof ASMVirtualRegister vr)
                        interferenceGraphCopy.computeIfAbsent(vr, k -> new HashSet<>()).remove(n);
                interferenceGraphCopy.remove(n);
            }

            // compute copy graph
            Map<ASMAbstractRegister, Map<ASMInstruction, ASMAbstractRegister>> copies = new HashMap<>();
            for (ASMBasicBlock block : method.getBlocks()) {
                for (ASMInstruction inst : block.getInstructions()) {
                    if (isCopy(inst)) {
                        // insert copy if both are abstract registers
                        // note .operandRegisters and .destinationRegisters takes care of conversion to longs
                        for (ASMAbstractRegister src : inst.operandRegisters()) {
                            for (ASMAbstractRegister dest : inst.destinationRegisters()) {
                                copies.computeIfAbsent(src, k -> new HashMap<>()).put(inst, dest);
                                copies.computeIfAbsent(dest, k -> new HashMap<>()).put(inst, src);
                            }
                        }
                    }
                }
            }

            // select --- attempt to color
            coloring = new HashMap<>();
            for (ASMRegister pr : physicalRegisters) {
                coloring.put(pr, pr);
            }
            while (!stack.isEmpty()) {
                ASMVirtualRegister n = stack.pop();
                List<ASMRegister> available = new LinkedList<>(isLeaf ? leafPhysicalRegisterOrder : physicalRegisters);
                for (ASMAbstractRegister neighbor : interferenceGraph.get(n))
                    if (coloring.containsKey(neighbor))
                        available.remove(coloring.get(neighbor));

                if (available.isEmpty()) {
                    spills.add(n);
                } else {
                    Map<ASMRegister, Double> partners = new HashMap<>();
                    for (ASMRegister avail : available) partners.put(avail, 0.0);
                    if (copies.containsKey(n)) {
                        for (ASMInstruction copy : copies.get(n).keySet()) {
                            ASMAbstractRegister neighbor = copies.get(n).get(copy);
                            if (coloring.containsKey(neighbor)) {
                                ASMRegister pr = coloring.get(neighbor);
                                if (partners.containsKey(pr))
                                    partners.put(pr, partners.get(pr) + Math.pow(10, loopDepth.get(parentBlock.get(copy))));
                            }
                        }
                    }

                    ASMRegister best = null;
                    double mostPartners = -1;
                    for (ASMRegister pr : available) {
                        if (partners.get(pr) > mostPartners) {
                            best = pr;
                            mostPartners = partners.get(pr);
                        }
                    }

                    coloring.put(n, best);
                }
            }

            return spills;
        }

        /**
         * Spills all registers in spill via first spill-everywhere discipline.
         * @param spill the registers to spill
         */
        private void spillAll(Set<ASMVirtualRegister> spill) {
// System.out.printf("Spilling %s%n", spill);
            spill.forEach(vr -> {
                if (!rematerialize(vr)) ab.spill(method, vr);
            });
        }

        /**
         * Determines if first virtual register can be rematerialized
         * @param vr the virtual register
         * @return first reference definition if it can be rematerialized
         */
        private ASMInstruction rematerializable(ASMVirtualRegister vr) {
            String literal = null;
            ASMInstruction referenceDef = null;
            for (ASMInstruction def : defs.get(vr)) {
                if (def.op() != ASMOperator.MOVB && def.op() != ASMOperator.MOVL && def.op() != ASMOperator.MOVQ && def.op() != ASMOperator.MOVABSQ)
                    return null;
                if (!(def.src() instanceof ASMLiteral lit))
                    return null;
                if (literal == null) {
                    literal = lit.getValue();
                    referenceDef = def;
                } else if (!literal.equals(lit.getValue()))
                    return null;
            }
            return referenceDef;
        }

        /**
         * Attempts to rematerialize first virtual register (instead of spilling)
         * @param vr the virtual register
         * @return whether the rematerialization succeeded
         * (if false, spilling must occur)
         */
        private boolean rematerialize(ASMVirtualRegister vr) {
            ASMInstruction referenceDef = rematerializable(vr);
            if (referenceDef == null)
                return false;
// System.out.printf("Rematerializing %s%n", vr);
            for (ASMBasicBlock b : method.getBlocks()) {
                Queue<ASMInstruction> workList = new LinkedList<>(b.getInstructions().stream().toList());
                while (!workList.isEmpty()) {
                    ASMInstruction inst = workList.poll();
                    if (inst.destinationRegisters().contains(vr)) {
                        b.getInstructions().remove(inst);
                    } else if (inst.operandRegisters().contains(vr)) {
                        ASMVirtualRegister newVr = new ASMVirtualRegister(ctx.getCfgCtx().getSymbolTable().newNode(vr.address()));
                        b.getInstructions().insertBefore(inst, new ASMInstruction(inst.ctx(), referenceDef.op(), referenceDef.src(), newVr, inst.specificCtx()));
                        if (vr.equals(inst.src()))
                            b.getInstructions().replace(inst,
                                    inst = new ASMInstruction(inst.ctx(), inst.op(), newVr, inst.dest(), inst.specificCtx()));
                        if (vr.equals(inst.dest()))
                            b.getInstructions().replace(inst,
                                    new ASMInstruction(inst.ctx(), inst.op(), inst.src(), newVr, inst.specificCtx()));
                    }
                }
            }
            return true;
        }

        /**
         * Renames virtual registers according to the mapping given in coloring.
         */
        private void rename() {
            for (ASMBasicBlock b : method.getBlocks()) {
                for (ASMInstruction inst : b.getInstructions()) {
                    if (inst.src() != null && inst.src() instanceof ASMVirtualRegister src) {
                        ASMRegister newSrc;
                        if (inst.op().src != null && inst.op().src != Type.UNKNOWN)
                            newSrc = coloring.get(src).toType(inst.op().src);
                        else
                            newSrc = coloring.get(src).toType(getType(src));
// System.out.printf("Replacing %s with %s%n", inst, newSrc);
                        b.getInstructions().replace(inst,
                                inst = new ASMInstruction(inst.ctx(), inst.op(), newSrc, inst.dest(), inst.specificCtx()));
                    }
                    if (inst.dest() != null && inst.dest() instanceof ASMVirtualRegister dest) {
                        ASMRegister newDest;
                        if (inst.op().dest != null && inst.op().dest != Type.UNKNOWN)
                            newDest = coloring.get(dest).toType(inst.op().dest);
                        else
                            newDest = coloring.get(dest).toType(getType(dest));
                        b.getInstructions().replace(inst,
                                new ASMInstruction(inst.ctx(), inst.op(), inst.src(), newDest, inst.specificCtx()));
                    }
                }
            }
        }

        /** Compute physical register liveness */
        private void computePhysicalLiveness() {
            regLiveOut = new HashMap<>();

            Map<ASMBasicBlock, Set<ASMRegister>> ueVar = new HashMap<>();
            Map<ASMBasicBlock, Set<ASMRegister>> varKill = new HashMap<>();

            // compute uevar and varkill
            for (ASMBasicBlock b : method.getBlocks()) {
                regLiveOut.put(b, new HashSet<>());
                varKill.put(b, new HashSet<>());
                ueVar.put(b, new HashSet<>());

                for (ASMInstruction inst : b.getInstructions()) {
                    for (ASMAbstractRegister operand : inst.operandRegisters())
                        if (operand instanceof ASMRegister pr && !varKill.get(b).contains(pr))
                            ueVar.get(b).add(pr);
                    for (ASMAbstractRegister dest : inst.destinationRegisters())
                        if (dest instanceof ASMRegister pr)
                            varKill.get(b).add(pr);
                }
            }

            // compute liveOut
            boolean changed = true;
            while (changed) {
                changed = false;

                // LIVEOUT(n) = union(m in succ(n)) UEVAR(m) union (LIVEOUT(m) - VARKILL(m))
                for (ASMBasicBlock n : method.getBlocks()) {
                    Set<ASMRegister> newLiveOut = new HashSet<>();
                    for (ASMBasicBlock m : n.successors()) {
                        Set<ASMRegister> liveIn = new HashSet<>(regLiveOut.get(m));
                        liveIn.removeAll(varKill.get(m));
                        liveIn.addAll(ueVar.get(m));

                        newLiveOut.addAll(liveIn);
                    }

                    if (!newLiveOut.equals(regLiveOut.get(n))) {
                        changed = true;
                        regLiveOut.put(n, newLiveOut);
                    }
                }
            }

            // compute live now sets
            regLiveAfter = new HashMap<>();
            for (ASMBasicBlock b : method.getBlocks()) {
                Set<ASMRegister> liveNow = new HashSet<>(regLiveOut.get(b));

                for (ASMInstruction inst : b.getInstructions().reverse()) {
                    regLiveAfter.put(inst, new HashSet<>(liveNow));
                    for (ASMAbstractRegister dest : inst.destinationRegisters())
                        if (dest instanceof ASMRegister pr)
                            liveNow.remove(pr);

                    liveNow.addAll(inst.operandRegisters().stream()
                            .filter(r -> r instanceof ASMRegister).map(r -> (ASMRegister) r).collect(Collectors.toSet()));
                }
            }
        }

        /**
         * Imposes the System-V calling convention on the method
         */
        private void imposeCallingConvention() {
            Set<ASMRegister> toCalleeSave = new HashSet<>();
            Map<ASMRegister, Set<ASMInstruction>> toCallerSave = new HashMap<>();
            Map<ASMInstruction, ASMBasicBlock> callerSaveBlocks = new HashMap<>();

            for (ASMBasicBlock b : method.getBlocks()) {
                for (ASMInstruction inst : b.getInstructions()) {
                    if (inst.op() == ASMOperator.CALL) {
// System.out.printf("liveBefore[%s] = %s%n", inst, liveBefore.get(inst));
                        for (ASMRegister pr : regLiveAfter.get(inst)) {
                            if (physicalRegisters.contains(pr)
                                    && !calleeSave.contains(pr)
                                    && !inst.destinationRegisters().contains(pr)) {
                                toCallerSave.computeIfAbsent(pr, k -> new HashSet<>()).add(inst);
                                callerSaveBlocks.put(inst, b);
                            }
                        }
                    }
                    /*
                    for (ASMAbstractRegister operand : inst.operandRegisters())
                        if (operand instanceof ASMRegister pr
                                && physicalRegisters.contains(pr)
                                && !calleeSave.contains(pr))
                            toCallerSave.add(pr);
                     */

                    for (ASMAbstractRegister dest : inst.destinationRegisters()) {
                        if (dest instanceof ASMRegister pr
                                && physicalRegisters.contains(pr)
                                && calleeSave.contains(pr)) {
                            toCalleeSave.add(pr);
                        }
                    }
                }
            }

// System.out.printf("For method %s, callee-saving %s%n", method.getName(), toCalleeSave);
// System.out.printf("For method %s, caller-saving %s%n", method.getName(), toCallerSave);

            // save and restore callee-save registers
            if (needCalleeSave()) {
                ASMBasicBlock prologue = method.getBlocks().peekFirst();
                ASMInstruction lastPrepInst = prologue.getInstructions().peekFirst();
                while (true) {
                    ASMInstruction next = prologue.getInstructions().next(lastPrepInst);
                    if (next == null) break;
                    if (next.dest() != ASMRegister.RBP && next.dest() != ASMRegister.RSP)
                        break;
                    lastPrepInst = next;
                }
                for (ASMRegister r : toCalleeSave) {
                    CFGAddress addr = ctx.getCfgCtx().getSymbolTable().addVar(Type.LONG);
                    ab.registerVariable(method, addr);
                    prologue.getInstructions().insertAfter(lastPrepInst,
                            new ASMInstruction(ctx, ASMOperator.MOVQ, r, ab.getMemoryLocation(method, addr))
                    );

                    for (ASMBasicBlock b : method.getBlocks()) {
                        for (ASMInstruction inst : b.getInstructions()) {
                            if (inst.op() == ASMOperator.LEAVE) {
                                b.getInstructions().insertBefore(inst,
                                        new ASMInstruction(ctx, ASMOperator.MOVQ, ab.getMemoryLocation(method, addr), r)
                                );
                            }
                        }
                    }
                }
            }

            // save and restore caller-save registers
            for (ASMRegister r : toCallerSave.keySet()) {
                CFGAddress addr = ctx.getCfgCtx().getSymbolTable().addVar(Type.LONG);
                ab.registerVariable(method, addr);

                for (ASMInstruction inst : toCallerSave.get(r)) {
                    ASMBasicBlock b = callerSaveBlocks.get(inst);

                    if (inst.op() == ASMOperator.CALL) {
                        if (/*inst.operandRegisters().contains(r) || */
                                inst.destinationRegisters().contains(r))
                            continue;

                        b.getInstructions().insertBefore(inst,
                                new ASMInstruction(ctx, ASMOperator.MOVQ, r,
                                        RegUtils.stackShift(ab.getMemoryLocation(method, addr), inst.specificCtx().pushed))
                        );
                        b.getInstructions().insertAfter(inst,
                                new ASMInstruction(ctx, ASMOperator.MOVQ,
                                        RegUtils.stackShift(ab.getMemoryLocation(method, addr), inst.specificCtx().pushed),
                                        r)
                        );
                    }
                }
            }

            // replace leaves with addq
            for (ASMBasicBlock b : method.getBlocks()) {
                for (ASMInstruction inst : b.getInstructions()) {
                    if (inst.op() == ASMOperator.LEAVE) {
                        b.getInstructions().replace(inst,
                                new ASMInstruction(ctx, ASMOperator.ADDQ, ab.getAlignedStackOffset(method), ASMRegister.RSP));
                    }
                }
            }

        }

        private boolean needCalleeSave() {
            if (!method.getName().equals("main")) return true;
            for (ASMMethod m : asm.getMethods())
                for (ASMBasicBlock b : m.getBlocks())
                    for (ASMInstruction inst : b.getInstructions())
                        if (inst.op() == ASMOperator.CALL && ((ASMLabel) inst.dest()).getLabel().equals("main"))
                            return true;
            return false;
        }

        /** mov coalescing with the goal of eliminating unneeded spills */
        private void registerScavenge() {
            for (ASMBasicBlock block : method.getBlocks()) {
                Queue<ASMInstruction> workList = new LinkedList<>(block.getInstructions().stream().toList());
                while (!workList.isEmpty()) {
                    ASMInstruction spill = workList.poll();
                    if (spill.op() != ASMOperator.MOVQ && spill.op() != ASMOperator.MOVL && spill.op() != ASMOperator.MOVB) continue;
                    if (!(spill.src() instanceof ASMRegister pr)) continue;
                    pr = pr.toType(Type.LONG);
                    if (!(spill.dest() instanceof ASMStackAddress saddr)) continue;
                    if (saddr.getRegister() != ASMRegister.RSP) continue;
                    int location = saddr.getOffset() + saddr.getAdditionalOffset().getValue();
                    int pushed = 0;
                    boolean clean = true;
                    boolean prunable = true;

                    ASMInstruction next = block.getInstructions().next(spill);
                    while (next != null) {
                        // replace with pr if possible
                        if ((next.op() == ASMOperator.MOVQ || next.op() == ASMOperator.MOVL || next.op() == ASMOperator.MOVB)
                                && next.src() instanceof ASMStackAddress saddr2 && saddr2.getRegister() == ASMRegister.RSP
                                && location + pushed == saddr2.getOffset() + saddr2.getAdditionalOffset().getValue()) {
                            if (clean) {
// System.out.printf("Scavenger: replacing %s with %s (from spill %s) (pushed = %d)%n", next, pr, spill, pushed);
                                block.getInstructions().replace(next, next = new ASMInstruction(next.ctx(), next.op(),
                                        pr.toType(next.op().src), next.dest(), next.specificCtx()));
                            } else prunable = false;
                        }
                        if ((next.op() == ASMOperator.MOVQ || next.op() == ASMOperator.MOVL || next.op() == ASMOperator.MOVB)
                                && next.dest() instanceof ASMStackAddress saddr2 && saddr2.getRegister() == ASMRegister.RSP
                                && location + pushed == saddr2.getOffset() + saddr2.getAdditionalOffset().getValue()) {
                            if (prunable) {
// System.out.printf("Scavenger: removing %s%n", next);
                                block.getInstructions().remove(spill);
                            }
                            break;
                        }
                        if (next.destinationRegisters().contains(pr))
                            clean = false;

                        // proceed
                        if (next.op() == ASMOperator.CALL && !calleeSave.contains(pr)) break;
                        if (next.op() == ASMOperator.PUSHQ) pushed += 8;
                        if (next.dest() == ASMRegister.RSP && next.op() == ASMOperator.SUBQ) {
                            if (next.src() instanceof ASMLiteral lit) pushed += Integer.parseInt(lit.getValue());
                            else break;
                        }
                        if (next.dest() == ASMRegister.RSP && next.op() == ASMOperator.ADDQ) {
                            if (next.src() instanceof ASMLiteral lit) pushed -= Integer.parseInt(lit.getValue());
                            else break;
                        }
                        next = block.getInstructions().next(next);
                    }
                }
            }
        }

    }

}