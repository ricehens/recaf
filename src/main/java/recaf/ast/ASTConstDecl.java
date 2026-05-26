package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTConstDecl(ASTContext ctx, ASTIdentifier id, ASTExpression expr)
        implements ASTDeclaration {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(ConstDecl) " + id.text(), expr);
    }

}
