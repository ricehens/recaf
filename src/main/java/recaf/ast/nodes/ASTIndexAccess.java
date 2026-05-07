package recaf.ast.nodes;

import recaf.ast.ASTUtils;

import java.util.List;

public record ASTIndexAccess(ASTContext ctx, List<ASTExpression> indices) implements ASTAccessor {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(IndexAccess)", indices);
    }

}
