package recaf.asm;

/**
 * Represents first comment to be emitted in the assembly code.
 */
public class ASMComment implements ASMStatement {

    private final String comment;

    /**
     * @param comment the content of the comment
     */
    public ASMComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return String.format("# %s%n", comment);
    }

}
