package recaf.ast;

import recaf.parse.ASTUtils;

public record ASTVarDecl(
        ASTContext ctx,
        ASTType type,
        ASTIdentifier id
) implements ASTDeclaration {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(VarDecl) " + id.text(), type);
    }

}
