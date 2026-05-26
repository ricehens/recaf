package recaf.ast;

import recaf.parse.ASTUtils;

import java.util.List;

public record ASTProgram(
        ASTContext ctx,
        ASTIdentifier id,
        List<ASTDeclaration> decls
) implements AST {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(Program) " + id.text(),
                decls);
    }

}
