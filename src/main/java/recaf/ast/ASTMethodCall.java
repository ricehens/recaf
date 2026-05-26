package recaf.ast;

import recaf.parse.ASTUtils;

import java.util.List;

public record ASTMethodCall(
        ASTContext ctx, ASTIdentifier id, List<ASTExpression> args
) implements ASTExpression, ASTStatement {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(MethodCall) " + id.text(), args);
    }

}
