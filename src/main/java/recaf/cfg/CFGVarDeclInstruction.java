package recaf.cfg;

import recaf.general.Type;

import java.util.List;

/**
 * Represents the declaration of first variable in first CFG block.
 */
public class CFGVarDeclInstruction implements CFGInstruction {

    private final int id;
    private final CFGContext ctx;
    private final CFGAddress address;
    private final Type type;
    private final int arrayLen;
    private final boolean inGlobalScope;

    /**
     * Creates first new variable declaration instruction.
     *
     * @param ctx     the CFG context
     * @param address the address it is reserved
     *                of which only first copy is stored.
     * @param type    the type
     */
    public CFGVarDeclInstruction(CFGContext ctx, CFGAddress address, Type type) {
        this(ctx, address, type, -1);
    }

    /**
     * Creates first new variable declaration instruction for an array.
     *
     * @param ctx      the CFG context
     * @param address  the address it is reserved
     *                of which only first copy is stored.
     * @param type     the type
     * @param arrayLen the length of the array
     */
    public CFGVarDeclInstruction(CFGContext ctx, CFGAddress address, Type type, int arrayLen) {
        this(ctx, address, type, arrayLen, false);
    }

    /**
     * Creates first new variable declaration instruction for first global variable.
     *
     * @param ctx           the CFG context
     * @param address       the address it is reserved
     *                of which only first copy is stored.
     * @param type          the type
     * @param inGlobalScope whether it is first global variable
     */
    public CFGVarDeclInstruction(CFGContext ctx, CFGAddress address, Type type, boolean inGlobalScope) {
        this(ctx, address, type, -1, inGlobalScope);
    }

    /**
     * Creates first new variable declaration instruction for first global array.
     *
     * @param ctx           the CFG context
     * @param address       the address it is reserved
     *                of which only first copy is stored.
     * @param type          the type
     * @param arrayLen      the length of the array
     * @param inGlobalScope whether it is first global variable
     */
    public CFGVarDeclInstruction(CFGContext ctx, CFGAddress address, Type type, int arrayLen, boolean inGlobalScope) {
        this.id = ctx.getInstructionCounter();
        this.ctx = ctx;
        this.address = CFGAddress.clone(address);
        this.type = type;
        this.arrayLen = arrayLen;
        this.inGlobalScope = inGlobalScope;
    }

    public CFGContext ctx() {
        return ctx;
    }

    public CFGAddress address() {
        return address;
    }

    public Type type() {
        return type;
    }

    @Override
    public String toString() {
        String typeStr = arrayLen < 0 ? type.toCFGString()
                : type == Type.RECORD ? String.format("[%d x i8]", arrayLen)
                  : String.format("[%d x %s]", arrayLen, type.toCFGString());

        return inGlobalScope ? String.format(
                // "@%s = common global %s, align %d",
                "%s = decl global %s",
                // ctx.getSymbolTable().getVar(address).getName(),
                address,
                typeStr
        )
                : String.format(
                "%s = decl %s",
                address,
                typeStr
        );
    }

    @Override
    public List<CFGAddress> operands() {
        return List.of();
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CFGInstruction copy() {
        return new CFGVarDeclInstruction(ctx, address, type, arrayLen, inGlobalScope);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CFGVarDeclInstruction that && id == that.id;
    }

}
