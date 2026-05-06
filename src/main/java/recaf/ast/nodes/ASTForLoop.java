package recaf.ast.nodes;

import recaf.ast.ASTUtils;

public record ASTForLoop(
        ASTContext ctx,
        ASTIdentifier iterator,
        ASTExpression start,
        ASTExpression end,
        boolean descending,
        ASTStatement body
) implements ASTStatement {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(ForLoop) " + iterator.text(),
                start, descending ? "(Direction) downto" : "(Direction) to", end, body);
    }

}
