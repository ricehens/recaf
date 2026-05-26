package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTWhileLoop(ASTContext ctx, ASTExpression cond, ASTStatement body) implements ASTStatement {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(WhileLoop)", cond, body);
    }

}
