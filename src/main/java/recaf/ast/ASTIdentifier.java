package recaf.ast;

public record ASTIdentifier(ASTContext ctx, String text) implements AST {

    @Override
    public String toString() {
        return "(Identifier) " + text;
    }

}
