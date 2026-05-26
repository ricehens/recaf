package recaf.ast;

import recaf.parse.ASTUtils;

public record ASTParameter(ASTContext ctx, ASTType type, ASTIdentifier name) {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(Parameter) " + name.text(), type);
    }

}
