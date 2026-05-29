package recaf.asm;

import recaf.common.Type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents an instruction within the main body of an x86_64 assembly program.
 */
public class ASMInstruction implements ASMStatement {

    private final ASMContext ctx;
    private final int id;
    private final ASMOperator op;
    private final ASMLocation src;
    private final ASMLocation dest;
    private final ASMInstructionContext specificCtx;

    /**
     * Constructs an assembly instruction with two arguments.
     *
     * @param op   the operator
     * @param src  the source literal, register, or address
     * @param dest the destination register or address
     * @param specificCtx any specific context to provide
     */
    public ASMInstruction(ASMContext ctx, ASMOperator op, ASMLocation src, ASMLocation dest, ASMInstructionContext specificCtx) {
        this.ctx = ctx;
        id = ctx.getInstructionCounter();
        this.op = op;
        this.src = src;
        this.dest = dest;
        this.specificCtx = specificCtx;

        // Check instruction params
        if (((src == null) ^ (op.src == null))
                || ((dest == null) ^ (op.dest == null))) {
            throw new AssertionError("Operator" + op + " argument number mismatch: " + src + ", " + dest);
        }
        if (((src instanceof ASMRegister) && ((ASMRegister) src).type != op.src)
                || ((dest instanceof ASMRegister) && ((ASMRegister) dest).type != op.dest)) {
            throw new AssertionError("Operator " + op + " type mismatch: " + src + ", " + dest);

        }
    }

    /**
     * Constructs an assembly instruction with two arguments.
     *
     * @param op   the operator
     * @param src  the source literal, register, or address
     * @param dest the destination register or address
     */
    public ASMInstruction(ASMContext ctx, ASMOperator op, ASMLocation src, ASMLocation dest) {
        this(ctx, op, src, dest, new ASMInstructionContext());
    }

    /**
     * Constructs an assembly instruction with only one argument.
     *
     * @param op   the operator
     * @param dest the destination register or address
     */
    public ASMInstruction(ASMContext ctx, ASMOperator op, ASMLocation dest, ASMInstructionContext specificCtx) {
        this(ctx, op, null, dest, specificCtx);
    }

    /**
     * Constructs an assembly instruction with only one argument.
     *
     * @param op   the operator
     * @param dest the destination register or address
     */
    public ASMInstruction(ASMContext ctx, ASMOperator op, ASMLocation dest) {
        this(ctx, op, null, dest);
    }

    /**
     * Constructs an assembly instruction with no arguments.
     *
     * @param op the operator
     */
    public ASMInstruction(ASMContext ctx, ASMOperator op) {
        this(ctx, op, null);
    }

    public Set<ASMAbstractRegister> operandRegisters() {
        Set<ASMAbstractRegister> result = new HashSet<>(op.extraOperands);

        // special case for call
        if (op == ASMOperator.CALL) {
            int args = 6;
            if (specificCtx != null)
                args = Math.min(6, specificCtx.numParams);
            result.addAll(List.of(ASMRegister.RDI, ASMRegister.RSI, ASMRegister.RDX, ASMRegister.RCX, ASMRegister.R8, ASMRegister.R9)
                    .subList(0, args));
        }

        // special case for xor -> 0
        if (op == ASMOperator.XORB || op == ASMOperator.XORL || op == ASMOperator.XORQ)
            if (src.equals(dest))
                return Set.of();

        if (src instanceof ASMAbstractRegister ar)
            result.add(ar);
        if (dest instanceof ASMAbstractRegister ar && op.useDest)
            result.add(ar);

        return result.stream()
                .map(r -> r instanceof ASMRegister pr ? pr.toType(Type.LONG) : r)
                .collect(Collectors.toSet());
    }

    public Set<ASMAbstractRegister> destinationRegisters() {
        Set<ASMAbstractRegister> result = new HashSet<>(op.extraDestinations);

        // special case for call
        if (op == ASMOperator.CALL) {
            int args = 6;
            if (specificCtx != null)
                args = Math.min(6, specificCtx.numParams);
            result.addAll(List.of(ASMRegister.RDI, ASMRegister.RSI, ASMRegister.RDX, ASMRegister.RCX, ASMRegister.R8, ASMRegister.R9)
                    .subList(0, args));
        }

        if (dest instanceof ASMAbstractRegister ar)
            result.add(ar);
        return result.stream()
                .map(r -> r instanceof ASMRegister pr ? pr.toType(Type.LONG) : r)
                .collect(Collectors.toSet());
    }

    public ASMContext ctx() {
        return ctx;
    }

    public ASMInstructionContext specificCtx() {
        return specificCtx;
    }

    public ASMOperator op() {
        return op;
    }

    public ASMLocation src() {
        return src;
    }

    public ASMLocation dest() {
        return dest;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ASMInstruction && ((ASMInstruction)obj).id == id;
    }

    @Override
    public String toString() {
        return String.format("%s%s%s%s%n",
                ASMUtils.pad(" "),
                ASMUtils.pad(op.toString()),
                src == null ? "" : (src + ", "),
                dest == null ? "" : dest.toString());
    }

}
