package recaf.ast;

import recaf.parse.ParseUtils;

import java.util.Optional;

public record ASTIfElse(
        ASTContext ctx, ASTExpression cond, ASTStatement thenBlock, Optional<ASTStatement> elseBlock
) implements ASTStatement {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(IfElse)", cond, thenBlock, elseBlock.orElse(null));
    }

}
