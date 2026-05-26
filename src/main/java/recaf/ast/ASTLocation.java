package recaf.ast;

import recaf.parse.ParseUtils;

import java.util.List;

public record ASTLocation(
        ASTContext ctx, ASTIdentifier id, List<ASTAccessor> accesses
) implements ASTExpression {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(Location) " + id.text(), accesses);
    }

}
