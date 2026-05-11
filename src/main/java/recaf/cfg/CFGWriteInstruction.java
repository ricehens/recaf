package recaf.cfg;

import recaf.general.Type;

import java.util.List;

/**
 * Represents a record/array write instruction in the control flow graph.
 */
public class CFGWriteInstruction implements CFGInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress recordAddress;
    private final CFGAddress indexAddress;
    private final CFGAddress valueAddress;
    private final int width;

    public CFGWriteInstruction(CFGContext ctx, CFGAddress recordAddress, int width, CFGAddress indexAddress, CFGAddress valueAddress) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.recordAddress = CFGAddress.clone(recordAddress);
        this.width = width;
        this.indexAddress = CFGAddress.clone(indexAddress);
        this.valueAddress = CFGAddress.clone(valueAddress);
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress recordAddress() {
        return recordAddress;
    }

    public CFGAddress indexAddress() {
        return indexAddress;
    }

    public CFGAddress valueAddress() {
        return valueAddress;
    }

    public int width() {
        return width;
    }

    @Override
    public List<CFGAddress> operands() {
        return ctx.getType(recordAddress) != Type.RECORD
                ? List.of(recordAddress, indexAddress, valueAddress)
                : List.of(indexAddress, valueAddress);
    }

    @Override
    public String toString() {
        return String.format("%s[%d x %s] = %s", recordAddress, width, indexAddress, valueAddress);
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
    public CFGInstruction copy() {
        return new CFGWriteInstruction(ctx, recordAddress, width, indexAddress, valueAddress);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGWriteInstruction that && id == that.id;
    }

}
