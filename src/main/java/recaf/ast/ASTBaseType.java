package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTBaseType(ASTContext ctx, ASTIdentifier id) implements ASTType {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(BaseType)", id);
    }
}
