package recaf.opt;

import recaf.cfg.CFGBasicBlock;
import recaf.cfg.CFGBranchInstruction;
import recaf.cfg.CFGContext;
import recaf.cfg.CFGMethod;

import java.util.*;

public class LoopUtils {

    private CFGMethod method;
    private CFGContext ctx;
    private SSAData data;
    private List<Loop> loops;

    public LoopUtils(CFGMethod method) {
        this.method = method;
        ctx = method.ctx();
        data = new SSAData(method);
        findLoops();
    }

    private void findLoops() {
        loops = new ArrayList<>();

        for (CFGBasicBlock block : method.getBlocks()) {
            if (block.getLastInstruction() instanceof CFGBranchInstruction branch) {
                CFGBasicBlock thenBlock = ctx.getSymbolTable().getBlock(branch.thenAddr());
                CFGBasicBlock elseBlock = ctx.getSymbolTable().getBlock(branch.elseAddr());
                if (data.getDominatorTree().dominates(thenBlock, block)) {
                    loops.add(new Loop(method, data, thenBlock, block));
                } else if (data.getDominatorTree().dominates(elseBlock, block)) {
                    loops.add(new Loop(method, data, elseBlock, block));
                }
            }
        }

        loops.sort(Comparator.comparingInt(l -> l.blocks.size()));
/*
System.out.println(loops);
for (Loop loop : loops) {
    System.out.printf("%s: %s%n", loop, loop.blocks.stream().map(CFGBasicBlock::address).toList());
    System.out.printf("init: %s%n", loop.ivInit);
    System.out.printf("update: %s%n", loop.updatePair);
    System.out.printf("cond: %s%n", loop.condPair);
    System.out.printf("numIters: %d%n", loop.getNumIterations());
}
 */
    }

    public List<Loop> getLoops() {
        return loops;
    }

    public static class Loop {

        /** The method containing the loop */
        CFGMethod method;
        /** The SSA data instance */
        private SSAData data;
        /** The loop header block, which dominates all loop blocks */
        CFGBasicBlock header;
        /** The block which contains a back edge back to the header */
        CFGBasicBlock backEdge;
        /** The branch instruction, within the "back edge" block */
        CFGBranchInstruction branch;
        /** The set of all blocks in the loop */
        Set<CFGBasicBlock> blocks;
        /** The list of blocks in order */
        List<CFGBasicBlock> blockList;

        public Loop(CFGMethod method, SSAData data, CFGBasicBlock header, CFGBasicBlock backEdge) {
            this.method = method;
            this.data = data;
            this.header = header;
            this.backEdge = backEdge;
            branch = (CFGBranchInstruction) backEdge.getLastInstruction();
            findBlocks();
        }

        private void findBlocks() {
            blocks = new HashSet<>();
            Queue<CFGBasicBlock> workList = new LinkedList<>();
            workList.offer(backEdge);
            blocks.add(header);

            while (!workList.isEmpty()) {
                CFGBasicBlock b = workList.poll();
                if (blocks.contains(b)) continue;
                blocks.add(b);
                workList.addAll(data.getDominatorTree().getPredecessors(b));
            }

            blockList = new ArrayList<>();
            for (CFGBasicBlock b : method.getBlocks()) {
                if (blocks.contains(b))
                    blockList.add(b);
            }
        }



        @Override
        public boolean equals(Object obj) {
            return obj instanceof Loop that && header.equals(that.header) && backEdge.equals(that.backEdge);
        }

        @Override
        public int hashCode() {
            return Objects.hash(header, backEdge);
        }

        @Override
        public String toString() {
            return String.format("Loop(header=%s, backEdge=%s)", header.address(), backEdge.address());
        }

    }

}
