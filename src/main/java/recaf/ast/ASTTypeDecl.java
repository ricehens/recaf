package recaf.ast;

import recaf.parse.ASTUtils;

public record ASTTypeDecl(
        ASTContext ctx,
        ASTIdentifier id,
        ASTType type
) implements ASTDeclaration {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(TypeDecl) " + id.text(),
                type);
    }

}
