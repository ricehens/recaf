package recaf.cfg;

import recaf.general.Type;
import recaf.utils.DoublyLinkedList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a method within the control flow graph.
 */
public class CFGMethod implements CFG {

    private final CFGContext ctx;
    private final Type type;
    private final String name;
    private final List<CFGAddress> params;
    private final List<CFGAddress> localVars;
    private final DoublyLinkedList<CFGBasicBlock> blocks;

    /**
     * Constructs a new method object.
     *
     * @param ctx     the CFG context
     * @param type    the return type
     * @param name    the text of the method
     * @param params  a list of parameters; defensive copies are made
     * @param localVars list of local variables; defensive copies are made
     * @param builder the CFG builder object
     */
    public CFGMethod(CFGContext ctx, Type type, String name, List<CFGAddress> params,
                     List<CFGAddress> localVars, CFGBuilder builder) {
        this.ctx = ctx;
        this.type = type;
        this.name = name;
        this.params = params.stream().map(CFGAddress::clone).collect(Collectors.toList());
        this.localVars = localVars.stream().map(CFGAddress::clone).collect(Collectors.toList());
        blocks = builder.getBlocks(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s %s(", type.toCFGString(), name));
        for (int i = 0; i < params.size(); i++) {
            CFGAddress param = params.get(i);
            sb.append(String.format("%s %s", ctx.getType(param).toCFGString(), param));
            if (i < params.size() - 1) sb.append(", ");
        }
        sb.append(") {").append(System.lineSeparator());

        for (CFGBasicBlock block : blocks) {
            sb.append(block);
        }

        sb.append("}").append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the CFG context object.
     *
     * @return the CFG context
     */
    public CFGContext ctx() {
        return ctx;
    }

    /**
     * Gets the text of the method.
     *
     * @return the method text
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the return type.
     *
     * @return the return type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the method's parameters.
     *
     * @return a list of parameters
     */
    public List<CFGAddress> getParams() {
        return params;
    }

    /**
     * Returns the method's local variables.
     *
     * @return list of local variables
     */
    public List<CFGAddress> getLocalVars() {
        return localVars;
    }

    /**
     * Returns the method's basic blocks.
     *
     * @return a list of basic blocks
     */
    public DoublyLinkedList<CFGBasicBlock> getBlocks() {
        return blocks;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGMethod that && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
