package recaf.ast.nodes;

import recaf.ast.ASTUtils;

public record ASTLocationDerefAccess(ASTContext ctx) implements ASTLocationAccess {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(DerefAccess)");
    }

}
