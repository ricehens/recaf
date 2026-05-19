package recaf.ast.nodes;

public record ASTNil(ASTContext ctx) implements ASTExpression {

    @Override
    public String toString() {
        return "(Nil)";
    }

}
