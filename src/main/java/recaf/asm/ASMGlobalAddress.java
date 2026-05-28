package recaf.asm;

/**
 * Represents the address to a global variable.
 */
public class ASMGlobalAddress implements ASMMemoryLocation {

    private final String name;

    public ASMGlobalAddress(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("%s(%%rip)", name);
    }

}
