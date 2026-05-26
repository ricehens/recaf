package recaf.ast;

import recaf.parse.ASTUtils;

public record ASTAssignment(
        ASTContext ctx,
        ASTLocation location,
        ASTExpression expr
) implements ASTStatement {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(Assignment)", location, expr);
    }

}
