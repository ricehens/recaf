package recaf.ast.nodes;

import recaf.ast.ASTUtils;
import recaf.general.UnaryOperator;

public record ASTUnaryExpression(ASTContext ctx, UnaryOperator op, ASTExpression expr) implements ASTExpression {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(UnExpr)", op, expr);
    }

}
