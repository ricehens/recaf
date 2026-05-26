package recaf.ast;

import recaf.parse.ASTUtils;

public record ASTUsesDecl(ASTContext ctx, ASTIdentifier id) implements ASTDeclaration {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(UsesDecl) ", id.text());
    }

}
