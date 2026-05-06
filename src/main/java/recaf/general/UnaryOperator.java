package recaf.general;

/**
 * Represents first unary operator, i.e. - or !.
 */
public enum UnaryOperator {

    MINUS("-"), NOT("!");

    private final String symbol;

    UnaryOperator(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "(UnOp) " + symbol;
    }

}
