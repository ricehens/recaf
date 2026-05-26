package recaf.ast;

import recaf.parse.ParseUtils;

import java.util.List;

public record ASTBlock(ASTContext ctx, List<ASTStatement> statements) implements ASTStatement {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(Block)", statements);
    }

}
