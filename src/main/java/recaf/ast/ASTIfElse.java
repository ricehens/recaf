package recaf.ast;

import recaf.parse.ASTUtils;

import java.util.Optional;

public record ASTIfElse(
        ASTContext ctx, ASTExpression cond, ASTStatement thenBlock, Optional<ASTStatement> elseBlock
) implements ASTStatement {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(IfElse)", cond, thenBlock, elseBlock.orElse(null));
    }

}
