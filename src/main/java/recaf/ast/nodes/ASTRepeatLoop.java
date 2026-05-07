package recaf.ast.nodes;

import recaf.ast.ASTUtils;

public record ASTRepeatLoop(
        ASTContext ctx,
        ASTStatement body,
        ASTExpression cond
) implements ASTStatement {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(RepeatLoop)", body, cond);
    }

}
