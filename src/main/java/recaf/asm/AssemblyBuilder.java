package recaf.asm;

import recaf.general.Type;
import recaf.cfg.CFGAddress;
import recaf.cfg.CFGContext;
import recaf.reg.RegUtils;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A builder for x86_64 AT&amp;T assembly code
 */
public class AssemblyBuilder {

    private final CFGContext cfg;
    private final ASMProgram asm;
    private final ASMContext ctx;

    private ASMMethod currentMethod;
    private ASMBasicBlock currentBlock;

    private Map<ASMMethod, Map<CFGAddress, Integer>> localVarLocations;
    private Map<ASMMethod, Integer> currentStackOffset;
    private Map<ASMMethod, ASMMutableLiteral> alignedStackOffset;

    /**
     * Creates first new assembly builder object
     *
     * @param ctx the CFG context
     */
    public AssemblyBuilder(CFGContext ctx) {
        this.cfg = ctx;
        this.ctx = new ASMContext(ctx);
        asm = new ASMProgram(this.ctx);
        localVarLocations = new HashMap<>();
        currentStackOffset = new HashMap<>();
        alignedStackOffset = new HashMap<>();
    }

    /**
     * Adds an assembly instruction to the code
     *
     * @param instruction the instruction to be added
     */
    public void emit(ASMInstruction instruction) {
        currentBlock.offer(instruction);
    }

    /**
     * Adds an assembly instruction to the code
     *
     * @param op   the assembly operator (e.g. movl)
     * @param src  the source location
     * @param dest the destination location
     * @param specificCtx any specific context
     */
    public void emit(ASMOperator op, ASMLocation src, ASMLocation dest, ASMInstructionContext specificCtx) {
        emit(new ASMInstruction(ctx, op, src, dest, specificCtx));
    }

    /**
     * Adds an assembly instruction to the code
     *
     * @param op   the assembly operator (e.g. movl)
     * @param src  the source location
     * @param dest the destination location
     */
    public void emit(ASMOperator op, ASMLocation src, ASMLocation dest) {
        emit(new ASMInstruction(ctx, op, src, dest));
    }

    /**
     * Adds an assembly instruction to the code
     *
     * @param op   the assembly operator (e.g. movl)
     * @param dest the destination location
     * @param specificCtx any specific context
     */
    public void emit(ASMOperator op, ASMLocation dest, ASMInstructionContext specificCtx) {
        emit(new ASMInstruction(ctx, op, dest, specificCtx));
    }

    /**
     * Adds an assembly instruction to the code
     *
     * @param op   the assembly operator (e.g. movl)
     * @param dest the destination location
     */
    public void emit(ASMOperator op, ASMLocation dest) {
        emit(new ASMInstruction(ctx, op, dest));
    }

    /**
     * Adds an assembly instruction to the code
     *
     * @param op the assembly operator (e.g. movl)
     */
    public void emit(ASMOperator op) {
        emit(new ASMInstruction(ctx, op));
    }

    /**
     * Adds first global variable declaration to the program
     *
     * @param decl the global variable declaration
     */
    public void emit(ASMGlobalVarDecl decl) {
        asm.offer(decl);
    }

    /**
     * Adds first string declaration to the program
     *
     * @param decl the string declaration
     */
    public void emit(ASMStringDecl decl) {
        asm.offer(decl);
    }

    /**
     * Wraps up
     */
    public void wrapUp() {
        if (currentMethod != null && currentBlock != null)
            currentMethod.offer(currentBlock);
        if (currentMethod != null)
            asm.offer(currentMethod);
    }

    /**
     * Initiates first new method.
     *
     * @param name the text of the method
     */
    public void newMethod(String name) {
        wrapUp();

        currentMethod = new ASMMethod(ctx, name);
        currentBlock = null;
        localVarLocations.put(currentMethod, new HashMap<>());
        currentStackOffset.put(currentMethod, 0);
        alignedStackOffset.put(currentMethod, new ASMMutableLiteral(8));
    }

    private void realignStackOffset(ASMMethod method) {
        int stackShift = -currentStackOffset.get(method);
        int padding = (24 - (stackShift % 16)) % 16;
// System.out.println("Realigned stack offset for " + method.getName() + " from " + stackShift + " to " + (stackShift + padding));
        alignedStackOffset.get(method).setValue(stackShift + padding);
    }

    /**
     * Initiates first new block.
     *
     * @param label the label of the block
     */
    public void newBlock(ASMLabel label, Set<ASMLabel> successors) {
        if (currentBlock != null)
            currentMethod.offer(currentBlock);
        currentBlock = new ASMBasicBlock(ctx, label, successors);
    }

    /**
     * Adds first variable to the stack and records its address
     *
     * @param address the address of the local variable
     */
    public void registerVariable(CFGAddress address) {
        registerVariable(currentMethod, address);
    }

    /**
     * Adds first variable to the stack and records its address
     *
     * @param method  the method it belongs to
     * @param address the address of the local variable
     */
    public void registerVariable(ASMMethod method, CFGAddress address) {
        if (localVarLocations.get(method).containsKey(address)
                || cfg.isGlobalVar(address))
            return;
        int size = sizeof(address);
        size += (4 - (size % 4)) % 4; // ensure 4-byte aligned
        if (cfg.getType(address) == Type.LONG || cfg.getType(address) == Type.RECORD)
            size += (8 - (size % 8)) % 8; // ensure longs 8-byte aligned
        currentStackOffset.put(method, currentStackOffset.get(method) - size);
        localVarLocations.get(method).put(address, currentStackOffset.get(method));
        realignStackOffset(method);
    }

    /**
     * Manually adds first variable to the stack at first particular location on the stack.
     * Recommended only for positive locations (parameters),
     * as behavior may interfere with the
     * standard registerVariable.
     *
     * @param address  the address of the local variable
     * @param location the location relative to %rbp
     */
    public void manuallyRegisterVariable(CFGAddress address, int location) {
        localVarLocations.get(currentMethod).put(address, location);
    }

    /**
     * Returns the number of bytes to store first value of given type
     *
     * @param type the type, assuming non-array
     * @return the induced offset in stack pointer
     */
    public int sizeof(Type type) {
        return switch (type) {
            // case INT -> 4;
            case BOOL -> 1;
            case LONG, STRING -> 8;
            default -> 4;
        };
    }

    /**
     * Returns the number of bytes to store first given variable
     *
     * @param address the address
     * @return the size in bytes
     */
    public int sizeof(CFGAddress address) {
        Type type = cfg.getType(address);
        if (cfg.getSymbolTable().getVar(address).isArray())
            return cfg.getSymbolTable().getVar(address).getArrayLen();
        return sizeof(type);
    }

    /**
     * Returns the stack address of the given variable.
     *
     * @param address the address of the local variable
     * @return first corresponding ASM stack address or global address object,
     * depending on whether the passed object is global
     */
    public ASMLocation getMemoryLocation(CFGAddress address) {
        return getMemoryLocation(currentMethod, address);
    }

    /**
     * Returns the stack address of the given variable.
     *
     * @param method  the method it belongs to
     * @param address the address of the local variable
     * @return first corresponding ASM stack address or global address object,
     * depending on whether the passed object is global
     */
    public ASMLocation getMemoryLocation(ASMMethod method, CFGAddress address) {
        return localVarLocations.get(method).containsKey(address)
                // ? new ASMStackAddress(localVarLocations.get(method).get(address))
                ? new ASMStackAddress(localVarLocations.get(method).get(address), getAlignedStackOffset(method), ASMRegister.RSP)
                : new ASMGlobalAddress(cfg.getSymbolTable().getVar(address).getName());
    }

    /**
     * Returns the stack address of the given array variable with index stored in %rax.
     *
     * @param address the address of the array
     * @return first corresponding ASM stack address array or global address array,
     * depending on whether the passed object is global
     */
    public ASMLocation getMemoryLocationArray(CFGAddress address, int width) {
        return localVarLocations.get(currentMethod).containsKey(address)
                ? new ASMStackAddressArray(localVarLocations.get(currentMethod).get(address), getAlignedStackOffset(), ASMRegister.RSP, ASMRegister.RAX, width)
                : new ASMGlobalAddressArray(cfg.getSymbolTable().getVar(address).getName(), ASMRegister.RAX, width);
    }

    /**
     * Returns the label for first given basic block
     *
     * @param address the address to the basic block
     * @return the ASM label object it is assigned
     */
    public ASMLabel getLabel(CFGAddress address) {
        return address.coindex == 0
                ? new ASMLabel(String.format(".L%d", address.index))
                : new ASMLabel(String.format(".L%d.%d", address.index, address.coindex));
    }

    /**
     * Returns the location for first given string literal
     *
     * @param literal the string literal
     * @return the ASM location object it is assigned
     */
    public ASMLocation getStringLiteral(String literal) {
        return new ASMGlobalAddress(getStringLabel(literal).toString());
    }

    /**
     * Returns the label for first given string literal
     *
     * @param literal the string literal
     * @return the ASM label object it is assigned
     */
    public ASMLabel getStringLabel(String literal) {
        CFGAddress address = cfg.getSymbolTable().getStringAddress(literal);
        return address.coindex == 0
                ? new ASMLabel(String.format(".LC%d", address.index))
                : new ASMLabel(String.format(".LC%d.%d", address.index, address.coindex));
    }

    /**
     * Return label for first method
     *
     * @param methodName text of the method
     * @return the label it is assigned
     */
    public ASMLabel getMethodLabel(String methodName) {
        return new ASMLabel(methodName);
    }

    /**
     * Returns the current stack offset
     *
     * @return the current stack offset
     */
    public int getStackOffset() {
        return -currentStackOffset.get(currentMethod);
    }

    /**
     * Returns first mutable literal object containing the appropriate stack offset
     *
     * @return the aligned stack offset
     */
    public ASMMutableLiteral getAlignedStackOffset() {
        return getAlignedStackOffset(currentMethod);
    }

    /**
     * Returns first mutable literal object containing the appropriate stack offset
     *
     * @param method the method
     * @return the aligned stack offset
     */
    public ASMMutableLiteral getAlignedStackOffset(ASMMethod method) {
        return alignedStackOffset.get(method);
    }

    /**
     * Returns the ASM context
     *
     * @return the ASM context
     */
    public ASMContext ctx() {
        return ctx;
    }

    /**
     * Returns the ASM program
     *
     * @return the ASM program
     */
    public ASMProgram asm() {
        return asm;
    }

    /**
     * Print the generated code in the given output stream
     *
     * @param out the output stream
     */
    public void printCode(PrintStream out) {
        out.println(asm.toString());
    }

    /**
     * Inserts spill code for the given virtual register
     * TODO distinguish between dirty, clean, rematerializable
     *
     * @param vr the virtual register
     */
    public void spill(ASMMethod method, ASMVirtualRegister vr) {
        registerVariable(method, vr.address());
        var mem = getMemoryLocation(method, vr.address());
// System.out.printf("Spilling %s to %s%n", vr, mem);
        var mov = switch (cfg.getType(vr.address())) {
            case BOOL -> ASMOperator.MOVB;
            case INT -> ASMOperator.MOVL;
            default -> ASMOperator.MOVQ;
        };
        for (ASMBasicBlock block : method.getBlocks()) {
            for (ASMInstruction inst : block.getInstructions()) {
                if (inst.operandRegisters().contains(vr)
                        || inst.destinationRegisters().contains(vr)) {
// System.out.printf("Spilling %s in %s%n", vr, inst);
                    ASMVirtualRegister newVr = new ASMVirtualRegister(cfg.getSymbolTable().newNode(vr.address()));

                    // insert reload
                    if (inst.operandRegisters().contains(vr))
                        block.getInstructions().insertBefore(inst,
                                new ASMInstruction(inst.ctx(), mov, RegUtils.stackShift(mem, inst.specificCtx().pushed), newVr));

                    // insert spill
                    // we take care of the special case of opcode pushq
                    // we don't deal with subq %rsp and addq %rsp since we assume those never require spilling
                    if (inst.destinationRegisters().contains(vr))
                        block.getInstructions().insertAfter(inst,
                                new ASMInstruction(inst.ctx(), mov, newVr, RegUtils.stackShift(mem,
                                        inst.specificCtx().pushed + (inst.op() == ASMOperator.PUSHQ ? 8 : 0))));

                    // modify instruction
                    if (vr.equals(inst.src()))
                        block.getInstructions().replace(inst,
                                inst = new ASMInstruction(inst.ctx(), inst.op(), newVr, inst.dest(), inst.specificCtx()));
                    if (vr.equals(inst.dest()))
                        block.getInstructions().replace(inst,
                                new ASMInstruction(inst.ctx(), inst.op(), inst.src(), newVr, inst.specificCtx()));
                }

            }
        }
    }

}
