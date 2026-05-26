package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTArrayRange(ASTContext ctx, ASTExpression lower, ASTExpression upper) implements AST {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(ArrayRange)", lower, upper);
    }

}
