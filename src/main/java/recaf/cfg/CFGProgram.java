package recaf.cfg;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains an entire Decaf program's control flow graph.
 */
public class CFGProgram implements CFG {

    private CFGContext ctx;

    private final List<CFGMethod> methods;

    /**
     * Creates a new CFG program instance
     *
     * @param ctx the CFG context
     * @param methods the methods
     */
    public CFGProgram(CFGContext ctx, List<CFGMethod> methods) {
        this.ctx = ctx;
        this.methods = new ArrayList<>(methods);
        methods.forEach(m -> ctx.getSymbolTable().addMethod(m));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        methods.forEach(m -> sb.append(m.toString()).append(System.lineSeparator()));
        return sb.toString();
    }

    @Override
    public void accept(CFGVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Get the current CFG context.
     *
     * @return the CFG context object
     */
    public CFGContext ctx() {
        return ctx;
    }

    /**
     * Returns the methods in this program.
     *
     * @return a list of methods
     */
    public List<CFGMethod> getMethods() {
        return methods;
    }

}
