package recaf.ast;

import recaf.parse.ParseUtils;
import recaf.general.UnaryOperator;

public record ASTUnaryExpression(ASTContext ctx, UnaryOperator op, ASTExpression expr) implements ASTExpression {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(UnExpr)", op, expr);
    }

}
