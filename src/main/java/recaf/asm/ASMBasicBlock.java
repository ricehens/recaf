package recaf.asm;

import recaf.utils.DominatorTree;
import recaf.utils.DoublyLinkedList;
import recaf.utils.HashLinkedList;

import java.util.List;
import java.util.Set;

public class ASMBasicBlock implements DominatorTree.Node<ASMBasicBlock> {

    private ASMContext ctx;
    private ASMLabel label;
    private int id;
    private Set<ASMLabel> successors;

    private DoublyLinkedList<ASMInstruction> instructions;

    public ASMBasicBlock(ASMContext ctx, ASMLabel label, Set<ASMLabel> successors) {
        this.ctx = ctx;
        id = ctx.getInstructionCounter();
        this.label = label;
        instructions = new HashLinkedList<>();
        this.successors = successors;
        ctx.addBlock(label, this);
    }

    public void offer(ASMInstruction instruction) {
        instructions.offerLast(instruction);
    }

    public DoublyLinkedList<ASMInstruction> getInstructions() {
        return instructions;
    }

    @Override
    public List<ASMBasicBlock> successors() {
        return successors.stream().map(ctx::getBlock).toList();
    }

    public int hashCode() {
        return id;
    }

    public boolean equals(Object obj) {
        return obj instanceof ASMBasicBlock && ((ASMBasicBlock)obj).id == id;
    }

    public ASMLabel getLabel() {
        return label;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(new ASMLabelInstruction(label).toString());
        for (ASMStatement instruction : instructions) {
            sb.append(instruction.toString());
        }
        return sb.toString();
    }

}
