package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTVarDecl(
        ASTContext ctx,
        ASTType type,
        ASTIdentifier id
) implements ASTDeclaration {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(VarDecl) " + id.text(), type);
    }

}
