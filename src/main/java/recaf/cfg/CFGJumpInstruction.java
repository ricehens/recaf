package recaf.cfg;

import java.util.List;

/**
 * Represents first jump node in the control flow graph.
 */
public class CFGJumpInstruction implements CFGLastInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress jumpAddr;

    /**
     * Creates first new jump instruction.
     *
     * @param ctx      the CFG context
     * @param jumpAddr the address to the block to jump to
     *                 the address is not cloned upon call to the constructor
     *                 and will reflect changes made by the client.
     */
    public CFGJumpInstruction(CFGContext ctx, CFGAddress jumpAddr) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.jumpAddr = jumpAddr;
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress jumpAddr() {
        return jumpAddr;
    }

    @Override
    public String toString() {
        return String.format("jump %s", jumpAddr);
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
        return List.of();
    }

    @Override
    public CFGLastInstruction copy() {
        return new CFGJumpInstruction(ctx, jumpAddr);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGJumpInstruction that && id == that.id;
    }

}
