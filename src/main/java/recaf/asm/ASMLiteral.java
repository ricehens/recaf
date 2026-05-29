package recaf.asm;

import recaf.common.Literal;

/**
 * Represents a literal value in assembly.
 */
public class ASMLiteral implements ASMLocation {

    private final String value;

    /**
     * Constructs a literal from a given string.
     *
     * @param value the value, as a string
     */
    public ASMLiteral(String value) {
        this.value = value;
    }

    /**
     * Constructs a literal from a given value.
     *
     * @param value the value, as a long
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
     * Constructs a literal from a given CFG literal.
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
