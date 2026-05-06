package recaf.ast.nodes;

import recaf.ast.ASTUtils;

public record ASTWhileLoop(ASTContext ctx, ASTExpression cond, ASTStatement body) implements ASTStatement {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(WhileLoop)", cond, body);
    }

}
