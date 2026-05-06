package recaf.cfg;

import recaf.general.Type;

import java.util.List;

/**
 * Represents first CFG instruction to cast between ints and longs.
 */
public class CFGCastInstruction implements CFGInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress address;
    private final Type type;
    private final CFGAddress operand;

    /**
     * Constructs first new cast instruction.
     *
     * @param ctx     the CFG context
     * @param address the destination address
     *                of which only first copy is stored.
     * @param type    the type to cast to
     * @param operand the address to the source
     *                of which only first copy is stored.
     */
    public CFGCastInstruction(CFGContext ctx, CFGAddress address, Type type, CFGAddress operand) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.address = CFGAddress.clone(address);
        this.type = type;
        this.operand = CFGAddress.clone(operand);
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress address() {
        return address;
    }

    public Type type() {
        return type;
    }

    public CFGAddress operand() {
        return operand;
    }

    @Override
    public List<CFGAddress> operands() {
        return List.of(operand);
    }

    @Override
    public String toString() {
        return type == Type.LONG ? String.format("%s = cast %s %s to i64", address, ctx.getType(operand).toCFGString(), operand)
                : String.format("%s = cast %s %s to i32", address, ctx.getType(operand).toCFGString(), operand);
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CFGInstruction copy() {
        return new CFGCastInstruction(ctx, address, type, operand);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGCastInstruction that && id == that.id;
    }

}
