package recaf.asm;

import recaf.common.Type;

import java.util.Collections;
import java.util.Set;

/**
 * Represents the operator for an assembly instruction.
 */
public enum ASMOperator {

    MOVQ("movq", Type.LONG, Type.LONG, false, true),
    MOVL("movl", Type.INT, Type.INT, false, true),
    MOVB("movb", Type.BOOL, Type.BOOL, false, true),
    MOVSLQ("movslq", Type.INT, Type.LONG, false, true),
    MOVZBL("movzbl", Type.BOOL, Type.INT, false, true),
    MOVZBQ("movzbq", Type.BOOL, Type.LONG, false, true),
    MOVABSQ("movabsq", Type.UNKNOWN, Type.LONG, false, true),
    CLTQ("cltq", null, null, false, false,
            Set.of(ASMRegister.EAX), Set.of(ASMRegister.RAX)),
    CLTD("cltd", null, null, false, false,
            Set.of(ASMRegister.EAX), Set.of(ASMRegister.EAX, ASMRegister.EDX)),
    CQTO("cqto", null, null, false, false,
            Set.of(ASMRegister.RAX), Set.of(ASMRegister.RAX, ASMRegister.RDX)),
    ADDQ("addq", Type.LONG, Type.LONG, true, true),
    ADDL("addl", Type.INT, Type.INT, true, true),
    SUBQ("subq", Type.LONG, Type.LONG, true, true),
    SUBL("subl", Type.INT, Type.INT, true, true),
    IMULQ("imulq", Type.LONG, Type.LONG, true, true),
    IMULO("imulq", null, Type.LONG, true, false,
            Set.of(ASMRegister.RAX), Set.of(ASMRegister.RAX, ASMRegister.RDX)),
    IMULL("imull", Type.INT, Type.INT, true, true),
    IDIVQ("idivq", null, Type.LONG, true, false,
            Set.of(ASMRegister.RAX, ASMRegister.RDX), Set.of(ASMRegister.RAX)),
    IDIVL("idivl", null, Type.INT, true, false,
            Set.of(ASMRegister.EAX, ASMRegister.EDX), Set.of(ASMRegister.EAX)),
    SHLQ("shlq", Type.BOOL, Type.LONG, true, true),
    SHLL("shll", Type.BOOL, Type.INT, true, true),
    SHRQ("shrq", Type.BOOL, Type.LONG, true, true),
    SHRL("shrl", Type.BOOL, Type.INT, true, true),
    SARQ("sarq", Type.BOOL, Type.LONG, true, true),
    SARL("sarl", Type.BOOL, Type.INT, true, true),
    CMPQ("cmpq", Type.LONG, Type.LONG, true, false),
    CMPL("cmpl", Type.INT, Type.INT, true, false),
    CMPB("cmpb", Type.BOOL, Type.BOOL, true, false),
    SETL("setl", null, Type.BOOL, false, true),
    SETG("setg", null, Type.BOOL, false, true),
    SETLE("setle", null, Type.BOOL, false, true),
    SETGE("setge", null, Type.BOOL, false, true),
    SETE("sete", null, Type.BOOL, false, true),
    SETNE("setne", null, Type.BOOL, false, true),
    JL("jl", null, Type.UNKNOWN, false, false),
    JG("jg", null, Type.UNKNOWN, false, false),
    JLE("jle", null, Type.UNKNOWN, false, false),
    JGE("jge", null, Type.UNKNOWN, false, false),
    JE("je", null, Type.UNKNOWN, false, false),
    JNE("jne", null, Type.UNKNOWN, false, false),
    JMP("jmp", null, Type.UNKNOWN, false, false),
    LEAQ("leaq", Type.UNKNOWN, Type.LONG, false, true),
    LEAL("leal", Type.UNKNOWN, Type.INT, false, true),
    PUSHQ("pushq", null, Type.LONG, true, false),
    SYSCALL("syscall", null, null, false, false),
    LEAVE("leave", null, null, false, false),
    RET("ret", null, null, false, false),
    NEGQ("negq", null, Type.LONG, true, true),
    NEGL("negl", null, Type.INT, true, true),
    XORQ("xorq", Type.LONG, Type.LONG, true, true),
    XORL("xorl", Type.INT, Type.INT, true, true),
    XORB("xorb", Type.BOOL, Type.BOOL, true, true),
    ANDQ("andq", Type.LONG, Type.LONG, true, true),
    ANDL("andl", Type.INT, Type.INT, true, true),
    CALL("call", null, Type.UNKNOWN, false, false,
            //Set.of(ASMRegister.RDI, ASMRegister.RSI, ASMRegister.RDX, ASMRegister.RCX, ASMRegister.R8, ASMRegister.R9),
            Set.of(),
            Set.of(ASMRegister.RAX)),
    TESTQ("testq", Type.LONG, Type.LONG, true, false),
    TESTL("testl", Type.INT, Type.INT, true, false),
    CMOVSQ("cmovs", Type.LONG, Type.LONG, true, true),
    CMOVSL("cmovs", Type.INT, Type.INT, true, true),
    ;

    private final String str;
    public final Type src;
    public final Type dest;
    public final boolean useDest; // whether the destination register is read from
    public final boolean defDest; // whether the destination register is written to
    public final Set<ASMRegister> extraOperands; // physical registers used
    public final Set<ASMRegister> extraDestinations; // physical registers written to

    ASMOperator(String str, Type src, Type dest, boolean useDest, boolean defDest) {
        this(str, src, dest, useDest, defDest, Collections.emptySet(), Collections.emptySet());
    }

    ASMOperator(String str, Type src, Type dest, boolean useDest, boolean defDest,
                Set<ASMRegister> extraOperands, Set<ASMRegister> extraDestinations) {
        this.str = str;
        this.src = src;
        this.dest = dest;
        this.useDest = useDest;
        this.defDest = defDest;
        this.extraOperands = Collections.unmodifiableSet(extraOperands);
        this.extraDestinations = Collections.unmodifiableSet(extraDestinations);
    }

    /**
     * Returns the formatted assembly code for this operator.
     *
     * @return assembly code
     */
    @Override
    public String toString() {
        return str;
    }

}
