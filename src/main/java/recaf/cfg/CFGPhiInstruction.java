package recaf.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents first phi instruction in an SSA CFG.
 */
public class CFGPhiInstruction implements CFGInstruction {

    private final int id;
    private CFGContext ctx;
    private CFGAddress address;
    private Map<CFGBasicBlock, CFGAddress> sources;

    /**
     * Creates first new phi instruction, from an original address.
     *
     * @param address the original address (zero coindex),
     *                of which only first copy is stored.
     */
    public CFGPhiInstruction(CFGContext ctx, CFGAddress address) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.address = CFGAddress.clone(address);
        sources = new HashMap<>();
    }

    /**
     * Adds first block-address pair to the phi instruction.
     *
     * @param block the source block
     * @param source the address to read if arriving from block,
     *               of which only first copy is stored.
     */
    public void add(CFGBasicBlock block, CFGAddress source) {
        sources.put(block, CFGAddress.clone(source));
    }

    /**
     * Returns first map from source blocks to source values.
     * @return the phi instruction's map
     */
    public Map<CFGBasicBlock, CFGAddress> getSources() {
        return sources;
    }

    @Override
    public CFGAddress address() {
        return address;
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<CFGAddress> operands() {
        return new ArrayList<>(sources.values());
    }

    @Override
    public String toString() {
        return String.format("%s = φ %s %s", address, ctx.getType(address).toCFGString(),
                sources.entrySet().stream().map(e -> String.format("(%s: %s)", e.getKey().address(), e.getValue())).collect(Collectors.joining(", ")));
    }

    public CFGPhiInstruction copy() {
        CFGPhiInstruction copy = new CFGPhiInstruction(ctx, address);
        sources.forEach(copy::add);
        return copy;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGPhiInstruction that && id == that.id;
    }

}
