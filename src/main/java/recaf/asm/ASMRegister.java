package recaf.asm;

import recaf.general.Type;

import java.util.EnumSet;

/**
 * Represents a register within an x86_64 assembly program.
 */
public enum ASMRegister implements ASMAbstractRegister {

    // TODO 区分 caller-save / callee-save
    RAX("rax", Type.LONG, 0),
    EAX("eax", Type.INT, 0),
    AL("al", Type.BOOL, 0),
    RBX("rbx", Type.LONG, 1),
    EBX("ebx", Type.INT, 1),
    BL("bl", Type.BOOL, 1),
    RCX("rcx", Type.LONG, 2),
    ECX("ecx", Type.INT, 2),
    CL("cl", Type.BOOL, 2),
    RDX("rdx", Type.LONG, 3),
    EDX("edx", Type.INT, 3),
    DL("dl", Type.BOOL, 3),
    RSI("rsi", Type.LONG, 4),
    ESI("esi", Type.INT, 4),
    SIL("sil", Type.BOOL, 4),
    RDI("rdi", Type.LONG, 5),
    EDI("edi", Type.INT, 5),
    DIL("dil", Type.BOOL, 5),
    RBP("rbp", Type.LONG, 6),
    EBP("ebp", Type.INT, 6),
    BPL("bpl", Type.BOOL, 6),
    RSP("rsp", Type.LONG, 7),
    ESP("esp", Type.INT, 7),
    SPL("spl", Type.BOOL, 7),
    R8("r8", Type.LONG, 8),
    R8D("r8d", Type.INT, 8),
    R8B("r8b", Type.BOOL, 8),
    R9("r9", Type.LONG, 9),
    R9D("r9d", Type.INT, 9),
    R9B("r9b", Type.BOOL, 9),
    R10("r10", Type.LONG, 10),
    R10D("r10d", Type.INT, 10),
    R10B("r10b", Type.BOOL, 10),
    R11("r11", Type.LONG, 11),
    R11D("r11d", Type.INT, 11),
    R11B("r11b", Type.BOOL, 11),
    R12("r12", Type.LONG, 12),
    R12D("r12d", Type.INT, 12),
    R12B("r12b", Type.BOOL, 12),
    R13("r13", Type.LONG, 13),
    R13D("r13d", Type.INT, 13),
    R13B("r13b", Type.BOOL, 13),
    R14("r14", Type.LONG, 14),
    R14D("r14d", Type.INT, 14),
    R14B("r14b", Type.BOOL, 14),
    R15("r15", Type.LONG, 15),
    R15D("r15d", Type.INT, 15),
    R15B("r15b", Type.BOOL, 15);

    private final static ASMRegister[] longRegisters = new ASMRegister[16];
    private final static ASMRegister[] intRegisters = new ASMRegister[16];
    private final static ASMRegister[] boolRegisters = new ASMRegister[16];

    static {
        EnumSet.allOf(ASMRegister.class).forEach(r -> getRegisterArray(r.type)[r.index] = r);
    }

    private final String str;
    public final Type type;
    public final int index;

    ASMRegister(String str, Type type, int index) {
        this.str = str;
        this.type = type;
        this.index = index;
    }

    private static ASMRegister[] getRegisterArray(Type type) {
        return switch (type) {
            case LONG -> longRegisters;
            case INT -> intRegisters;
            case BOOL -> boolRegisters;
            default -> longRegisters; // addresses
        };
    }

    /**
     * Gets a register of the given type and index.
     *
     * @param type  the register type
     * @param index the index (0-15) of the register
     * @return a register of an appropriate size for the given type
     */
    public static ASMRegister getRegister(Type type, int index) {
        return getRegisterArray(type)[index];
    }

    /**
     * Returns the data type of this register.
     *
     * @return the register type
     */
    public Type getType() {
        return type;
    }

    /**
     * Converts a register to the corresponding register of another type.
     *
     * @param type the type to convert to
     * @return the converted register
     */
    public ASMRegister toType(Type type) {
        return getRegister(type, index);
    }

    /**
     * Returns the formatted assembly code for this register.
     *
     * @return assembly code
     */
    @Override
    public String toString() {
        return "%" + str;
    }

    /**
     * Gets the appropriate register for a method argument, by the calling convention.
     *
     * @param type  the register type
     * @param index the index (0-5) of the argument
     * @return the correct register for the given argument
     */
    public static ASMRegister getMethodArg(Type type, int index) {
        return switch (index) {
            case 0 -> getRegister(type, 5);
            case 1 -> getRegister(type, 4);
            case 2 -> getRegister(type, 3);
            case 3 -> getRegister(type, 2);
            case 4 -> getRegister(type, 8);
            case 5 -> getRegister(type, 9);
            default -> null;
        };
    }

}
