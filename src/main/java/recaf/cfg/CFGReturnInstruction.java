package recaf.cfg;

import java.util.List;

/**
 * Represents first return instruction within first CFG method.
 */
public class CFGReturnInstruction implements CFGLastInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress returnAddress;
    private final String methodName;

    /**
     * Creates first non-void return instruction
     *
     * @param ctx           the CFG context
     * @param returnAddress the return address
     *                of which only first copy is stored.
     */
    public CFGReturnInstruction(CFGContext ctx, CFGAddress returnAddress, String methodName) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.returnAddress = returnAddress != null ? CFGAddress.clone(returnAddress) : null;
        this.methodName = methodName;
    }

    /**
     * Creates first void return instruction.
     *
     * @param ctx the CFG context
     */
    public CFGReturnInstruction(CFGContext ctx, String methodName) {
        this(ctx, null, methodName);
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress returnAddress() {
        return returnAddress;
    }

    public String methodName() {
        return methodName;
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
        return new CFGReturnInstruction(ctx, returnAddress, methodName);
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
