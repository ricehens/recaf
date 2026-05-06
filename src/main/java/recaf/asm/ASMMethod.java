package recaf.asm;

import recaf.utils.DoublyLinkedList;
import recaf.utils.HashLinkedList;

public class ASMMethod {

    private ASMContext ctx;
    private String name;
    private int id;

    private DoublyLinkedList<ASMBasicBlock> blocks;

    public ASMMethod(ASMContext ctx, String name) {
        this.ctx = ctx;
        id = ctx.getInstructionCounter();
        this.name = name;
        blocks = new HashLinkedList<>();
    }

    public void offer(ASMBasicBlock block) {
        blocks.offerLast(block);
    }

    public int hashCode() {
        return id;
    }

    public DoublyLinkedList<ASMBasicBlock> getBlocks() {
        return blocks;
    }

    public boolean equals(Object obj) {
        return obj instanceof ASMMethod && ((ASMMethod)obj).id == id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name.equals("main")
                ? new ASMDirective(ASMDirectiveOp.GLOBL, "main").toString() : "");
        for (ASMBasicBlock block : blocks) {
            sb.append(block.toString());
        }
        return sb.toString();
    }

}
