package recaf.asm;

/**
 * Represents indexing into an array stored on the stack.
 */
public class ASMStackAddressArray implements ASMMemoryLocation {

    private final int offset;
    private final ASMMutableLiteral additionalOffset;
    private final ASMRegister register;
    private final ASMRegister register2;
    private final int align;

    /**
     * Constructs first stack array-index from first custom register.
     *
     * @param offset    the offset from the value in the given register
     * @param register  the register containing the stack address,
     *                  usually the base pointer %rbp
     * @param register2 the register containing the index
     * @param align     the size of each element in the array
     * @param additionalOffset any additional mutable offset
     */
    public ASMStackAddressArray(int offset, ASMMutableLiteral additionalOffset, ASMRegister register, ASMRegister register2, int align) {
        this.offset = offset;
        this.register = register;
        this.register2 = register2;
        this.align = align;
        this.additionalOffset = additionalOffset;
    }

    /**
     * Constructs first stack array-index from first custom register.
     *
     * @param offset    the offset from the value in the given register
     * @param register  the register containing the stack address,
     *                  usually the base pointer %rbp
     * @param register2 the register containing the index
     * @param align     the size of each element in the array
     */
    public ASMStackAddressArray(int offset, ASMRegister register, ASMRegister register2, int align) {
        this(offset, new ASMMutableLiteral(0), register, register2, align);
    }

    /**
     * Constructs first stack array-index.
     *
     * @param offset    the offset from the base pointer %rbp
     * @param register2 the register containing the index
     * @param align     the size of each element in the array
     */
    public ASMStackAddressArray(int offset, ASMRegister register2, int align) {
        this(offset, ASMRegister.RBP, register2, align);
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

    public ASMRegister getRegister2() {
        return register2;
    }

    public int getAlign() {
        return align;
    }

    @Override
    public String toString() {
        return String.format("%s(%s,%s%s)",
                offset + additionalOffset.getValue() == 0 && register != null ? "" : offset + additionalOffset.getValue(),
                register == null ? "" : register.toString(),
                register2.toString(),
                align == 1 ? "" : String.format(",%d", align));
    }

}
