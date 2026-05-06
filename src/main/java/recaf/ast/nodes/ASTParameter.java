package recaf.ast.nodes;

import recaf.ast.ASTUtils;

public record ASTParameter(ASTContext ctx, ASTType type, ASTIdentifier name) {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(Parameter) " + name.text(), type);
    }

}
