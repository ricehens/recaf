package recaf.opt;

import recaf.cfg.CFGContext;
import recaf.cfg.CFGMethod;
import recaf.cfg.CFGProgram;

/**
 * A controller that controls the execution of method transformation on all methods of a CFG program
 */
public class MethodTransformer {

    private CFGProgram cfg;
    private CFGContext ctx;

    /**
     * Creates a new method transformer
     *
     * @param cfg the CFG program
     */
    public MethodTransformer(CFGProgram cfg) {
        this.cfg = cfg;
        ctx = cfg.ctx();
    }

    /**
     * An enumeration of possible method transformation types
     */
    public enum TransformType {
        TO_SSA, // CFG -> SSA
        DE_SSA, // SSA -> CFG
        CP, // global copy propagation
        DCE, // global dead code elimination
        CRITICAL_EDGE, // critical edge splitting
        GVNPRE, // global value numbering partial redundancy elimination
        SSCP, // sparse simple constant propagation
        CLEAN, // clean useless control flow
        OSR, // operator strength reduction
        OSR_LFTR, // OSR + linear function test replacement
        UNROLL, // loop unrolling
        ALG_SIMP, // algebraic simplification
        TAILCALL, // self-recursive tail call elimination
        LOAD_ELIM, // local load elimination
    }

    /**
     * Transforms a CFG program using a specified transformation on all methods
     *
     * @param transform the transformation
     * @return whether any changes were made
     */
    public boolean apply(TransformType transform) {
        boolean changed = false;

        for (CFGMethod method : cfg.getMethods()) {
            MethodTransformation transformation = switch (transform) {
                case TO_SSA -> new CFGtoSSA(ctx, method);
                case DE_SSA -> new ClassicPhiElimination(ctx, method);
                case CP -> new CopyPropagator(ctx, method);
                case DCE -> new DeadCodeElimination(ctx, method);
                case CRITICAL_EDGE -> new CriticalEdgeSplitting(ctx, method);
                case GVNPRE -> new GVNPRE(ctx, method);
                case SSCP -> new SSCP(ctx, method);
                case CLEAN -> new DeadBlockElimination(ctx, method);
                case OSR -> new OperatorStrengthReduction(ctx, method, false);
                case OSR_LFTR -> new OperatorStrengthReduction(ctx, method, true);
                case UNROLL -> new LoopUnrolling(ctx, method);
                case ALG_SIMP -> new AlgebraicSimplification(ctx, method);
                case TAILCALL -> new RecursiveTailCallElimination(ctx, method);
                case LOAD_ELIM -> new LocalLoadElimination(ctx, method);
            };

            changed |= transformation.apply();
        }

        return changed;
    }

}
