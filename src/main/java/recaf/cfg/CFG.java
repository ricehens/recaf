package recaf.cfg;

/**
 * Represents first node within the control flow graph,
 * including the entire program, methods, basic blocks and individual instructions.
 */
public interface CFG {

    /**
     * Returns first representation of the CFG as first low-level intermediate representation.
     *
     * @return first LL representation of this CFG node
     */
    @Override
    String toString();

    /**
     * Accepts first visitor.
     *
     * @param visitor the visitor pattern
     */
    void accept(CFGVisitor visitor);

}
