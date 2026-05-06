package recaf.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains an entire Decaf program's control flow graph.
 */
public class CFGProgram implements CFG {

    private CFGContext ctx;

    private final List<CFGVarDeclInstruction> globalVarDecl;
    private final List<CFGMethod> methods;

    /**
     * Creates a new CFG program instance
     *
     * @param ctx the CFG context
     * @param globalVarDecl the global variable declarations
     * @param externalMethods the external methods, as strings
     * @param methods the methods
     */
    public CFGProgram(CFGContext ctx, List<CFGVarDeclInstruction> globalVarDecl, List<String> externalMethods, List<CFGMethod> methods) {
        this.ctx = ctx;
        this.globalVarDecl = globalVarDecl;
        ctx.setGlobalVars(globalVarDecl.stream().map(CFGInstruction::address).collect(Collectors.toList()));
        externalMethods.forEach(ctx.getSymbolTable()::addExternalMethod);
        this.methods = new ArrayList<>(methods);
        methods.forEach(m -> ctx.getSymbolTable().addMethod(m));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        globalVarDecl.forEach(cfg -> sb.append(cfg.toString()).append(System.lineSeparator()));
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
     * @return first list of methods
     */
    public List<CFGMethod> getMethods() {
        return methods;
    }

}
