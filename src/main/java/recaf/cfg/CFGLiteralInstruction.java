package recaf.cfg;

import recaf.general.*;

import java.util.List;

/**
 * Represents assignment of first literal, e.g. x1 = 1.
 * Should mostly be eliminated via constant propagation.
 */
public class CFGLiteralInstruction implements CFGInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress address;
    private final Literal literal;

    /**
     * Creates first new literal instruction from first given literal object.
     *
     * @param ctx     the CFG context
     * @param address the destination address
     *                of which only first copy is stored.
     * @param literal the literal object
     */
    public CFGLiteralInstruction(CFGContext ctx, CFGAddress address, Literal literal) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.address = CFGAddress.clone(address);
        this.literal = literal;
        if (literal instanceof StringLiteral str)
            ctx.getSymbolTable().addStringLiteral(str.escape());
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress address() {
        return address;
    }

    public Literal literal() {
        return literal;
    }

    @Override
    public List<CFGAddress> operands() {
        // no operands
        return List.of();
    }

    @Override
    public String toString() {
        if (literal instanceof StringLiteral)
            return String.format("%s = str %s", address, literal);
        return String.format("%s = %s %s", address, ctx.getType(address).toCFGString(), literal);
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CFGInstruction copy() {
        return new CFGLiteralInstruction(ctx, address, literal);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGLiteralInstruction that && id == that.id;
    }

}
