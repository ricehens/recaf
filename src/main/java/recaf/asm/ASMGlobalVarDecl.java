package recaf.asm;

public class ASMGlobalVarDecl {

    private ASMContext ctx;
    private int id;
    private ASMLabel label;
    private int size;

    public ASMGlobalVarDecl(ASMContext ctx, ASMLabel label, int size) {
        this.ctx = ctx;
        id = ctx.getInstructionCounter();
        this.label = label;
        this.size = size;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ASMGlobalVarDecl that && id == that.id;
    }

    @Override
    public String toString() {
        return String.format("%s%s%s",
                new ASMLabelInstruction(label),
                new ASMDirective(ASMDirectiveOp.ZERO, size),
                new ASMDirective(ASMDirectiveOp.ALIGN, 16));
    }

}
