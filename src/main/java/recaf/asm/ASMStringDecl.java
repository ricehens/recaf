package recaf.asm;

public class ASMStringDecl {

    private ASMContext ctx;
    private int id;
    private ASMLabel label;
    private String strLiteral;

    public ASMStringDecl(ASMContext ctx, ASMLabel label, String strLiteral) {
        this.ctx = ctx;
        id = ctx.getInstructionCounter();
        this.label = label;
        this.strLiteral = strLiteral;
    }

    public int hashCode() {
        return id;
    }

    public boolean equals(Object obj) {
        return obj instanceof ASMStringDecl && ((ASMStringDecl)obj).id == id;
    }

    public String toString() {
        return String.format("%s%s%s",
                new ASMDirective(ASMDirectiveOp.ALIGN, 16),
                new ASMLabelInstruction(label),
                new ASMDirective(ASMDirectiveOp.STRING, String.format("\"%s\"", strLiteral)));
    }

}
