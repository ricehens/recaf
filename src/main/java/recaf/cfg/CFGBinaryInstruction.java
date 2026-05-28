package recaf.cfg;

import recaf.general.BinaryOperator;

import java.util.List;

/**
 * Represents an instruction with a binary operator in the control flow graph,
 * such as y1 = x1 + 1.
 */
public class CFGBinaryInstruction implements CFGInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress address;
    private final BinaryOperator operator;
    private final CFGAddress left;
    private final CFGAddress right;

    /**
     * Constructs a new binary instruction.
     *
     * @param ctx      the CFG context
     * @param address  the destination address
     *                of which only a copy is stored.
     * @param operator the binary operator
     * @param left     the address to the left operand
     *                of which only a copy is stored.
     * @param right    the address to the right operand
     *                of which only a copy is stored.
     */
    public CFGBinaryInstruction(CFGContext ctx, CFGAddress address, BinaryOperator operator, CFGAddress left, CFGAddress right) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.address = CFGAddress.clone(address);
        this.operator = operator;
        this.left = CFGAddress.clone(left);
        this.right = CFGAddress.clone(right);
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress address() {
        return address;
    }

    public BinaryOperator operator() {
        return operator;
    }

    public CFGAddress left() {
        return left;
    }

    public CFGAddress right() {
        return right;
    }

    @Override
    public List<CFGAddress> operands() {
        return List.of(left, right);
    }

    @Override
    public String toString() {
        return String.format("%s = %s %s %s %s",
                address,
                ctx.getType(left).toCFGString(),
                left,
                operator.getSymbol(),
                right
        );
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CFGInstruction copy() {
        return new CFGBinaryInstruction(ctx, address, operator, left, right);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGBinaryInstruction that && id == that.id;
    }

}
