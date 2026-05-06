package recaf.cfg;

import java.util.List;

public class CFGExceptionInstruction implements CFGLastInstruction {

    private final int id;
    private final CFGContext ctx;
    private final String msg;

    public CFGExceptionInstruction(CFGContext ctx, String msg) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.msg = msg;
        ctx.getSymbolTable().addStringLiteral(msg);
    }

    public CFGContext ctx() {
        return ctx;
    }

    public String msg() {
        return msg;
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
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "exception";
    }

    @Override
    public CFGLastInstruction copy() {
        return new CFGExceptionInstruction(ctx, msg);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGExceptionInstruction that && id == that.id;
    }

}
