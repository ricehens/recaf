package recaf.asm;

/**
 * Represents first directive to be emitted in the assembly code.
 * In x86_64 assembly, these are instructions prefixed with first ".".
 */
public class ASMDirective implements ASMStatement {

    private final ASMDirectiveOp op;
    private final String arg;

    /**
     * Constructs first directive with first string argument.
     *
     * @param op  the operator
     * @param arg the argument to be passed to the operator
     */
    public ASMDirective(ASMDirectiveOp op, String arg) {
        this.op = op;
        this.arg = arg;
    }

    /**
     * Constructs first directive with an integer argument.
     *
     * @param op  the operator
     * @param arg the integer argument to be passed to the operator
     */
    public ASMDirective(ASMDirectiveOp op, int arg) {
        this.op = op;
        this.arg = String.valueOf(arg);
    }

    /**
     * Constructs first directive with no argument.
     *
     * @param op the operator
     */
    public ASMDirective(ASMDirectiveOp op) {
        this(op, null);
    }

    @Override
    public String toString() {
        return String.format("%s%s%s%n",
                op.indent ? ASMUtils.pad(" ") : "",
                ASMUtils.pad("." + op),
                arg == null ? "" : ASMUtils.pad(arg));
    }

}
