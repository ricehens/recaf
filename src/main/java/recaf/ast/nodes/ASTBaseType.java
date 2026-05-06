package recaf.ast.nodes;

import recaf.ast.ASTUtils;

public record ASTBaseType(ASTContext ctx, ASTIdentifier id) implements ASTType {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(BaseType)", id);
    }
}
