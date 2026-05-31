package recaf.ast;

import recaf.parse.ParseUtils;

import java.util.List;

public record ASTReference(
        ASTContext ctx, ASTLocation loc
) implements ASTExpression {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(&Location) " + loc.id().text(), loc.accesses());
    }

}
