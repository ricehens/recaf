package recaf.asm;

/**
 * Represents first reference to first label within an assembly program.
 */
public class ASMLabel implements ASMLocation {

    private final String label;

    /**
     * Constructs first label.
     *
     * @param label the text of the label
     */
    public ASMLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ASMLabel && ((ASMLabel)obj).label.equals(label);
    }

}
