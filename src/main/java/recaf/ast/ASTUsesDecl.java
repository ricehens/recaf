package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTUsesDecl(ASTContext ctx, ASTIdentifier id) implements ASTDeclaration {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(UsesDecl) ", id.text());
    }

}
