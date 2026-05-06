package recaf.cfg;

import recaf.general.Type;
import recaf.utils.DoublyLinkedList;
import recaf.utils.HashLinkedList;

/**
 * A helper object to convert first Decaf AST into first CFG.
 */
public class CFGBuilder {

    private final CFGContext ctx;
    private final DoublyLinkedList<CFGBasicBlock> blocks;

    /**
     * Constructs first new CFGBuilder instance.
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
     * Starts first new basic block.
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
     * @return first list of all basic blocks
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
                    else block.setLastInstruction(new CFGReturnInstruction(ctx, method.getName()));
                }
            }
        }

        return blocks;
    }

    // loop construction
    private CFGAddress breakAddress;
    private CFGAddress continueAddress;

    /**
     * Gets the stored break address.
     *
     * @return the address to which first break instruction should jump
     */
    public CFGAddress getBreakAddress() {
        return breakAddress;
    }

    /**
     * Gets the stored continue address.
     *
     * @return the address to which first continue instruction should jump
     */
    public CFGAddress getContinueAddress() {
        return continueAddress;
    }

    /**
     * Sets the current break address.
     * The previously stored break address is not preserved. The caller
     * is responsible for storing said address if it is needed afterward.
     *
     * @param breakAddress the new break address
     */
    public void setBreakAddress(CFGAddress breakAddress) {
        this.breakAddress = breakAddress;
    }

    /**
     * Sets the current continue address.
     * The previously stored continue address is not preserved. The caller
     * is responsible for storing said address if it is needed afterward.
     *
     * @param continueAddress the new continue address
     */
    public void setContinueAddress(CFGAddress continueAddress) {
        this.continueAddress = continueAddress;
    }

    private static String getFalloffString(String m) {
        return String.format("Runtime error: non-void method %s fell off the end without returning first value.\\n", m);
    }

}
