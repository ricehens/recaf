package recaf.common;

/**
 * Represents a binary operator in Decaf, e.g. +.
 */
public enum BinaryOperator {

    PLUS("+", BinOpType.ARITH_OP),
    MINUS("-", BinOpType.ARITH_OP),
    TIMES("*", BinOpType.ARITH_OP),
    DIVIDES("/", BinOpType.ARITH_OP),
    MOD("%", BinOpType.ARITH_OP),
    LT("<", BinOpType.REL_OP),
    GT(">", BinOpType.REL_OP),
    LEQ("<=", BinOpType.REL_OP),
    GEQ(">=", BinOpType.REL_OP),
    EQ("==", BinOpType.EQ_OP),
    NEQ("!=", BinOpType.EQ_OP),
    AND("&&", BinOpType.COND_OP),
    OR("||", BinOpType.COND_OP);

    private final String symbol;
    private final BinOpType type;

    BinaryOperator(String symbol, BinOpType type) {
        this.symbol = symbol;
        this.type = type;
    }

    @Override
    public String toString() {
        return "(BinOp) " + symbol;
    }

    /**
     * Returns the type of the binary operator, distinguishing ARITH_OP (+),
     * REL_OP (&lt;), EQ_OP (==), COND_OP (&amp;&amp;).
     *
     * @return type of the binary operator
     */
    public BinOpType getType() {
        return type;
    }

    /**
     * Represents the type of the binary operator, distinguishing ARITH_OP (+),
     * REL_OP (&lt;), EQ_OP (==), COND_OP (&amp;&amp;).
     */
    public enum BinOpType {
        ARITH_OP, REL_OP, EQ_OP, COND_OP
    }

    /**
     * Returns the symbol (e.g. "+") of the operator
     *
     * @return the symbol of the operator
     */
    public String getSymbol() {
        return symbol;
    }

}
