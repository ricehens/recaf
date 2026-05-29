package recaf.ast;

import recaf.common.Literal;

public record ASTLiteral(ASTContext ctx, Literal literal) implements ASTExpression {

    @Override
    public String toString() {
        return "(Literal) " + literal.toString();
    }

}
