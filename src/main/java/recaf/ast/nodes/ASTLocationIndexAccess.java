package recaf.ast.nodes;

import recaf.ast.ASTUtils;

import java.util.List;

public record ASTLocationIndexAccess(ASTContext ctx, List<ASTExpression> indices) implements ASTLocationAccess {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(IndexAccess)", indices);
    }

}
