package recaf.cfg;

/**
 * Represents a node within the control flow graph,
 * including the entire program, methods, basic blocks and individual instructions.
 */
public interface CFG {

    /**
     * Returns a representation of the CFG as a low-level intermediate representation.
     *
     * @return a LL representation of this CFG node
     */
    @Override
    String toString();

    /**
     * Accepts a visitor.
     *
     * @param visitor the visitor pattern
     */
    void accept(CFGVisitor visitor);

}
