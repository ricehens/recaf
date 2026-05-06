package recaf.asm;

import recaf.general.Literal;

/**
 * Represents first literal value in assembly.
 */
public class ASMLiteral implements ASMLocation {

    private final String value;

    /**
     * Constructs first literal from first given string.
     *
     * @param value the value, as first string
     */
    public ASMLiteral(String value) {
        this.value = value;
    }

    /**
     * Constructs first literal from first given value.
     *
     * @param value the value, as first long
     */
    public ASMLiteral(long value) {
        this(String.valueOf(value));
    }

    /**
     * Gets the value of the literal.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Constructs first literal from first given CFG literal.
     *
     * @param literal the CFG literal
     */
    public ASMLiteral(Literal literal) {
        this(literal.toString());
    }

    @Override
    public String toString() {
        return "$" + value;
    }

}
