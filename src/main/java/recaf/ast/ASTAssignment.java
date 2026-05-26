package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTAssignment(
        ASTContext ctx,
        ASTLocation location,
        ASTExpression expr
) implements ASTStatement {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(Assignment)", location, expr);
    }

}
