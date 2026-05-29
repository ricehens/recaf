package recaf.codegen;

import recaf.asm.*;
import recaf.common.Type;

import java.util.*;
import java.util.stream.Collectors;

public class AssemblyOptimizer {

    private AssemblyBuilder ab;
    private ASMProgram asm;
    private ASMContext ctx;

    public AssemblyOptimizer(AssemblyBuilder ab) {
        this.ab = ab;
        this.asm = ab.asm();
        this.ctx = ab.ctx();
    }

    public void apply() {
        for (ASMMethod method : asm.getMethods()) {
            new MethodAssemblyOptimizer(method).apply();
        }
    }

    public class MethodAssemblyOptimizer {

        private ASMMethod method;

        Map<ASMInstruction, ASMBasicBlock> parentBlock;
        Map<ASMBasicBlock, Set<ASMRegister>> liveOut;
        Map<ASMInstruction, Set<ASMRegister>> liveBefore;
        Map<ASMInstruction, Set<ASMRegister>> liveAfter;

        public MethodAssemblyOptimizer(ASMMethod method) {
            this.method = method;
        }

        public void apply() {
            singleInstPeephole();
            multoLea();
            leaCoalesce();
            arithmeticCoalesce();
            singleInstPeephole();
        }

        private void computeLiveness() {
            parentBlock = new HashMap<>();
            liveOut = new HashMap<>();
            liveBefore = new HashMap<>();
            liveAfter = new HashMap<>();

            for (ASMBasicBlock b : method.getBlocks())
                for (ASMInstruction inst : b.getInstructions())
                    parentBlock.put(inst, b);

            Map<ASMBasicBlock, Set<ASMRegister>> ueVar = new HashMap<>();
            Map<ASMBasicBlock, Set<ASMRegister>> varKill = new HashMap<>();
            // compute uevar and varkill
            for (ASMBasicBlock b : method.getBlocks()) {
                liveOut.put(b, new HashSet<>());
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
                        Set<ASMRegister> liveIn = new HashSet<>(liveOut.get(m));
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

            // compute live now sets
            for (ASMBasicBlock b : method.getBlocks()) {
                Set<ASMRegister> liveNow = new HashSet<>(liveOut.get(b));

                for (ASMInstruction inst : b.getInstructions().reverse()) {
                    liveAfter.put(inst, new HashSet<>(liveNow));

                    for (ASMAbstractRegister dest : inst.destinationRegisters())
                        if (dest instanceof ASMRegister pr)
                            liveNow.remove(pr);

                    liveNow.addAll(inst.operandRegisters().stream()
                            .filter(r -> r instanceof ASMRegister).map(r -> (ASMRegister) r).collect(Collectors.toSet()));

                    liveBefore.put(inst, new HashSet<>(liveNow));
                }
            }
        }

        /** single-instruction peephole to eliminate useless instructions */
        private void singleInstPeephole() {
            for (ASMBasicBlock block : method.getBlocks()) {
                Queue<ASMInstruction> workList = new LinkedList<>(block.getInstructions().stream().toList());
                while (!workList.isEmpty()) {
                    ASMInstruction inst = workList.poll();
                    if (inst.op() == ASMOperator.MOVQ || inst.op() == ASMOperator.MOVL || inst.op() == ASMOperator.MOVB)
                        if (inst.src().equals(inst.dest()))
                            block.getInstructions().remove(inst);

                    if (inst.op() == ASMOperator.ADDQ || inst.op() == ASMOperator.ADDL
                            || inst.op() == ASMOperator.SUBQ || inst.op() == ASMOperator.SUBL)
                        if (inst.src() instanceof ASMLiteral lit && lit.getValue().equals("0"))
                            block.getInstructions().remove(inst);
                }
            }
        }

        /** combine adjacent arithmetic instructions */
        private void arithmeticCoalesce() {
            for (ASMBasicBlock block : method.getBlocks()) {
                for (ASMInstruction inst : block.getInstructions()) {
                    ASMInstruction next = block.getInstructions().next(inst);
                    if (next == null) continue;

                    if ((inst.op() == ASMOperator.ADDQ || inst.op() == ASMOperator.ADDL || inst.op() == ASMOperator.SUBQ || inst.op() == ASMOperator.SUBL)
                            && (next.op() == ASMOperator.ADDQ || next.op() == ASMOperator.ADDL || next.op() == ASMOperator.SUBQ || next.op() == ASMOperator.SUBL)
                            && inst.dest().equals(next.dest()) && inst.src() instanceof ASMLiteral lit1 && next.src() instanceof ASMLiteral lit2) {
                        long val1 = Long.parseLong(lit1.getValue());
                        if (inst.op() == ASMOperator.SUBQ || inst.op() == ASMOperator.SUBL) val1 = -val1;
                        long val2 = Long.parseLong(lit2.getValue());
                        if (next.op() == ASMOperator.SUBQ || next.op() == ASMOperator.SUBL) val2 = -val2;
                        long val = val1 + val2;
                        block.getInstructions().replace(inst, new ASMInstruction(inst.ctx(),
                                val < 0 ? ASMOperator.SUBQ : ASMOperator.ADDQ,
                                new ASMLiteral(val), inst.dest(), inst.specificCtx()));
                        block.getInstructions().remove(next);
                    }
                }
            }
        }

        /** convert immediate multiplications and shifts to lea when possible */
        private void multoLea() {
            for (ASMBasicBlock block : method.getBlocks()) {
                for (ASMInstruction inst : block.getInstructions()) {
                    if ((inst.op() == ASMOperator.IMULQ || inst.op() == ASMOperator.IMULL) && inst.src() instanceof ASMLiteral lit
                    && inst.dest() instanceof ASMRegister pr) {
                        long multiplicand = Long.parseLong(lit.getValue());
                        if (!Set.of(3L, 5L, 9L).contains(Math.abs(multiplicand))) continue;
                        if (multiplicand < 0)
                            block.getInstructions().insertAfter(inst, new ASMInstruction(inst.ctx(),
                                    inst.op() == ASMOperator.IMULQ ? ASMOperator.NEGQ : ASMOperator.NEGL,
                                    null, pr, inst.specificCtx()));
                        block.getInstructions().replace(inst, new ASMInstruction(
                                inst.ctx(),
                                inst.op() == ASMOperator.IMULQ ? ASMOperator.LEAQ : ASMOperator.LEAL,
                                switch ((int) Math.abs(multiplicand)) {
                                    case 3 -> new ASMStackAddressArray(0, pr.toType(Type.LONG), pr.toType(Type.LONG), 2);
                                    case 5 -> new ASMStackAddressArray(0, pr.toType(Type.LONG), pr.toType(Type.LONG), 4);
                                    case 9 -> new ASMStackAddressArray(0, pr.toType(Type.LONG), pr.toType(Type.LONG), 8);
                                    default -> throw new AssertionError("This should never happen.");
                                },
                        inst.dest(),
                        inst.specificCtx()
                        ));
                    } else if ((inst.op() == ASMOperator.SHLQ || inst.op() == ASMOperator.SHLL) && inst.src() instanceof ASMLiteral lit
                            && inst.dest() instanceof ASMRegister pr) {
                        int shift = Integer.parseInt(lit.getValue());
                        if (!Set.of(1, 2, 3).contains(shift)) continue;
                        block.getInstructions().replace(inst, new ASMInstruction(
                                inst.ctx(),
                                inst.op() == ASMOperator.SHLQ ? ASMOperator.LEAQ : ASMOperator.LEAL,
                                switch (shift) {
                                    case 1 -> new ASMStackAddressArray(0, pr.toType(Type.LONG), pr.toType(Type.LONG), 1);
                                    case 2 -> new ASMStackAddressArray(0, null, pr.toType(Type.LONG), 4);
                                    case 3 -> new ASMStackAddressArray(0, null, pr.toType(Type.LONG), 8);
                                    default -> throw new AssertionError("This should never happen.");
                                },
                                inst.dest(),
                                inst.specificCtx()
                        ));
                    }
                }
            }
        }

        /** merge lea's with surroundings whenever possible */
        private void leaCoalesce() {
            computeLiveness();

            for (ASMBasicBlock block : method.getBlocks()) {
                Queue<ASMInstruction> workList = new LinkedList<>(block.getInstructions().stream().toList());
                while (!workList.isEmpty()) {
                    ASMInstruction inst = workList.poll();
                    if (inst.op() == ASMOperator.LEAQ || inst.op() == ASMOperator.LEAL) {
                        if (!(inst.src() instanceof ASMStackAddressArray saddr)) continue;
                        if (saddr.getRegister() == ASMRegister.RSP || saddr.getRegister() == ASMRegister.RBP) continue;

                        // attempt to merge with later add
                        if (inst.dest() instanceof ASMRegister pr && pr.toType(Type.LONG) == saddr.getRegister2()
                                && (saddr.getRegister() == null || pr.toType(Type.LONG) == saddr.getRegister())) {
                            pr = pr.toType(Type.LONG);
                            ASMInstruction use = block.getInstructions().next(inst);
                            while (use != null) {
                                if (use.destinationRegisters().contains(pr) || use.operandRegisters().contains(pr))
                                    break;
                                use = block.getInstructions().next(use);
                            }

                            if (use != null && Set.of(ASMOperator.ADDQ, ASMOperator.ADDL).contains(use.op())) {
                                ASMLocation addDest = use.dest();
                                if (addDest instanceof ASMRegister prad) addDest = prad.toType(Type.LONG);
                                ASMLocation addSrc = use.src();
                                if (addSrc instanceof ASMRegister pras) addSrc = pras.toType(Type.LONG);

                                if (pr == addDest && pr != addSrc) {
                                    if (addSrc instanceof ASMRegister pr2 && saddr.getRegister() == null) {
                                        block.getInstructions().replace(use,
                                                new ASMInstruction(use.ctx(), inst.op(),
                                                        new ASMStackAddressArray(saddr.getOffset(), saddr.getAdditionalOffset(),
                                                                pr2.toType(Type.LONG), saddr.getRegister2(), saddr.getAlign()),
                                                        use.dest(), use.specificCtx()));
                                        block.getInstructions().remove(inst);
                                    } else if (addSrc instanceof ASMLiteral lit && saddr.getOffset() == 0) {
                                        block.getInstructions().replace(use,
                                                new ASMInstruction(use.ctx(), inst.op(),
                                                        new ASMStackAddressArray(Integer.parseInt(lit.getValue()), saddr.getAdditionalOffset(),
                                                                saddr.getRegister(), saddr.getRegister2(), saddr.getAlign()),
                                                        use.dest(), use.specificCtx()));
                                        block.getInstructions().remove(inst);
                                    }
                                } else if (pr == addSrc && pr != addDest && !liveAfter.get(use).contains(pr)
                                        && saddr.getRegister() == null && addDest instanceof ASMRegister pr2) {
// System.out.printf("(1) (%s) Merging %s and %s%n", method.getName(), inst, use);
                                    block.getInstructions().replace(use,
                                            new ASMInstruction(use.ctx(), inst.op(),
                                                    new ASMStackAddressArray(saddr.getOffset(), saddr.getAdditionalOffset(),
                                                            pr2, saddr.getRegister2(), saddr.getAlign()),
                                                    use.dest(), use.specificCtx()));
                                    block.getInstructions().remove(inst);
                                }
                            }
                        }
                    }
                }
            }

            for (ASMBasicBlock block : method.getBlocks()) {
                Queue<ASMInstruction> workList = new LinkedList<>(block.getInstructions().stream().toList());
                while (!workList.isEmpty()) {
                    ASMInstruction inst = workList.poll();
                    if (inst.op() == ASMOperator.LEAQ || inst.op() == ASMOperator.LEAL) {
                        if (!(inst.src() instanceof ASMStackAddressArray saddr)) continue;
                        if (saddr.getRegister() == ASMRegister.RSP || saddr.getRegister() == ASMRegister.RBP) continue;

                        // attempt to merge with earlier copy
                        if (inst.dest() instanceof ASMRegister pr && pr.toType(Type.LONG) == saddr.getRegister2()) {
                            pr = pr.toType(Type.LONG);
                            ASMInstruction def = block.getInstructions().prev(inst);
                            Set<ASMRegister> killed = new HashSet<>();
                            while (def != null) {
                                if (def.destinationRegisters().contains(pr))
                                    break;
                                def.destinationRegisters().stream().filter(r -> r instanceof ASMRegister)
                                        .map(r -> (ASMRegister) r).forEach(killed::add);
                                if (def.operandRegisters().contains(pr)) {
                                    def = null;
                                    break;
                                }
                                def = block.getInstructions().prev(def);
                            }

                            if (def != null && (def.op() == ASMOperator.MOVQ || def.op() == ASMOperator.MOVL)
                                    && def.src() instanceof ASMRegister pr0 && !killed.contains(pr0.toType(Type.LONG))) {
// System.out.printf("(2) (%s, %s) Merging %s and %s%n", method.getName(), block.getLabel(), inst, def);
                                block.getInstructions().replace(inst,
                                        new ASMInstruction(inst.ctx(), inst.op(),
                                                new ASMStackAddressArray(saddr.getOffset(), saddr.getAdditionalOffset(),
                                                        pr == saddr.getRegister() ? pr0.toType(Type.LONG) : saddr.getRegister(),
                                                        pr0.toType(Type.LONG), saddr.getAlign()),
                                                inst.dest(), inst.specificCtx()));
                                block.getInstructions().remove(def);
                            }
                        }
                    }
                }
            }

            for (ASMBasicBlock block : method.getBlocks()) {
                Queue<ASMInstruction> workList = new LinkedList<>(block.getInstructions().stream().toList());
                while (!workList.isEmpty()) {
                    ASMInstruction inst = workList.poll();
                    if (inst.op() == ASMOperator.LEAQ || inst.op() == ASMOperator.LEAL) {
                        if (!(inst.src() instanceof ASMStackAddressArray saddr)) continue;
                        if (saddr.getRegister() == ASMRegister.RSP || saddr.getRegister() == ASMRegister.RBP) continue;

                        // attempt to merge with earlier copy
                        if (inst.dest() instanceof ASMRegister pr && pr.toType(Type.LONG) == saddr.getRegister()) {
                            pr = pr.toType(Type.LONG);
                            ASMInstruction def = block.getInstructions().prev(inst);
                            Set<ASMRegister> killed = new HashSet<>();
                            while (def != null) {
                                if (def.destinationRegisters().contains(pr))
                                    break;
                                def.destinationRegisters().stream().filter(r -> r instanceof ASMRegister)
                                        .map(r -> (ASMRegister) r).forEach(killed::add);
                                if (def.operandRegisters().contains(pr)) {
                                    def = null;
                                    break;
                                }
                                def = block.getInstructions().prev(def);
                            }

                            if (def != null && (def.op() == ASMOperator.MOVQ || def.op() == ASMOperator.MOVL)
                                    && def.src() instanceof ASMRegister pr0 && !killed.contains(pr0.toType(Type.LONG))) {
// System.out.printf("(3) (%s) Merging %s and %s%n", method.getName(), inst, def);
                                block.getInstructions().replace(inst,
                                        new ASMInstruction(inst.ctx(), inst.op(),
                                                new ASMStackAddressArray(saddr.getOffset(), saddr.getAdditionalOffset(),
                                                        pr0.toType(Type.LONG),
                                                        pr == saddr.getRegister2() ? pr0.toType(Type.LONG) : saddr.getRegister2(),
                                                        saddr.getAlign()),
                                                inst.dest(), inst.specificCtx()));
                                block.getInstructions().remove(def);
                            }
                        }
                    }
                }

            }

        }

    }

}
