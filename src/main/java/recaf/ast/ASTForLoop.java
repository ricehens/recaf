package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTForLoop(
        ASTContext ctx,
        ASTLocation dummy,
        ASTExpression start,
        ASTExpression end,
        boolean descending,
        ASTStatement body
) implements ASTStatement {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(ForLoop)",
                dummy, start, descending ? "(Direction) downto" : "(Direction) to", end, body);
    }

}
