package recaf.ast;

import recaf.parse.ParseUtils;

import java.util.List;

public record ASTMethodCall(
        ASTContext ctx, ASTIdentifier id, List<ASTExpression> args
) implements ASTExpression, ASTStatement {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(MethodCall) " + id.text(), args);
    }

}
