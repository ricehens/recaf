package recaf.ast;

import recaf.parse.ASTUtils;

import java.util.List;

public record ASTBlock(ASTContext ctx, List<ASTStatement> statements) implements ASTStatement {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(Block)", statements);
    }

}
