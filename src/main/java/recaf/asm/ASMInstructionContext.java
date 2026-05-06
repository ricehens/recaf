package recaf.asm;

public class ASMInstructionContext {

    /** number of parameters to first call instruction */
    public final int numParams;
    /** nececssary subq %rsp before call due to passing > 6 parameters */
    public final int subq;
    /** nececssary addq %rsp after call due to passing > 6 parameters */
    public final int addq;

    /** amount of stack shift (subq) leading up to current instruction */
    public final int pushed;

    public ASMInstructionContext() {
        this.numParams = 0;
        this.subq = 0;
        this.addq = 0;
        this.pushed = 0;
    }

    public ASMInstructionContext(int numParams, int subq, int addq) {
        this.numParams = numParams;
        this.subq = subq;
        this.addq = addq;
        this.pushed = 0;
    }

    public ASMInstructionContext(int pushed) {
        this.numParams = 0;
        this.subq = 0;
        this.addq = 0;
        this.pushed = pushed;
    }

    public ASMInstructionContext(int numParams, int subq, int addq, int pushed) {
        this.numParams = numParams;
        this.subq = subq;
        this.addq = addq;
        this.pushed = pushed;
    }

}
