package recaf.cfg;

import recaf.common.Type;
import recaf.utils.DoublyLinkedList;
import recaf.utils.HashLinkedList;

/**
 * A helper object to convert a Decaf AST into a CFG.
 */
public class CFGBuilder {

    private final CFGContext ctx;
    private final DoublyLinkedList<CFGBasicBlock> blocks;

    /**
     * Constructs a new CFGBuilder instance.
     *
     * @param ctx the CFG context
     */
    public CFGBuilder(CFGContext ctx) {
        this.ctx = ctx;
        blocks = new HashLinkedList<>();
    }

    /**
     * Gives the current basic block.
     *
     * @return the current basic block
     */
    public CFGBasicBlock currentBlock() {
        return blocks.peekLast();
    }

    /**
     * Starts a new basic block.
     *
     * @return the new basic block
     */
    public CFGBasicBlock newBlock() {
        blocks.offerLast(new CFGBasicBlock(ctx));
        return currentBlock();
    }

    /**
     * Appends an instruction to the current basic block
     *
     * @param instruction the instruction object to add
     */
    public void offer(CFGInstruction instruction) {
        currentBlock().offer(instruction);
    }

    /**
     * Polishes all basic blocks, preparing them for use,
     * before returning them.
     *
     * @param method the method that owns the basic blocks
     * @return a list of all basic blocks
     */
    public DoublyLinkedList<CFGBasicBlock> getBlocks(CFGMethod method) {
        blocks.forEach(block -> block.setMethod(method));

        // set last instructions
        for (CFGBasicBlock block : blocks) {
            CFGInstruction lastInstruction = block.getInstructions().peekLast();
            if (lastInstruction instanceof CFGLastInstruction last) {
                block.getInstructions().pollLast();
                block.setLastInstruction(last);
            } else {
                CFGBasicBlock next = blocks.next(block);
                if (next != null)
                    block.setLastInstruction(new CFGJumpInstruction(ctx, next.address()));
                else {
                    if (method.getType() != Type.VOID)
                        block.setLastInstruction(new CFGExceptionInstruction(ctx, getFalloffString(method.getName())));
                    else block.setLastInstruction(new CFGReturnInstruction(ctx));
                }
            }
        }

        return blocks;
    }

    private static String getFalloffString(String m) {
        return String.format("Runtime error: non-void method %s fell off the end without returning a value.\\n", m);
    }

}
