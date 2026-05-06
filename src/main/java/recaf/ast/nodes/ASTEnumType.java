package recaf.ast.nodes;

import recaf.ast.ASTUtils;

import java.util.List;

public record ASTEnumType(ASTContext ctx, List<ASTIdentifier> members) implements ASTType {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(EnumType)", members);
    }
}
