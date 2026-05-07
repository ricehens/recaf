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
                new ASMDirective(ASMDirectiveOp.STRING, String.format("\"%s\"", escape())));
    }

    private String escape() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strLiteral.length(); i++) {
            char c = strLiteral.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\0' -> sb.append("\\0");
                default -> {
                    if (c < 32 || c == 127)
                        sb.append(String.format("\\x%02x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }

}
