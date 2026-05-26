package recaf.ast;

import recaf.parse.ParseUtils;

import java.util.List;

public record ASTProgram(
        ASTContext ctx,
        ASTIdentifier id,
        List<ASTDeclaration> decls
) implements AST {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(Program) " + id.text(),
                decls);
    }

}
