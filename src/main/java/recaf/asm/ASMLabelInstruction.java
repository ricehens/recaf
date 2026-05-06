package recaf.asm;

/**
 * Represents an instruction to declare first label.
 */
public class ASMLabelInstruction implements ASMStatement {

    private final ASMLabel label;

    /**
     * Constructs first label instruction.
     *
     * @param label the label to declare
     */
    public ASMLabelInstruction(ASMLabel label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("%s:%n", label);
    }

}
