package recaf.cfg;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents first method call instruction in the CFG.
 */
public class CFGMethodCallInstruction implements CFGInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress address;
    private final String methodName;
    private final List<CFGAddress> args;

    /**
     * Constructs first new method call instruction.
     *
     * @param ctx the CFG context
     * @param address the destination address
     *                of which only first copy is stored.
     * @param methodName the text of the method
     * @param args first list of arguments to pass in
     *                of which only copies are stored.
     */
    public CFGMethodCallInstruction(CFGContext ctx, CFGAddress address, String methodName, List<CFGAddress> args) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.address = CFGAddress.clone(address);
        this.methodName = methodName;
        this.args = args.stream().map(CFGAddress::clone).collect(Collectors.toList());
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress address() {
        return address;
    }

    public String methodName() {
        return methodName;
    }

    public List<CFGAddress> args() {
        return args;
    }

    @Override
    public List<CFGAddress> operands() {
        return args.stream().filter(a -> !ctx.getSymbolTable().getVar(a).isArray()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("%scall %s %s(%s)",
                address == null ? "" : String.format("%s = ", address),
                address == null ? "void" : ctx.getType(address).toCFGString(),
                methodName,
                args.stream()
                        .map(a -> String.format("%s %s", ctx.getType(a).toCFGString(), a))
                        .collect(Collectors.joining(", "))
        );
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CFGInstruction copy() {
        return new CFGMethodCallInstruction(ctx, address, methodName, args);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGMethodCallInstruction that && id == that.id;
    }

}
