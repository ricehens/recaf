package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTPointerType(ASTContext ctx, ASTType type) implements ASTType {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(PointerType)", type);
    }

}
