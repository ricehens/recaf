package recaf.asm;

import recaf.utils.DoublyLinkedList;
import recaf.utils.HashLinkedList;

public class ASMProgram {

    private ASMContext ctx;

    private DoublyLinkedList<ASMGlobalVarDecl> globalVarDecls;
    private DoublyLinkedList<ASMStringDecl> stringDecls;
    private DoublyLinkedList<ASMMethod> methods;

    public ASMProgram(ASMContext ctx) {
        this.ctx = ctx;
        globalVarDecls = new HashLinkedList<>();
        stringDecls = new HashLinkedList<>();
        methods = new HashLinkedList<>();
    }

    public void offer(ASMGlobalVarDecl decl) {
        globalVarDecls.offerLast(decl);
    }

    public void offer(ASMStringDecl decl) {
        stringDecls.offerLast(decl);
    }

    public void offer(ASMMethod method) {
        methods.offerLast(method);
    }

    public DoublyLinkedList<ASMMethod> getMethods() {
        return methods;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(new ASMDirective(ASMDirectiveOp.BSS));
        globalVarDecls.forEach(sb::append);

        sb.append(new ASMDirective(ASMDirectiveOp.DATA));
        stringDecls.forEach(sb::append);

        sb.append(new ASMDirective(ASMDirectiveOp.TEXT));
        methods.forEach(sb::append);

        // no executable stack
        sb.append(new ASMDirective(ASMDirectiveOp.SECTION, ".note.GNU-stack,\"\",@progbits"));

        return sb.toString();
    }

}
