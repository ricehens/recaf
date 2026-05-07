package recaf.ast.nodes;

import recaf.ast.ASTUtils;

public record ASTFieldAccess(ASTContext ctx, ASTIdentifier field) implements ASTAccessor {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(FieldAccess) " + field.text());
    }

}
