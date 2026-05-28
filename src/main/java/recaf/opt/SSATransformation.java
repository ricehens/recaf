package recaf.opt;

import recaf.cfg.CFGContext;
import recaf.cfg.CFGMethod;

/**
 * An abstract class to represent a transformation on a method of a SSA CFG
 */
public abstract class SSATransformation extends MethodTransformation {

    protected SSAData data;

    protected SSATransformation(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
        data = new SSAData(method);
    }

    /**
     * Transforms a method of a SSA CFG
     *
     * @return whether any changes were made
     */
    public abstract boolean apply();

}
