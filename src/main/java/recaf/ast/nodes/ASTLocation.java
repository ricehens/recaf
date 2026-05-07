package recaf.ast.nodes;

import recaf.ast.ASTUtils;

import java.util.List;

public record ASTLocation(
        ASTContext ctx, ASTIdentifier id, List<ASTAccessor> accesses
) implements ASTExpression {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(Location) " + id.text(), accesses);
    }

}
