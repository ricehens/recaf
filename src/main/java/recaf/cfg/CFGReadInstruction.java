package recaf.cfg;

import recaf.general.Type;

import java.util.List;

/**
 * Represents a record/array read instruction in the control flow graph.
 */
public class CFGReadInstruction implements CFGInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress address;
    private final CFGAddress recordAddress;
    private final CFGAddress indexAddress;
    private final int width;

    public CFGReadInstruction(CFGContext ctx, CFGAddress address, CFGAddress recordAddress, int width, CFGAddress indexAddress) {
        id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.address = CFGAddress.clone(address);
        this.recordAddress = CFGAddress.clone(recordAddress);
        this.width = width;
        this.indexAddress = CFGAddress.clone(indexAddress);
    }

    @Override
    public List<CFGAddress> operands() {
        return ctx.getType(recordAddress) == Type.POINTER
                ? List.of(recordAddress, indexAddress)
                : List.of(indexAddress);
    }

    @Override
    public String toString() {
        return String.format("%s = %s[%d x %s]", address, recordAddress, width, indexAddress);
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CFGInstruction copy() {
        return new CFGReadInstruction(ctx, address, recordAddress, width, indexAddress);
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress address() {
        return address;
    }

    public CFGAddress recordAddress() {
        return recordAddress;
    }

    public CFGAddress indexAddress() {
        return indexAddress;
    }

    public int width() {
        return width;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGReadInstruction that && id == that.id;
    }

}
