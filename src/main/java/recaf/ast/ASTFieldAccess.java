package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTFieldAccess(ASTContext ctx, ASTIdentifier field) implements ASTAccessor {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(FieldAccess) " + field.text());
    }

}
