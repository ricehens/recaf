package recaf.ast;

import recaf.parse.ASTUtils;

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
