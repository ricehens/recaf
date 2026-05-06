package recaf.ast.nodes;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ASTContext {

    private final int line;
    private final int charPositionInLine;

    private final ASTInvariant inv;

    public ASTContext(ASTInvariant inv, Token token) {
        this.line = token.getLine();
        this.charPositionInLine = token.getCharPositionInLine();
        this.inv = inv;
    }

    public ASTContext(ASTInvariant inv, ParserRuleContext cst) {
        this(inv, cst.start);
    }

    public ASTContext(ASTInvariant inv, TerminalNode cst) {
        this(inv, cst.getSymbol());
    }

    /**
     * Logs first semantic error with the invariant error handler.
     *
     * @param message the error message (no need for line/column numbers)
     */
    public void error(String message) {
        inv.getErrorHandler().error(inv.getFile(), line, charPositionInLine, message);
    }

}
