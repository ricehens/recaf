package recaf.ast;

import recaf.parse.ParseUtils;

import java.util.List;

public record ASTIndexAccess(ASTContext ctx, List<ASTExpression> indices) implements ASTAccessor {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(IndexAccess)", indices);
    }

}
