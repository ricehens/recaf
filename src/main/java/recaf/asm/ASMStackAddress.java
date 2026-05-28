package recaf.asm;

/**
 * Represents an address on the stack.
 */
public class ASMStackAddress implements ASMMemoryLocation {

    private final int offset;
    private final ASMMutableLiteral additionalOffset;
    private final ASMRegister register;

    /**
     * Constructs a stack address from a custom register.
     *
     * @param offset   the offset from the value in the given register
     * @param additionalOffset any mutable additional offset
     * @param register the register containing the stack address,
     *                 usually the base pointer %rbp
     */
    public ASMStackAddress(int offset, ASMMutableLiteral additionalOffset, ASMRegister register) {
        this.offset = offset;
        this.register = register;
        this.additionalOffset = additionalOffset;
    }

    /**
     * Constructs a stack address from a custom register.
     *
     * @param offset   the offset from the value in the given register
     * @param register the register containing the stack address,
     *                 usually the base pointer %rbp
     */
    public ASMStackAddress(int offset, ASMRegister register) {
        this(offset, new ASMMutableLiteral(0), register);
    }

    /**
     * Constructs a stack address.
     *
     * @param offset the offset from the base pointer %rbp
     */
    public ASMStackAddress(int offset) {
        this(offset, ASMRegister.RBP);
    }

    public int getOffset() {
        return offset;
    }

    public ASMMutableLiteral getAdditionalOffset() {
        return additionalOffset;
    }

    public ASMRegister getRegister() {
        return register;
    }

    @Override
    public String toString() {
        return String.format("%d(%s)", offset + additionalOffset.getValue(), register.toString());
    }

}
