package recaf.ast;

import recaf.parse.ASTUtils;

public record ASTFieldAccess(ASTContext ctx, ASTIdentifier field) implements ASTAccessor {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(FieldAccess) " + field.text());
    }

}
