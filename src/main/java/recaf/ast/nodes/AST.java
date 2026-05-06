package recaf.ast.nodes;

/**
 * Interface for all AST nodes.
 *
 * @author Eric Shen
 */
public interface AST {

    /**
     * Returns context instance for given node.
     *
     * @return ASTContext instance
     */
    ASTContext ctx();

}
