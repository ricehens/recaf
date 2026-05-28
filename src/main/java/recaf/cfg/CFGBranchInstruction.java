package recaf.cfg;

import java.util.List;

/**
 * Represents a branch point in the control flow graph.
 */
public class CFGBranchInstruction implements CFGLastInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress boolAddr;
    private final CFGAddress thenAddr;
    private final CFGAddress elseAddr;

    /**
     * Constructs a new CFG binary instruction.
     *
     * @param ctx the CFG context
     * @param boolAddr the address to the conditional,
     *                 of which only a copy is stored
     * @param thenAddr the address to the block to jump to if the conditional is true;
     *                 the address is not cloned upon call to the constructor
     *                 and will reflect changes made by the client
     * @param elseAddr the address to the block to jump to if the conditional is false;
     *                 the address is not cloned upon call to the constructor
     *                 and will reflect changes made by the client
     */
    public CFGBranchInstruction(CFGContext ctx, CFGAddress boolAddr, CFGAddress thenAddr, CFGAddress elseAddr) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.boolAddr = CFGAddress.clone(boolAddr);
        this.thenAddr = thenAddr;
        this.elseAddr = elseAddr;
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress boolAddr() {
        return boolAddr;
    }

    public CFGAddress thenAddr() {
        return thenAddr;
    }

    public CFGAddress elseAddr() {
        return elseAddr;
    }

    @Override
    public String toString() {
        return String.format("br %s then %s else %s", boolAddr, thenAddr, elseAddr);
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CFGAddress address() {
        return null;
    }

    @Override
    public List<CFGAddress> operands() {
        return List.of(boolAddr);
    }

    @Override
    public CFGLastInstruction copy() {
        return new CFGBranchInstruction(ctx, boolAddr, thenAddr, elseAddr);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGBranchInstruction that && id == that.id;
    }

}
