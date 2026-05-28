package recaf.cfg;

import java.util.List;

/**
 * Represents a return instruction within a CFG method.
 */
public class CFGReturnInstruction implements CFGLastInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress returnAddress;

    /**
     * Creates a non-void return instruction
     *
     * @param ctx           the CFG context
     * @param returnAddress the return address
     *                of which only a copy is stored.
     */
    public CFGReturnInstruction(CFGContext ctx, CFGAddress returnAddress) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.returnAddress = returnAddress != null ? CFGAddress.clone(returnAddress) : null;
    }

    /**
     * Creates a void return instruction.
     *
     * @param ctx the CFG context
     */
    public CFGReturnInstruction(CFGContext ctx) {
        this(ctx, null);
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress returnAddress() {
        return returnAddress;
    }

    @Override
    public String toString() {
        return returnAddress == null ? "ret" : String.format("ret %s %s", ctx.getType(returnAddress).toCFGString(), returnAddress);
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
        return returnAddress == null ? List.of() : List.of(returnAddress);
    }

    @Override
    public CFGLastInstruction copy() {
        return new CFGReturnInstruction(ctx, returnAddress);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGReturnInstruction that && id == that.id;
    }

}
