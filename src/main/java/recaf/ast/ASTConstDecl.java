package recaf.ast;

import recaf.parse.ASTUtils;

public record ASTConstDecl(ASTContext ctx, ASTIdentifier id, ASTExpression expr)
        implements ASTDeclaration {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(ConstDecl) " + id.text(), expr);
    }

}
