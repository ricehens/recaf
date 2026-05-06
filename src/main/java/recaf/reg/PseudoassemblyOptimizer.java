package recaf.reg;

import recaf.asm.*;

import java.util.*;

public class PseudoassemblyOptimizer {

    private AssemblyBuilder ab;
    private ASMProgram asm;
    private ASMContext ctx;

    public PseudoassemblyOptimizer(AssemblyBuilder ab) {
        this.ab = ab;
        this.asm = ab.asm();
        this.ctx = ab.ctx();
    }

    public void apply() {
        for (ASMMethod method : asm.getMethods()) {
            new MethodPseudoassemblyOptimizer(method).apply();
        }
    }

    public class MethodPseudoassemblyOptimizer {

        private ASMMethod method;

        private Map<ASMVirtualRegister, Set<ASMInstruction>> defs;
        private Map<ASMVirtualRegister, Set<ASMInstruction>> uses;
        private Map<ASMInstruction, ASMBasicBlock> i2b;

        private static final Map<ASMOperator, ASMOperator> setToJump = Map.ofEntries(
                Map.entry(ASMOperator.SETL, ASMOperator.JL),
                Map.entry(ASMOperator.SETLE, ASMOperator.JLE),
                Map.entry(ASMOperator.SETG, ASMOperator.JG),
                Map.entry(ASMOperator.SETGE, ASMOperator.JGE),
                Map.entry(ASMOperator.SETE, ASMOperator.JE),
                Map.entry(ASMOperator.SETNE, ASMOperator.JNE)
        );

        private static final Map<ASMOperator, ASMOperator> jumpInverter = Map.ofEntries(
                Map.entry(ASMOperator.JL, ASMOperator.JGE),
                Map.entry(ASMOperator.JLE, ASMOperator.JG),
                Map.entry(ASMOperator.JG, ASMOperator.JLE),
                Map.entry(ASMOperator.JGE, ASMOperator.JL),
                Map.entry(ASMOperator.JE, ASMOperator.JNE),
                Map.entry(ASMOperator.JNE, ASMOperator.JE)
        );

        public MethodPseudoassemblyOptimizer(ASMMethod method) {
            this.method = method;
        }

        public void apply() {
            computeDefUse();
            deadStoreElimination();
            branchConditionCoalesce();
        }

        private void computeDefUse() {
            defs = new HashMap<>();
            uses = new HashMap<>();
            i2b = new HashMap<>();
            for (ASMMethod method : asm.getMethods()) {
                for (ASMBasicBlock block : method.getBlocks()) {
                    for (ASMInstruction inst : block.getInstructions()) {
                        i2b.put(inst, block);
                        for (ASMAbstractRegister operand : inst.operandRegisters()) {
                            if (operand instanceof ASMVirtualRegister vr) {
                                uses.computeIfAbsent(vr, k -> new HashSet<>()).add(inst);
                                defs.putIfAbsent(vr, new HashSet<>());
                            }
                        }
                        for (ASMAbstractRegister dest : inst.destinationRegisters()) {
                            if (dest instanceof ASMVirtualRegister vr) {
                                defs.computeIfAbsent(vr, k -> new HashSet<>()).add(inst);
                                uses.putIfAbsent(vr, new HashSet<>());
                            }
                        }
                    }
                }
            }
        }

        /* coalesces multi-step computation of the branch conditional */
        // we'll assume first property of the pseudoassembly: each cmp is followed by at most one set instruction
        // which is true upon exiting InstructionSelection
        private void branchConditionCoalesce() {
            for (ASMBasicBlock block : method.getBlocks()) {
                Queue<ASMInstruction> workList = new LinkedList<>(block.getInstructions().stream().toList());
                outer:
                while (!workList.isEmpty()) {
                    ASMInstruction set = workList.poll();
                    if (!setToJump.containsKey(set.op())) continue;
                    if (!(set.dest() instanceof ASMVirtualRegister vr)
                    || uses.get(vr).size() != 1)
                        continue;

                    ASMInstruction cmp = block.getInstructions().prev(set);
                    if (cmp.op() != ASMOperator.CMPL && cmp.op() != ASMOperator.CMPQ && cmp.op() != ASMOperator.CMPB) continue;

                    ASMInstruction cmpb = uses.get(vr).iterator().next();
                    if (cmpb.op() != ASMOperator.CMPB) continue;
                    if (!(cmpb.src() instanceof ASMLiteral lit) || !lit.getValue().equals("0")) continue;

                    ASMInstruction jne = cmpb;
                    while (jne.op() != ASMOperator.JNE) {
                        if (setToJump.containsKey(jne.op()) || setToJump.containsValue(jne.op()))
                            continue outer;
                        jne = block.getInstructions().next(jne);
                        if (jne == null)
                            continue outer;
                        for (ASMAbstractRegister dest : jne.destinationRegisters()) {
                            if (cmp.operandRegisters().contains(dest))
                                continue outer;
                        }
                    }

                    block.getInstructions().insertBefore(jne,
                            new ASMInstruction(ctx, cmp.op(), cmp.src(), cmp.dest(), cmp.specificCtx()));
                    block.getInstructions().replace(jne,
                            new ASMInstruction(ctx, setToJump.get(set.op()), jne.src(), jne.dest(), jne.specificCtx()));
                    block.getInstructions().remove(cmp);
                    block.getInstructions().remove(set);
                    block.getInstructions().remove(cmpb);
                }

                ASMInstruction jmp = block.getInstructions().peekLast();
                ASMBasicBlock nextBlock = method.getBlocks().next(block);
                if (nextBlock != null && jmp != null && jmp.op() == ASMOperator.JMP) {
                    if (jmp.dest().equals(nextBlock.getLabel())) {
                        block.getInstructions().remove(jmp);
                        continue;
                    }

                    ASMInstruction j = block.getInstructions().prev(jmp);
                    if (j == null || !jumpInverter.containsKey(j.op()))
                        continue;
                    if (j.dest().equals(nextBlock.getLabel())) {
                        block.getInstructions().replace(j,
                                new ASMInstruction(ctx, jumpInverter.get(j.op()), j.src(), jmp.dest(), j.specificCtx()));
                        block.getInstructions().remove(jmp);
                    }
                }
            }
        }

        Map<ASMBasicBlock, Set<ASMVirtualRegister>> liveOut;

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

        private void deadStoreElimination() {
            boolean changed = true;
            while (changed) {
                changed = false;
                computeLiveness();
                Set<ASMInstruction> dead = new HashSet<>();
                for (ASMBasicBlock block : method.getBlocks()) {
                    Set<ASMVirtualRegister> liveNow = liveOut.get(block);
                    for (ASMInstruction inst : block.getInstructions().reverse()) {
                        if (inst.op() == ASMOperator.MOVQ || inst.op() == ASMOperator.MOVL
                                || inst.op() == ASMOperator.MOVB) {
                            if (inst.dest() instanceof ASMVirtualRegister dest && !liveNow.contains(dest)) {
// System.out.printf("Killed %s%n", inst);
                                dead.add(inst);
                                continue;
                            }
                        }

                        for (ASMAbstractRegister dest : inst.destinationRegisters())
                            if (dest instanceof ASMVirtualRegister vr)
                                liveNow.remove(vr);
                        for (ASMAbstractRegister use : inst.operandRegisters())
                            if (use instanceof ASMVirtualRegister vr)
                                liveNow.add(vr);
                    }
                }

                for (ASMBasicBlock block : method.getBlocks()) {
                    Queue<ASMInstruction> workList = new LinkedList<>(block.getInstructions().stream().toList());
                    while (!workList.isEmpty()) {
                        ASMInstruction inst = workList.poll();
                        if (dead.contains(inst)) {
                            block.getInstructions().remove(inst);
                            changed = true;
                        }
                    }
                }
            }
        }

    }

}
