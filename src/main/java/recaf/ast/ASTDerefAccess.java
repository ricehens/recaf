package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTDerefAccess(ASTContext ctx) implements ASTAccessor {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(DerefAccess)");
    }

}
