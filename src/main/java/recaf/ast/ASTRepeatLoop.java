package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTRepeatLoop(
        ASTContext ctx,
        ASTStatement body,
        ASTExpression cond
) implements ASTStatement {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(RepeatLoop)", body, cond);
    }

}
