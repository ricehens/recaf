package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTParameter(ASTContext ctx, ASTVarDecl vd, boolean byReference) implements AST {

    @Override
    public String toString() {
        return ParseUtils.generateToString(byReference ? "(&Parameter)" : "(Parameter)", vd);
    }

}
