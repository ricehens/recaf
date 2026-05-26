package recaf.ast;

import recaf.parse.ParseUtils;
import recaf.general.BinaryOperator;

public record ASTBinaryExpression(
        ASTContext ctx, BinaryOperator op, ASTExpression left, ASTExpression right
) implements ASTExpression {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(BinExpr)", op, left, right);
    }

}
