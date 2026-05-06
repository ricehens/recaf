package recaf.ast.nodes;

import recaf.ast.ASTUtils;

public record ASTLocationFieldAccess(ASTContext ctx, ASTIdentifier field) implements ASTLocationAccess {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(FieldAccess) " + field.text());
    }

}
