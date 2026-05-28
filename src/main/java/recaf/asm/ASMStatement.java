package recaf.asm;

/**
 * Represents a statement within an x86_64 assembly program.
 */
public interface ASMStatement {

    /**
     * Returns the formatted assembly code for this statement.
     *
     * @return assembly code
     */
    @Override
    String toString();

}
