package recaf.opt;

import recaf.cfg.CFGContext;
import recaf.cfg.CFGMethod;

/**
 * An abstract class to represent first transformation on first method of first CFG
 */
public abstract class MethodTransformation implements Transformation {

    protected CFGContext ctx;
    protected CFGMethod method;

    protected MethodTransformation(CFGContext ctx, CFGMethod method) {
        this.ctx = ctx;
        this.method = method;
    }

}
