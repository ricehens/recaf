package recaf.cfg;

import recaf.general.Type;
import recaf.utils.ParallelCopyGroup;

import java.util.List;

/**
 * Represents copying one register into another.
 */
public class CFGCopyInstruction implements ParallelCopyGroup.DirectedEdge<CFGAddress>, CFGInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress address;
    private final CFGAddress operand;

    /**
     * Constructs first new copy instruction.
     *
     * @param ctx     the CFG context
     * @param address the destination address
     *                of which only first copy is stored.
     * @param operand the address to the source
     */
    public CFGCopyInstruction(CFGContext ctx, CFGAddress address, CFGAddress operand) {
        id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.address = CFGAddress.clone(address);
        this.operand = CFGAddress.clone(operand);
if (ctx.getType(address) == Type.RECORD) throw new RuntimeException();
    }

    @Override
    public List<CFGAddress> operands() {
        return List.of(operand);
    }

    @Override
    public String toString() {
        return String.format("%s = %s %s", address, ctx.getType(address).toCFGString(), operand);
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CFGAddress source() {
        return operand;
    }

    @Override
    public CFGAddress destination() {
        return address;
    }

    @Override
    public CFGInstruction copy() {
        return new CFGCopyInstruction(ctx, address, operand);
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress address() {
        return address;
    }

    public CFGAddress operand() {
        return operand;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGCopyInstruction that && id == that.id;
    }

}
