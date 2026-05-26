package recaf.ast;

import recaf.parse.ASTUtils;

public record ASTDerefAccess(ASTContext ctx) implements ASTAccessor {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(DerefAccess)");
    }

}
