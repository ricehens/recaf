package recaf.ast.nodes;

import recaf.ast.ASTUtils;

public record ASTDerefAccess(ASTContext ctx) implements ASTAccessor {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(DerefAccess)");
    }

}
