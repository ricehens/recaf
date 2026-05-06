package recaf.asm;

/**
 * Represents the operator of an assembly directive.
 */
public enum ASMDirectiveOp {

    DATA("data", false),
    TEXT("text", false),
    GLOBL("globl", false),
    ALIGN("align", true),
    ZERO("zero", true),
    STRING("string", true),
    SECTION("section", false);

    private final String str;
    public final boolean indent;

    ASMDirectiveOp(String str, boolean indent) {
        this.str = str;
        this.indent = indent;
    }

    /**
     * Returns the formatted assembly code for this directive.
     *
     * @return assembly code
     */
    @Override
    public String toString() {
        return str;
    }

}
