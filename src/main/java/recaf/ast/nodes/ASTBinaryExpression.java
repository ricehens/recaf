package recaf.ast.nodes;

import recaf.ast.ASTUtils;
import recaf.general.BinaryOperator;

public record ASTBinaryExpression(
        ASTContext ctx, BinaryOperator op, ASTExpression left, ASTExpression right
) implements ASTExpression {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(BinExpr)", op, left, right);
    }

}
