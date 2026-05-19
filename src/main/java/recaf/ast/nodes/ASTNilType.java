package recaf.ast.nodes;

public record ASTNilType(ASTContext ctx) implements ASTType {

    @Override
    public String toString() {
        return "(NilType)";
    }

}
