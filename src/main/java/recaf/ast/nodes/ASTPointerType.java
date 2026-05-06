package recaf.ast.nodes;

import recaf.ast.ASTUtils;

public record ASTPointerType(ASTContext ctx, ASTBaseType type) implements ASTType {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(PointerType)", type);
    }

}
