package recaf.reg;

import recaf.cfg.*;
import recaf.opt.CriticalEdgeSplitting;
import recaf.opt.OptUtils;
import recaf.opt.SSAData;

import java.util.*;
import java.util.stream.Collectors;

@Deprecated
public class HackRegisterAllocator {

    /** The program being transformed */
    private final CFGProgram cfg;
    /** The CFG context */
    private final CFGContext ctx;

    /** The SSAData instances for all methods */
    private Map<CFGMethod, SSAData> data;

    /** The number of registers available */
    private final int numRegisters;
    /** The physical registers available */
    private PhysicalRegister[] registers;
    /** Additional scratch register */
    private PhysicalRegister scratch;

    /** Memory addresses for spilled and global variables, etc. */
    private Map<CFGAddress, MemoryAddress> memoryAddresses;

    /** Liveness entering first block, after phi instructions */
    private Map<CFGBasicBlock, Set<CFGAddress>> liveIn;
    /** Liveness exiting first block */
    private Map<CFGBasicBlock, Set<CFGAddress>> liveOut;
    /** live entering first particular instruction; undefined for phi instructions */
    private Map<CFGInstruction, Set<CFGAddress>> liveNow;

    /** in_B: the set of all v in I used before they are displaced (see spilling) */
    private Map<CFGBasicBlock, Set<CFGAddress>> in;
    /** out_B: the value of Q after processing each block (see spilling) */
    private Map<CFGBasicBlock, Set<CFGAddress>> out;

    /** Reload or rematerialization operations */
    private Map<CFGAddress, Set<CFGInstruction>> reloads;
    /** Spill operations */
    private Map<CFGAddress, Set<CFGSpillInstruction>> spills;
    /** phi-spills */
    private Set<CFGAddress> phiSpills;

    public HackRegisterAllocator(CFGProgram cfg, int numRegisters) {
        this.cfg = cfg;
        ctx = cfg.ctx();
        this.numRegisters = numRegisters;
    }

    public void apply() {
        // split critical edges
        cfg.getMethods().forEach(m -> new CriticalEdgeSplitting(ctx, m).apply());

        // initialize registers
        initRegisters();

        // initialize memory addresses
        initMemoryAddresses();

        // compute SSA data
        computeData();

        // compute liveness information
        cfg.getMethods().forEach(this::computeLiveness);

        // spilling
        cfg.getMethods().forEach(this::spilling);
    }

    /** Initialize the correct number of registers */
    private void initRegisters() {
        registers = new PhysicalRegister[numRegisters];
        for (int i = 0; i < numRegisters; i++) {
            registers[i] = new PhysicalRegister(i);
        }
        scratch = new PhysicalRegister(numRegisters);
    }

    /**
     * Initialize global variables and appropriate method parameters
     * to memory addresses, storing the results in memoryAddresses.
     */
    private void initMemoryAddresses() {
        memoryAddresses = new HashMap<>();
        for (CFGAddress global : ctx.getGlobalVars()) {
            memoryAddresses.put(global, new MemoryAddress());
        }

        for (CFGMethod method : cfg.getMethods()) {
            for (int i = 6; i < method.getParams().size(); i++) {
                CFGAddress param = method.getParams().get(i);
                memoryAddresses.put(param, new MemoryAddress());
            }
        }
    }

    /**
     * Compute SSA data instances for all methods
     */
    private void computeData() {
        data = new HashMap<>();
        for (CFGMethod method : cfg.getMethods()) {
            data.put(method, new SSAData(method));
        }
    }

    /** UEVAR(m, n) = upwardly-exposed variables from m to n */
    private Map<CFGBasicBlock, Map<CFGBasicBlock, Set<CFGAddress>>> ueVar;
    /** VARKILL(n) = variables defined in n */
    private Map<CFGBasicBlock, Set<CFGAddress>> varKill;

    /**
     * Computes the set of variables live in each basic block,
     * storing the results in liveIn and liveOut.
     * TODO rematerialization of literals
     *
     * @param method the method
     */
    private void computeLiveness(CFGMethod method) {
        liveIn = new HashMap<>();
        liveOut = new HashMap<>();
        ueVar = new HashMap<>();
        varKill = new HashMap<>();

        // compute uevar and varkill
        for (CFGBasicBlock b : method.getBlocks()) {
            liveOut.put(b, new HashSet<>());
            varKill.put(b, new HashSet<>());
            ueVar.put(b, new HashMap<>());

            Set<CFGBasicBlock> preds = data.get(method).getDominatorTree().getPredecessors(b);
            for (CFGBasicBlock pred : preds) {
                ueVar.get(b).put(pred, new HashSet<>());
            }

            Set<CFGAddress> phiDefined = new HashSet<>();
            for (CFGInstruction inst : b.getAllInstructions()) {
                if (inst instanceof CFGPhiInstruction phi) {
                    phiDefined.add(phi.address());
                    for (CFGBasicBlock pred : preds) {
                        CFGAddress operand = phi.getSources().get(pred);
                        if (operand == null) continue;
                        if (!varKill.get(b).contains(operand))
                            ueVar.get(b).get(pred).add(operand);
                    }
                } else {
                    for (CFGBasicBlock pred : preds)
                        inst.operands().stream()
                                .filter(x -> !varKill.get(b).contains(x) && !phiDefined.contains(x))
                                .forEach(ueVar.get(b).get(pred)::add);
                }
                varKill.get(b).add(inst.address());
            }
        }

        // solve dataflow equations
        // LIVEOUT(n) = union(m : succ(n)) UEVAR(m, n) \cup (LIVEOUT(m) - VARKILL(m))
        boolean changed = true;
        while (changed) {
            changed = false;
            for (CFGBasicBlock n : method.getBlocks()) {
                Set<CFGAddress> newLiveOut = new HashSet<>();
                for (CFGBasicBlock m : n.successors()) {
                    newLiveOut.addAll(ueVar.get(m).get(n));
                    Set<CFGAddress> tmp = new HashSet<>(liveOut.get(m));
                    varKill.get(m).forEach(tmp::remove);
                    newLiveOut.addAll(tmp);

                    if (!newLiveOut.equals(liveOut.get(n))) {
                        changed = true;
                        liveOut.put(n, newLiveOut);
                    }
                }
            }
        }

        // compute livenow sets
        liveNow = new HashMap<>();
        liveIn = new HashMap<>();
        for (CFGBasicBlock b : method.getBlocks()) {
            Set<CFGAddress> live = new HashSet<>(liveOut.get(b));
            for (CFGInstruction inst : OptUtils.reverse(b.getAllInstructions())) {
                if (inst instanceof CFGPhiInstruction) break;
                live.addAll(inst.operands());
                if (inst.address() != null) live.remove(inst.address());
                liveNow.put(inst, new HashSet<>(live));
            }

            liveIn.put(b, new HashSet<>(live));
        }
    }

    /** Memoization key for belady computation */
    private record BeladyKey(CFGBasicBlock b, CFGInstruction l, CFGAddress v) {}
    /** Memoization table for belady computation */
    private Map<BeladyKey, Integer> beladyMemoize = new HashMap<>();

    /**
     * Computes the belady min-heuristic for first given variable at first given instruction.
     *
     * @param b the basic block l belongs to
     * @param l the instruction
     * @param v the variable
     * @return the min distance to the next use of v
     */
    private int belady(CFGMethod m, CFGBasicBlock b, CFGInstruction l, CFGAddress v) {
        BeladyKey key = new BeladyKey(b, l, v);
        if (beladyMemoize.containsKey(key)) {
            return beladyMemoize.get(key);
        }

        // if v is used here, N(l, v) = 0
        if (l.operands().contains(v)) {
            beladyMemoize.put(key, 0);
            return 0;
        }

        // if v is not live at l, N(l, v) = infty
        if (!(l instanceof CFGPhiInstruction) && !liveNow.get(l).contains(v)) {
            beladyMemoize.put(key, Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }

        // else N(l, v) = min(N(succ, v) + 1)
        int min = Integer.MAX_VALUE;
        beladyMemoize.put(key, Integer.MAX_VALUE); // avoid infinite recursion
        for (CFGInstruction succ : successors(b, l)) {
            int succBelady = belady(m, data.get(m).getBlock(succ), succ, v);
            int increment = l instanceof CFGPhiInstruction && succ instanceof CFGPhiInstruction
                    ? 0 : 1;
            if (succBelady < Integer.MAX_VALUE)
                min = Math.min(min, succBelady + increment);
        }

        beladyMemoize.put(key, min);
        return min;
    }

    /**
     * Computes all possible next instructions.
     *
     * @param b the current basic block
     * @param l the current instruction
     * @return first set of all possible next instructions
     */
    private Set<CFGInstruction> successors(CFGBasicBlock b, CFGInstruction l) {
        if (l instanceof CFGPhiInstruction phi) {
            CFGInstruction succ = b.getPhiInstructions().next(phi);
            if (succ == null)
                succ = b.getInstructions().peekFirst();
            if (succ == null)
                succ = b.getLastInstruction();
            return Set.of(succ);
        } else if (!(l instanceof CFGLastInstruction)) {
            CFGInstruction succ = b.getInstructions().next(l);
            if (succ == null)
                succ = b.getLastInstruction();
            return Set.of(succ);
        } else {
            return b.successors().stream()
                    .map(s -> s.getAllInstructions().get(0))
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Spills and reloads variables so that the register pressure in any point is
     * at most numRegisters.
     *
     * @param method the method being spilled
     */
    private void spilling(CFGMethod method) {
        in = new HashMap<>();
        out = new HashMap<>();
        spills = new HashMap<>();
        reloads = new HashMap<>();

        // perform local spilling
        for (CFGBasicBlock block : method.getBlocks()) {
            // live upon entering block (after phi instructions)
            List<CFGAddress> P = new ArrayList<>(liveIn.get(block));

            if (block.equals(method.getBlocks().peekFirst())) {
                for (int i = 0; i < Math.min(6, method.getParams().size()); i++) {
                    P.add(method.getParams().get(i));
                }
            }
            P.sort(Comparator.comparingInt(v -> belady(method, block, block.getAllInstructions().get(0), v)));

            // initial value of Q
            Set<CFGAddress> I = new HashSet<>();
            for (int i = 0; i < Math.min(P.size(), numRegisters); i++) {
                I.add(P.get(i));
            }
            // addresses currently in registers
            Set<CFGAddress> Q = new HashSet<>(I);

            // {v in I : used in B before displaced}
            Set<CFGAddress> inB = new HashSet<>();
            // helper for computing in: those displaced before use
            Set<CFGAddress> notInB = new HashSet<>();

            // TODO deal with global variables?
            Queue<CFGInstruction> workList = new LinkedList<>(block.getAllInstructions());
            while (!workList.isEmpty()) {
                CFGInstruction inst = workList.poll();
                if (inst instanceof CFGPhiInstruction) continue;

                // unloaded operands
                Set<CFGAddress> L = new HashSet<>(inst.operands());
                L.stream().filter(x -> I.contains(x) && !notInB.contains(x))
                                .forEach(inB::add);
                Q.forEach(L::remove);

                // TODO deal with method calls?
                // esp those with > 6 args

                // number of spills needed to make space for this instruction
                int numSpills = Math.max(0,
                        L.size() + Q.size()
                                + (inst.address() != null ? 1 : 0)
                                - numRegisters);

                // remove farthest away instructions (by belady)
                List<CFGAddress> farthestAway = new ArrayList<>(Q);
                farthestAway.sort(Comparator.comparingInt(v -> -belady(method, block, inst, v)));

                // spill
                for (int i = 0; i < numSpills; i++) {
                    CFGAddress bye = farthestAway.get(i);
                    Q.remove(bye);

                    // no need to spill
                    if (I.contains(bye) && !inB.contains(bye)) {
                        notInB.add(bye);
                        continue;
                    }

                    CFGSpillInstruction spill = spill(bye);

                    if (inst instanceof CFGLastInstruction)
                        block.getInstructions().offerLast(spill);
                    else block.getInstructions().insertBefore(inst, spill);

                    spills.computeIfAbsent(bye, k -> new HashSet<>()).add(spill);
                }

                // reload unloaded operands
                for (CFGAddress hi : L) {
                    Q.add(hi);
                    CFGReloadInstruction reload = reload(hi);

                    if (inst instanceof CFGLastInstruction)
                        block.getInstructions().offerLast(reload);
                    else block.getInstructions().insertBefore(inst, reload);

                    reloads.computeIfAbsent(hi, k -> new HashSet<>()).add(reload);
                }

                if (inst.address() != null)
                    Q.add(inst.address());
            }

            // TODO what exactly is in_B defined as? the two papers differ
            in.put(block, new HashSet<>(I));
            in.get(block).removeAll(notInB);
            out.put(block, Q);

System.out.printf("in_B[%s] = %s%n", block.address(), inB);
System.out.printf("out_B[%s] = %s%n", block.address(), Q);
        }

        // global linking between in and out sets
        for (CFGBasicBlock block : method.getBlocks()) {
            int numPredecessors = data.get(method).getDominatorTree().getPredecessors(block).size();
            for (CFGBasicBlock pred : data.get(method).getDominatorTree().getPredecessors(block)) {
                // TODO: Hack only says reload; are the spills unnecessary?
                Set<CFGAddress> needSpill = out.get(pred); in.get(block).forEach(needSpill::remove);
                Set<CFGAddress> needReload = in.get(block); out.get(pred).forEach(needReload::remove);

                // critical edge split
                if (numPredecessors == 1) {
System.out.printf("Inserting at %s transition from %s to %s%n", block.address(), pred.address(), block.address());
System.out.printf("Spilling %s, reloading %s%n", needSpill, needReload);
                    // spill at beginning of block

                    // reloads second
                    for (CFGAddress reloadAddr : needReload) {
                        CFGReloadInstruction reload = reload(reloadAddr);
                        block.getInstructions().offerFirst(reload);
                        reloads.computeIfAbsent(reloadAddr, k -> new HashSet<>()).add(reload);
                    }

                    // spills first
                    for (CFGAddress spillAddr : needSpill) {
                        CFGSpillInstruction spill = spill(spillAddr);
                        block.getInstructions().offerFirst(spill);
                        spills.computeIfAbsent(spillAddr, k -> new HashSet<>()).add(spill);
                    }
                } else {
System.out.printf("Inserting at %s transition from %s to %s%n", pred.address(), pred.address(), block.address());
System.out.printf("Spilling %s, reloading %s%n", needSpill, needReload);
                    // spill at end of predecessor

                    // calculate phi translations
                    for (CFGPhiInstruction phi : block.getPhiInstructions()) {
                        if (needReload.contains(phi.address())) {
                            needReload.remove(phi.address());
                            CFGAddress phiTranslation = phi.getSources().get(pred);
                            if (phiTranslation != null)
                                needReload.add(phiTranslation);
                        }
                    }

                    // spills first
                    // note no branch instruction since pred has one successor
                    for (CFGAddress spillAddr : needSpill) {
                        CFGSpillInstruction spill = spill(spillAddr);
                        pred.getInstructions().offerLast(spill);
                        spills.computeIfAbsent(spillAddr, k -> new HashSet<>()).add(spill);
                    }

                    // reloads second
                    for (CFGAddress reloadAddr : needReload) {
                        CFGReloadInstruction reload = reload(reloadAddr);
                        pred.getInstructions().offerLast(reload);
                        reloads.computeIfAbsent(reloadAddr, k -> new HashSet<>()).add(reload);
                    }
                }
            }
        }
    }

    /** Maps each variable to the first memory address to which it was spilled */
    private Map<CFGAddress, CFGAddress> spillLocations = new HashMap<>();

    private CFGSpillInstruction spill(CFGAddress address) {
        CFGAddress spillAddr = spillLocations.computeIfAbsent(address,
                k -> ctx.getSymbolTable().newNode(address));
        return new CFGSpillInstruction(ctx, spillAddr, address);

        /*
        // TODO: Hack thesis says to assign different memory addresses to each spill?
        CFGAddress spillAddr = ctx.getSymbolTable().newNode(address);
        spillLocations.putIfAbsent(address, spillAddr);
        return new CFGSpillInstruction(ctx, spillAddr, address);
         */
    }

    private CFGReloadInstruction reload(CFGAddress address) {
        CFGAddress spillAddr = spillLocations.computeIfAbsent(address,
                k -> ctx.getSymbolTable().newNode(address));
        return new CFGReloadInstruction(ctx, address, spillAddr);
    }

    /**
     * Represents first memory address, e.g. in which data is spilled
     * or in which first parameter or global variable is stored.
     */
    private class MemoryAddress {
        private static int counter = 0;
        int index;

        public MemoryAddress() {
            index = counter++;
        }
    }

    /**
     * Represents one of finitely many physical registers.
     */
    private record PhysicalRegister(int index) {}

    /**
     * Represents first spill instruction:
     * var with mem addr <- SPILL(var with reg)
     */
    private class CFGSpillInstruction extends CFGCopyInstruction {
        public CFGSpillInstruction(CFGContext ctx, CFGAddress address, CFGAddress operand) {
            super(ctx, address, operand);
        }

        @Override
        public String toString() {
            String[] split = super.toString().split(" = ");
            return String.format("%s = spill %s",
                    split[0], split[1]);
        }
    }

    /**
     * Represents first reload instruction:
     * var with reg <- RELOAD(var with mem addr)
     */
    private class CFGReloadInstruction extends CFGCopyInstruction {
        public CFGReloadInstruction(CFGContext ctx, CFGAddress address, CFGAddress operand) {
            super(ctx, address, operand);
        }

        @Override
        public String toString() {
            String[] split = super.toString().split(" = ");
            return String.format("%s = reload %s",
                    split[0], split[1]);
        }
    }

}
