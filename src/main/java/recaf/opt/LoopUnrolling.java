package recaf.opt;

import recaf.general.*;
import recaf.cfg.*;
import recaf.opt.OptUtils.RegionConstant;
import recaf.utils.NumUtils;

import java.math.BigInteger;
import java.util.*;

// \begin{asbestos}
public class LoopUnrolling extends SSATransformation {

    private LoopUtils utils;
    private Set<CFGBasicBlock> visited;

    private final static int UNROLL_FACTOR = 4;
    private final static int SMALL_LOOP_UNROLL_FACTOR = 8;
    private final static int STATIC_UNROLL_FACTOR = 16;

    public LoopUnrolling(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
    }

    @Override
    public boolean apply() {
        visited = new HashSet<>();
        utils = new LoopUtils(method);
        List<Loop> countingLoops = utils.getLoops().stream().map(Loop::new).filter(Loop::isCountingLoop).toList();
        countingLoops.forEach(this::unroll);
        return !countingLoops.isEmpty();
    }

    private void unroll(Loop loop) {
        // if nested ,don't unroll outer loop
        for (CFGBasicBlock b : loop.blocks) {
            if (visited.contains(b))
                return;
        }
        visited.add(loop.header);

        int unrollFactor = UNROLL_FACTOR;
        long numIters = loop.getNumIterations();
        if (loop.blocks.size() == 1)
            unrollFactor = SMALL_LOOP_UNROLL_FACTOR;
        if (numIters >= 0 || numIters == -2)
            unrollFactor = STATIC_UNROLL_FACTOR;

// System.out.println(("Unrolling " + loop));
        CFGBasicBlock insertBefore = loop.header;

        Map<Integer, Map<CFGBasicBlock, CFGBasicBlock>> newBlocks = new HashMap<>();
        Map<Integer, Map<CFGAddress, CFGAddress>> newAddresses = new HashMap<>();

        CFGBasicBlock prologue = new CFGBasicBlock(ctx);
        prologue.setMethod(method);

        int remainder = (int) (numIters % unrollFactor);
        if (remainder == 0) remainder = unrollFactor;
        int numCopies = unrollFactor + (numIters >= 0 ? remainder - 1 : 0);
        for (int i = 0; i < numCopies; i++) {
            newBlocks.put(i, new HashMap<>());
            newAddresses.put(i, new HashMap<>());
        }

        CFGBasicBlock epilogueEntry = new CFGBasicBlock(ctx);
        epilogueEntry.setMethod(method);

        for (int i = 0; i < numCopies; i++) {
            for (int j = 0; j < loop.blockList.size(); j++) {
                CFGBasicBlock newBlock = new CFGBasicBlock(ctx);
                newBlock.setMethod(method);
                newBlocks.get(i).put(loop.blockList.get(j), newBlock);
            }
        }

        // redirect
        for (CFGBasicBlock block : method.getBlocks()) {
            if (loop.blocks.contains(block)) continue;
            if (block.getLastInstruction() instanceof CFGJumpInstruction jump) {
                if (jump.jumpAddr().equals(loop.header.address()))
                    block.setLastInstruction(new CFGJumpInstruction(ctx, prologue.address()));
            }
            else if (block.getLastInstruction() instanceof CFGBranchInstruction branch) {
                if (branch.thenAddr().equals(loop.header.address()))
                    block.setLastInstruction(new CFGBranchInstruction(ctx, branch.boolAddr(), prologue.address(), branch.elseAddr()));
                else if (branch.elseAddr().equals(loop.header.address()))
                    block.setLastInstruction(new CFGBranchInstruction(ctx, branch.boolAddr(), branch.thenAddr(), prologue.address()));
            }
        }

        // set up prologue
        Map<CFGAddress, CFGAddress> prologueAddresses = new HashMap<>();
        for (CFGPhiInstruction phi : loop.header.getPhiInstructions()) {
            CFGPhiInstruction newPhi = phi.copy();;
            newPhi.address().set(prologueAddresses.computeIfAbsent(phi.address(), k -> ctx.getSymbolTable().newNode(k)));
            prologue.getPhiInstructions().offerLast(newPhi);
        }

        Literal factor = ctx.getType(loop.updatePair.iv) == Type.LONG ? new LongLiteral(unrollFactor) : new IntLiteral(unrollFactor);
        RegionConstant scaledRc;
        if (loop.updatePair.rc.isLiteral) {
            scaledRc = new RegionConstant(OptUtils.compute(BinaryOperator.TIMES, loop.updatePair.rc.literal, factor));
        } else {
            scaledRc = new RegionConstant(ctx.getSymbolTable().newNode(loop.updatePair.rc.address));
            prologue.getInstructions().offerLast(new CFGBinaryImmediateInstruction(ctx, scaledRc.address, BinaryOperator.TIMES, loop.updatePair.rc.address, factor));
        }

        RegionConstant shiftedLimit;
        if (scaledRc.isLiteral) {
            if (loop.condPair.rc.isLiteral) {
                shiftedLimit = new RegionConstant(OptUtils.compute(loop.updatePair.op == BinaryOperator.PLUS ? BinaryOperator.MINUS : BinaryOperator.PLUS,
                        loop.condPair.rc.literal, scaledRc.literal));
            } else {
                shiftedLimit = new RegionConstant(ctx.getSymbolTable().newNode(loop.condPair.rc.address));
                prologue.getInstructions().offerLast(new CFGBinaryImmediateInstruction(ctx, shiftedLimit.address,
                        loop.updatePair.op == BinaryOperator.PLUS ? BinaryOperator.MINUS : BinaryOperator.PLUS,
                        loop.condPair.rc.address, scaledRc.literal));
            }
        } else {
            if (loop.condPair.rc.isLiteral) {
                CFGAddress rcLit = ctx.getSymbolTable().newNode(scaledRc.address);
                prologue.getInstructions().offerLast(new CFGLiteralInstruction(ctx, rcLit, loop.condPair.rc.literal));
                shiftedLimit = new RegionConstant(ctx.getSymbolTable().newNode(scaledRc.address));
                prologue.getInstructions().offerLast(new CFGBinaryInstruction(ctx, shiftedLimit.address,
                        loop.updatePair.op == BinaryOperator.PLUS ? BinaryOperator.MINUS : BinaryOperator.PLUS,
                        rcLit, scaledRc.address));
            } else {
                shiftedLimit = new RegionConstant(ctx.getSymbolTable().newNode(loop.condPair.rc.address));
                prologue.getInstructions().offerLast(new CFGBinaryInstruction(ctx, shiftedLimit.address,
                        loop.updatePair.op == BinaryOperator.PLUS ? BinaryOperator.MINUS : BinaryOperator.PLUS,
                        loop.condPair.rc.address, scaledRc.address));
            }
        }

        CFGAddress prologueCond = ctx.getSymbolTable().addVar(Type.BOOL);
        CFGAddress prologueIV = prologueAddresses.get(loop.updatePair.iv);
        prologue.getInstructions().offerLast(
                shiftedLimit.isLiteral
                        ? new CFGBinaryImmediateInstruction(ctx, prologueCond, loop.condPair.op, prologueIV, shiftedLimit.literal)
                        : new CFGBinaryInstruction(ctx, prologueCond, loop.condPair.op, prologueIV, shiftedLimit.address)
        );
        prologue.setLastInstruction(new CFGBranchInstruction(ctx, prologueCond, newBlocks.get(0).get(loop.header).address(), loop.header.address()));

        // insert new blocks
        method.getBlocks().insertBefore(insertBefore, prologue);
        for (int i = 0; i < unrollFactor; i++) {
            for (int j = 0; j < loop.blockList.size(); j++) {
                method.getBlocks().insertBefore(insertBefore, newBlocks.get(i).get(loop.blockList.get(j)));
            }
        }
        method.getBlocks().insertBefore(insertBefore, epilogueEntry);
        for (int i = unrollFactor; i < numCopies; i++) {
            for (int j = 0; j < loop.blockList.size(); j++) {
                method.getBlocks().insertBefore(insertBefore, newBlocks.get(i).get(loop.blockList.get(j)));
            }
        }

        // initialize new addresses
        for (int i = 0; i < numCopies; i++) {
            for (CFGBasicBlock oldBlock : loop.blocks) {
                for (CFGInstruction inst : oldBlock.getAllInstructions()) {
                    CFGAddress address = inst.address();
                    if (address == null) continue;
                    if (ctx.isGlobalVar(address)) continue;
                    CFGAddress newAddress = newAddresses.get(i).computeIfAbsent(address, k -> ctx.getSymbolTable().newNode(k));
                    newAddresses.get(i).put(address, newAddress);
                }
            }
        }

        // insert instructions
        for (int i = 0; i < numCopies; i++) {
            for (CFGBasicBlock oldBlock : loop.blocks) {
                CFGBasicBlock newBlock = newBlocks.get(i).get(oldBlock);

                // insert phi functions
                for (CFGPhiInstruction phi : oldBlock.getPhiInstructions()) {
                    CFGAddress newPhiDest = newAddresses.get(i).get(phi.address());
                    CFGPhiInstruction newPhi = new CFGPhiInstruction(ctx, newPhiDest);
                    for (CFGBasicBlock source : phi.getSources().keySet()) {
                        if (loop.blocks.contains(source)) {
                            if (source.equals(loop.backEdge)) {
                                int prevI = i >= unrollFactor ? i - 1 : (i + unrollFactor - 1) % unrollFactor;
                                CFGBasicBlock newSource = newBlocks.get(prevI).get(source);
                                CFGAddress newValue = newAddresses.get(prevI).getOrDefault(phi.getSources().get(source), phi.getSources().get(source));
                                newPhi.add(newSource, newValue);
                            } else {
                                CFGBasicBlock newSource = newBlocks.get(i).get(source);
                                CFGAddress newValue = newAddresses.get(i).getOrDefault(phi.getSources().get(source), phi.getSources().get(source));
                                newPhi.add(newSource, newValue);
                            }
                        } else {
                            newPhi.add(source, phi.getSources().get(source));
                        }
                    }

                    // also redirect from prologue
                    if (i == 0 && oldBlock.equals(loop.header) && prologueAddresses.containsKey(phi.address())) {
                        newPhi.add(prologue, prologueAddresses.get(phi.address()));
                    }

                    newBlock.getPhiInstructions().offerLast(newPhi);
                }

                for (CFGInstruction inst : oldBlock.getInstructions()) {
                    CFGInstruction newInst = inst.copy();
                    if (inst.address() != null && !ctx.isGlobalVar(inst.address()))
                        newInst.address().set(newAddresses.get(i).get(inst.address()));
                    for (CFGAddress operand : newInst.operands()) {
                        operand.set(newAddresses.get(i).getOrDefault(operand, operand));
                    }
                    newBlock.getInstructions().offerLast(newInst);
                }

                CFGLastInstruction last = oldBlock.getLastInstruction();
                if (last != null) {
                    CFGLastInstruction newLast = last.copy();
                    for (CFGAddress operand : newLast.operands()) {
                        operand.set(newAddresses.get(i).getOrDefault(operand, operand));
                    }

                    if (newLast instanceof CFGJumpInstruction jump) {
                        CFGBasicBlock jumpBlock = ctx.getSymbolTable().getBlock(jump.jumpAddr());
                        if (loop.blocks.contains(jumpBlock))
                            newLast = new CFGJumpInstruction(ctx, newBlocks.get(i).get(jumpBlock).address());
                    } else if (newLast instanceof CFGBranchInstruction branch) {
                        CFGBasicBlock thenBlock = ctx.getSymbolTable().getBlock(branch.thenAddr());
                        CFGBasicBlock elseBlock = ctx.getSymbolTable().getBlock(branch.elseAddr());
                        newLast = new CFGBranchInstruction(ctx, branch.boolAddr(),
                                loop.blocks.contains(thenBlock) ? newBlocks.get(i).get(thenBlock).address() : branch.thenAddr(),
                                loop.blocks.contains(elseBlock) ? newBlocks.get(i).get(elseBlock).address() : branch.elseAddr());
                    }

                    newBlock.setLastInstruction(newLast);
                }
            }
        }

        // adjust branch instructions
        for (int i = 0; i < numCopies - 1; i++) {
            if (i == unrollFactor - 1) continue;
            CFGBasicBlock oldBack = newBlocks.get(i).get(loop.backEdge);
            CFGBasicBlock newHead = newBlocks.get(i + 1).get(loop.header);
            for (CFGPhiInstruction phi : newHead.getPhiInstructions()) {
                if (phi.address().equals(newAddresses.get(i + 1).get(loop.updatePair.iv))) {
                    phi.add(oldBack, newAddresses.get(i).get(loop.condPair.iv));
                }
            }
            oldBack.setLastInstruction(new CFGJumpInstruction(ctx, newHead.address()));
        }


        // adjust final backedge
        CFGBasicBlock finalBack = newBlocks.get(unrollFactor - 1).get(loop.backEdge);
        CFGBasicBlock finalHead = newBlocks.get(unrollFactor - 1).get(loop.header);
        CFGBasicBlock repeatHead = newBlocks.get(0).get(loop.header);
        CFGBranchInstruction finalBranch = (CFGBranchInstruction) finalBack.getLastInstruction();

        CFGAddress oldIV = newAddresses.get(0).get(loop.updatePair.iv);
        CFGAddress newIV = ctx.getSymbolTable().newNode(oldIV);
        finalBack.getInstructions().offerLast(
                scaledRc.isLiteral
                ? new CFGBinaryImmediateInstruction(ctx, newIV, loop.updatePair.op, oldIV, scaledRc.literal)
                : new CFGBinaryInstruction(ctx, newIV, loop.updatePair.op, oldIV, scaledRc.address)
        );

        CFGAddress newCond = ctx.getSymbolTable().addVar(Type.BOOL);
        finalBack.getInstructions().offerLast(
                shiftedLimit.isLiteral
                        ? new CFGBinaryImmediateInstruction(ctx, newCond, loop.condPair.op, newIV, shiftedLimit.literal)
                        : new CFGBinaryInstruction(ctx, newCond, loop.condPair.op, newIV, shiftedLimit.address)
        );
        finalBranch.boolAddr().set(newCond);

        if (finalBranch.thenAddr().equals(finalHead.address())) {
            finalBack.setLastInstruction(new CFGBranchInstruction(ctx, finalBranch.boolAddr(),
                    repeatHead.address(), epilogueEntry.address()));
        } else if (finalBranch.elseAddr().equals(finalHead.address())) {
            finalBack.setLastInstruction(new CFGBranchInstruction(ctx, finalBranch.boolAddr(),
                    epilogueEntry.address(), repeatHead.address()));
        } else throw new RuntimeException("This should never happen.");

        for (CFGPhiInstruction phi : repeatHead.getPhiInstructions()) {
            if (phi.address().equals(newAddresses.get(0).get(loop.updatePair.iv))) {
                phi.add(finalBack, newIV);
            }
        }

        // handle epilogue
        if (numIters == -2) {
            // no epilogue needed
            finalBack.setLastInstruction(new CFGJumpInstruction(ctx, repeatHead.address()));
        } else if (numIters < 0) {
            epilogueEntry.setLastInstruction(new CFGJumpInstruction(ctx, loop.header.address()));

            for (CFGPhiInstruction phi : loop.header.getPhiInstructions()) {
                Queue<CFGBasicBlock> workList = new LinkedList<>(phi.getSources().keySet());
                while (!workList.isEmpty()) {
                    CFGBasicBlock source = workList.poll();
                    if (loop.blocks.contains(source)) {
// System.out.println(phi + " 的 " + source.address());
                        CFGAddress newValue = newAddresses.get(unrollFactor - 1).getOrDefault(phi.getSources().get(source), phi.getSources().get(source));
                        phi.add(epilogueEntry, newValue);
                    }
                }
                if (prologueAddresses.containsKey(phi.address())) {
                    phi.add(prologue, prologueAddresses.get(phi.address()));
                }
                if (phi.address().equals(loop.updatePair.iv)) {
                    phi.add(epilogueEntry, newIV);
                }
            }
        } else {
            // static case
            method.getBlocks().remove(epilogueEntry);
            CFGBasicBlock lastUnroll = newBlocks.get(unrollFactor - 1).get(loop.backEdge);
            CFGBranchInstruction lastBranch = (CFGBranchInstruction) lastUnroll.getLastInstruction();
            CFGBasicBlock firstEpilogue = remainder == 1 ? loop.header : newBlocks.get(unrollFactor).get(loop.header);
// System.out.println(firstEpilogue.address())            ;
            lastUnroll.setLastInstruction(
                    new CFGBranchInstruction(ctx, lastBranch.boolAddr(), lastBranch.thenAddr(), firstEpilogue.address())
            );

            // adjust initial redirect
            CFGBranchInstruction prologueBranch = (CFGBranchInstruction) prologue.getLastInstruction();
            prologue.setLastInstruction(new CFGBranchInstruction(ctx, prologueBranch.boolAddr(),
                    prologueBranch.thenAddr(), firstEpilogue.address()));

            CFGAddress iv = remainder == 1 ? loop.updatePair.iv : newAddresses.get(unrollFactor).get(loop.updatePair.iv);
// System.out.println(iv);
            Map<CFGPhiInstruction, CFGPhiInstruction> phiCorrespondence = new HashMap<>();
            if (remainder > 1)
                for (CFGPhiInstruction phi : loop.header.getPhiInstructions())
                    for (CFGPhiInstruction phi2 : firstEpilogue.getPhiInstructions())
                        if (phi2.address().equals(newAddresses.get(unrollFactor).get(phi.address())))
                            phiCorrespondence.put(phi2, phi);
// System.out.println(newAddresses.get(unrollFactor));
// System.out.println(phiCorrespondence);
            for (CFGPhiInstruction phi : firstEpilogue.getPhiInstructions()) {
                Queue<CFGBasicBlock> workList = new LinkedList<>(phi.getSources().keySet());
                while (!workList.isEmpty()) {
                    CFGBasicBlock source = workList.poll();
                    CFGAddress value = phi.getSources().get(source);
                    CFGAddress phiAddress = phiCorrespondence.getOrDefault(phi, phi).address();
                    if (prologueAddresses.containsKey(phiAddress)) {
                        CFGAddress newValue = prologueAddresses.get(phiAddress);
                        phi.add(prologue, newValue);
                    }
                    if (loop.blocks.contains(source)) {
                        CFGAddress newValue = newAddresses.get(numCopies - 1).get(value);
                        phi.add(lastUnroll, newValue);
                    }
                }
                if (phi.address().equals(iv)) {
                    phi.add(prologue, prologueIV);
                    phi.add(lastUnroll, newAddresses.get(unrollFactor - 1).get(loop.condPair.iv));
                }
            }

            // adjust transition from penultimate epilogue block
            if (remainder > 1) {
                CFGBasicBlock trueFinalBack = newBlocks.get(numCopies - 1).get(loop.backEdge);
                trueFinalBack.setLastInstruction(new CFGJumpInstruction(ctx, loop.backEdge.address()));

                CFGBasicBlock oldBack = newBlocks.get(numCopies - 1).get(loop.backEdge);
// System.out.println(loop.updatePair.iv);
                for (CFGPhiInstruction phi : loop.header.getPhiInstructions()) {
                    Queue<CFGBasicBlock> workList = new LinkedList<>(phi.getSources().keySet());
                    while (!workList.isEmpty()) {
                        CFGBasicBlock source = workList.poll();
                        CFGAddress value = phi.getSources().get(source);
                        if (loop.blocks.contains(source)) {
                            CFGAddress newValue = newAddresses.get(numCopies - 1).get(value);
                            phi.add(oldBack, newValue);
                        }
                    }
                    if (phi.address().equals(loop.updatePair.iv)) {
                        phi.add(oldBack, newAddresses.get(numCopies - 1).get(loop.condPair.iv));
                    }
                }
                oldBack.setLastInstruction(new CFGJumpInstruction(ctx, loop.header.address()));
            }

            loop.backEdge.setLastInstruction(new CFGJumpInstruction(ctx,
                    ((CFGBranchInstruction) loop.backEdge.getLastInstruction()).elseAddr()));
        }
// System.out.println("Done unrolling!");
    }

    private class Loop extends LoopUtils.Loop {

        /** If counting loop, the conditional involving the (incremented) induction variable */
        IvRcPair condPair;
        /** If counting loop, the update/increment to the induction variable */
        IvRcPair updatePair;
        /** Initial value of the induction variable */
        RegionConstant ivInit;

        public Loop(LoopUtils.Loop loop) {
            super(LoopUnrolling.this.method, data, loop.header, loop.backEdge);
        }

        private boolean isCountingLoop() {
            // check header dominates all
            for (CFGBasicBlock b : blocks) {
                if (!(data.getDominatorTree().dominates(header, b)))
                    return false;
            }

            // check single exit
            for (CFGBasicBlock b : blocks) {
                if (b.equals(backEdge)) continue;
                for (CFGBasicBlock succ : b.successors()) {
                    if (!blocks.contains(succ))
                        return false;
                }
            }

            CFGAddress condition = branch.boolAddr();
            CFGInstruction condDef = data.getDefinition(condition);
            if (condDef == null || !blocks.contains(data.getBlock(condDef)))
                return false;

            condPair = getIfIvRcPair(condDef);
            if (condPair == null)
                return false;

            CFGInstruction ivDef = data.getDefinition(condPair.iv);
            if (ivDef == null || !blocks.contains(data.getBlock(ivDef)))
                return false;

            updatePair = getIfIvRcPair(ivDef);
            if (updatePair == null)
                return false;

            CFGInstruction origDef = data.getDefinition(updatePair.iv);
            if (origDef == null || !blocks.contains(data.getBlock(origDef)))
                return false;
            if (origDef instanceof CFGPhiInstruction phi) {
                boolean constantInit = true;
                CFGAddress ivInitAddr = null;
                if (!phi.getSources().containsKey(backEdge)
                        || !condPair.iv.equals(phi.getSources().get(backEdge)))
                    return false;
                for (CFGBasicBlock source : phi.getSources().keySet()) {
                    if (!source.equals(backEdge) && blocks.contains(source))
                        return false;
                    if (constantInit && !source.equals(backEdge)) {
                        if (ivInitAddr == null) ivInitAddr = phi.getSources().get(source);
                        if (!ivInitAddr.equals(phi.getSources().get(source))) {
                            ivInitAddr = null;
                            constantInit = false;
                        }
                    }
                }

                if (ivInitAddr != null) {
                    CFGInstruction initDef = data.getDefinition(ivInitAddr);
                    if (initDef instanceof CFGLiteralInstruction lit)
                        ivInit = new RegionConstant(lit.literal());
                    else ivInit = new RegionConstant(ivInitAddr);
                }
                return true;
            }

            return false;
        }

        private IvRcPair getIfIvRcPair(CFGInstruction inst) {
            if (ctx.isGlobalVar(inst.address())) return null;

            if (inst instanceof CFGBinaryImmediateInstruction bim) {
                CFGAddress iv = bim.left();
                Literal rc = bim.right();

                if (ctx.isGlobalVar(iv)) return null;

                return switch (bim.operator()) {
                    case PLUS, MINUS, LEQ, GEQ, LT, GT -> new IvRcPair(iv, new RegionConstant(rc), bim.operator());
                    default -> null;
                };
            } else if (inst instanceof CFGBinaryInstruction bin) {
                CFGAddress iv = bin.left();
                CFGAddress rc = bin.right();

                if (ctx.isGlobalVar(iv) || ctx.isGlobalVar(rc)) return null;

                CFGInstruction rcDef = data.getDefinition(rc);
                // if (rcDef instanceof CFGArrayReadInstruction || rcDef instanceof CFGMethodCallInstruction) return null;
                if (rcDef != null && !data.getBlock(rcDef).equals(header)
                        && data.getDominatorTree().dominates(data.getBlock(rcDef), header)) {
                    return switch (bin.operator()) {
                        case PLUS, MINUS, LEQ, GEQ, LT, GT, NEQ -> new IvRcPair(iv, new RegionConstant(rc), bin.operator()); // NEQ?
                        default -> null;
                    };
                }

                iv = bin.right();
                rc = bin.left();

                if (rcDef != null && !data.getBlock(rcDef).equals(header)
                        && data.getDominatorTree().dominates(data.getBlock(rcDef), header)) {
                    return switch (bin.operator()) {
                        case PLUS, NEQ -> new IvRcPair(iv, new RegionConstant(rc), bin.operator());
                        case LEQ, GEQ, LT, GT -> new IvRcPair(iv, new RegionConstant(rc), OptUtils.reverseComparison(bin.operator()));
                        default -> null;
                    };
                }
            }
            return null;
        }

        /** -1 if not known, -2 if infinite */
        private long getNumIterations() {
            if (ivInit == null) return -1;
            if (!ivInit.isLiteral) return -1; // could be smarter later
            if (!updatePair.rc.isLiteral) return -1;
            if (!condPair.rc.isLiteral) return -1;

            BigInteger init = BigInteger.valueOf(new NumUtils(ivInit.literal).asLong());
            BigInteger update = BigInteger.valueOf(new NumUtils(updatePair.rc.literal).asLong());
            BigInteger cond = BigInteger.valueOf(new NumUtils(condPair.rc.literal).asLong());
            BigInteger factor = BigInteger.valueOf(updatePair.op == BinaryOperator.MINUS ? -1 : 1);

            if (!((BoolLiteral) OptUtils.compute(condPair.op, ivInit.literal, condPair.rc.literal)).value())
                return 0;
            if (cond.subtract(init).multiply(update).multiply(factor).compareTo(BigInteger.ZERO) < 0) return -2;

            BigInteger estimate = cond.subtract(init).divide(update).multiply(factor);
            for (long i = Math.max(0, estimate.longValue() - 2); i <= estimate.longValue() + 2; i++) {
                if (!((BoolLiteral) OptUtils.compute(condPair.op,
                        new LongLiteral(init.longValue() + i * update.longValue() * factor.longValue()),
                        new LongLiteral(cond.longValue()))).value())
                    return i;
            }

            return -1; // wtf?
        }

        private record IvRcPair(CFGAddress iv, RegionConstant rc, BinaryOperator op) {}

    }

}
// \end{asbestos}
