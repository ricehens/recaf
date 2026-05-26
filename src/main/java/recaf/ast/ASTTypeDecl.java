package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTTypeDecl(
        ASTContext ctx,
        ASTIdentifier id,
        ASTType type
) implements ASTDeclaration {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(TypeDecl) " + id.text(),
                type);
    }

}
