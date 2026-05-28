package recaf.cfg;

import recaf.utils.DominatorTree;
import recaf.utils.DoublyLinkedList;
import recaf.utils.HashLinkedList;

import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a basic block within the control flow graph.
 */
public class CFGBasicBlock implements CFG, DominatorTree.Node<CFGBasicBlock> {

    private final CFGContext ctx;
    private CFGMethod method;
    private final DoublyLinkedList<CFGPhiInstruction> phiInstructions;
    private final DoublyLinkedList<CFGInstruction> instructions;
    private CFGLastInstruction lastInstruction;
    private final CFGAddress address;

    /**
     * Constructs a new basic block.
     *
     * @param ctx the CFG context
     */
    public CFGBasicBlock(CFGContext ctx) {
        this.ctx = ctx;
        phiInstructions = new HashLinkedList<>();
        instructions = new HashLinkedList<>();
        address = ctx.getSymbolTable().addBlock(this);
    }

    /**
     * Appends a new instruction to the basic block.
     *
     * @param instruction the instruction to add
     */
    public void offer(CFGInstruction instruction) {
        instructions.offerLast(instruction);
    }

    /**
     * Returns the address of the basic block
     *
     * @return the address
     */
    public CFGAddress address() {
        return address;
    }

    /**
     * Returns all instructions in the basic block
     *
     * @return a doubly-linked list of instructions
     */
    public DoublyLinkedList<CFGInstruction> getInstructions() {
        return instructions;
    }

    /**
     * Return all phi instructions in the basic block
     *
     * @return a doubly-linked list of phi instructions
     */
    public DoublyLinkedList<CFGPhiInstruction> getPhiInstructions() {
        return phiInstructions;
    }

    /**
     * Returns the jump or branch instruction at the end of the basic block
     *
     * @return the last instruction
     */
    public CFGLastInstruction getLastInstruction() {
        return lastInstruction;
    }

    /**
     * Sets the last instruction of the basic block to the given instruction
     *
     * @param lastInstruction the new last instruction
     */
    public void setLastInstruction(CFGLastInstruction lastInstruction) {
        this.lastInstruction = lastInstruction;
    }

    /**
     * Returns a list of all phi instructions and normal instructions in the basic block
     *
     * @return a list of instructions
     */
    public List<CFGInstruction> getAllInstructions() {
        Stream<CFGInstruction> most = Stream.concat(phiInstructions.stream(), instructions.stream());
        return lastInstruction == null ? most.toList() : Stream.concat(most, Stream.of(lastInstruction)).toList();
    }

    /**
     * Returns the current method this basic block belongs to
     *
     * @return the method this basic block belongs to
     */
    public CFGMethod getMethod() {
        return method;
    }

    /**
     * Sets the current method this basic block belongs to
     *
     * @param method the method this basic block belongs to
     */
    public void setMethod(CFGMethod method) {
        this.method = method;
    }

    /**
     * Returns all blocks that can be directly reached from the current block.
     *
     * @return a list of successors
     */
    @Override
    public List<CFGBasicBlock> successors() {
        if (lastInstruction != null) {
            if (lastInstruction instanceof CFGJumpInstruction jump)
                return List.of(ctx.getSymbolTable().getBlock(jump.jumpAddr()));

            if (lastInstruction instanceof CFGBranchInstruction branch)
                return List.of(ctx.getSymbolTable().getBlock(branch.thenAddr()),
                        ctx.getSymbolTable().getBlock(branch.elseAddr()));

            if (lastInstruction instanceof CFGReturnInstruction)
                return List.of();

            if (lastInstruction instanceof CFGExceptionInstruction)
                return List.of();
        }
/*
        CFGBasicBlock next = method.getBlocks().next(this);
        return next == null ? List.of() : List.of(next);

 */
        throw new AssertionError("Requesting successors of block " + address + " with no last instruction\n" + method);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(address.toString().substring(1)).append(":").append(System.lineSeparator());
        for (CFGInstruction instruction : phiInstructions) {
            sb.append("  ").append(instruction.toString()).append(System.lineSeparator());
        }
        for (CFGInstruction instruction : instructions) {
            sb.append("  ").append(instruction.toString()).append(System.lineSeparator());
        }
        if (lastInstruction != null)
            sb.append("  ").append(lastInstruction.toString()).append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CFGBasicBlock other))
            return false;
        return address.equals(other.address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

}
