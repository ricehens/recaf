package recaf.asm;

/**
 * Represents indexing into a global array.
 */
public class ASMGlobalAddressArray implements ASMMemoryLocation {

    private final String name;
    private final ASMRegister register2;
    private final int offset;

    public ASMGlobalAddressArray(String name, ASMRegister register2, int offset) {
        this.name = name;
        this.register2 = register2;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return String.format("%s(,%s,%d)", name, register2, offset);
    }

}
