package recaf.ast;

import recaf.parse.ParseUtils;

public record ASTParameter(ASTContext ctx, ASTType type, ASTIdentifier name) {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(Parameter) " + name.text(), type);
    }

}
