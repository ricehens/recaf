package recaf.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import recaf.parse.SemanticChecker;

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
     * Logs a semantic error with the invariant error handler.
     *
     * @param message the error message (no need for line/column numbers)
     */
    public void error(String message) {
        inv.getErrorHandler().error(inv.getFile(), line, charPositionInLine, message);
    }

    public SemanticChecker getSemanticChecker() {
        return inv.getSemanticChecker();
    }

    public SemanticChecker setSemanticChecker(SemanticChecker sc) {
        this.inv.setSemanticChecker(sc);
        return sc;
    }

}
