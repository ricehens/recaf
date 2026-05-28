package recaf.cfg;

import recaf.general.UnaryOperator;

import java.util.List;

/**
 * Represents an CFG instruction with a unary operator, such as minus or not.
 */
public class CFGUnaryInstruction implements CFGInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress address;
    private final UnaryOperator operator;
    private final CFGAddress operand;

    /**
     * Constructs a new unary instruction.
     *
     * @param ctx      the CFG context
     * @param address  the destination address
     *                of which only a copy is stored.
     * @param operator the operator
     * @param operand  the operand
     *                of which only a copy is stored.
     */
    public CFGUnaryInstruction(CFGContext ctx, CFGAddress address, UnaryOperator operator, CFGAddress operand) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.address = CFGAddress.clone(address);
        this.operator = operator;
        this.operand = CFGAddress.clone(operand);
    }
    
    public CFGContext ctx() {
        return ctx;
    }
    
    public CFGAddress address() {
        return address;
    }
    
    public UnaryOperator operator() {
        return operator;
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
        return switch (operator) {
            case MINUS -> String.format("%s = neg %s %s", address, ctx.getType(address).toCFGString(), operand);
            case NOT -> String.format("%s = not %s", address, operand);
        };
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CFGInstruction copy() {
        return new CFGUnaryInstruction(ctx, address, operator, operand);
    }
    
    @Override
    public int hashCode() {
        return id;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGUnaryInstruction that && id == that.id;
    }

}
