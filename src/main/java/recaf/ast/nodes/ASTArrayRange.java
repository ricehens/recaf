package recaf.ast.nodes;

import recaf.ast.ASTUtils;

public record ASTArrayRange(ASTContext ctx, ASTExpression lower, ASTExpression upper) implements AST {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(ArrayRange)", lower, upper);
    }

}
