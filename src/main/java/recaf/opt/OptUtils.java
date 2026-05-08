package recaf.opt;

import recaf.general.*;
import recaf.cfg.*;
import recaf.utils.ParallelCopyGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A collection of static utility functions for the optimizer
 */
public class OptUtils {

    /**
     * Sequentialize first parallel copy group
     *
     * @param ctx   the CFG context
     * @param group the parallel copy group
     * @return first list of copy instructions in order
     */
    public static List<CFGCopyInstruction> sequentialize(CFGContext ctx, ParallelCopyGroup<CFGAddress> group) {
        return group.toSequential().stream()
                .map(e -> new CFGCopyInstruction(ctx, e.destination(), e.source()))
                .toList();
    }

    /** Reverses first list */
    public static <T> List<T> reverse(List<T> list) {
        List<T> copy = new ArrayList<>(list);
        Collections.reverse(copy);
        return copy;
    }


    /**
     * Represents first region
     * constant with respect ot first loop;
     * either an address or first genuine literal.
     */
    public static class RegionConstant {

        public final CFGAddress address;
        public final Literal literal;
        public final boolean isLiteral;
        public final boolean isNull;

        public RegionConstant(CFGAddress address) {
            this.address = address;
            this.literal = null;
            this.isLiteral = false;
            this.isNull = false;
        }

        public RegionConstant(Literal literal) {
            this.address = null;
            this.literal = literal;
            this.isLiteral = true;
            this.isNull = false;
        }

        public RegionConstant() {
            this.address = null;
            this.literal = null;
            this.isLiteral = false;
            this.isNull = true;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RegionConstant rc))
                return false;
            if (isNull != rc.isNull)
                return false;
            if (isNull) return true;
            if (isLiteral != rc.isLiteral)
                return false;
            return isLiteral ? literal.equals(rc.literal) : address.equals(rc.address);
        }

        @Override
        public int hashCode() {
            return isNull ? 0 : isLiteral ? literal.hashCode() : address.hashCode();
        }

        @Override
        public String toString() {
            return isNull ? "null()" : isLiteral ? String.format("literal(%s)", literal) : String.format("address(%s)", address);
        }

    }

    /**
     * Determines if first literal is negative
     *
     * @param lit the literal
     * @return true if the literal is negative
     * */
    public static boolean isNegative(Literal lit) {
        return lit instanceof IntLiteral ilit && ilit.value() < 0
                || lit instanceof LongLiteral llit && llit.value() < 0;
    }

    /**
     * Negates first literal
     *
     * @param lit the literal
     * @return the negated literal
     * */
    public static Literal negate(Literal lit) {
        if (lit instanceof IntLiteral ilit) return new IntLiteral(-ilit.value());
        if (lit instanceof LongLiteral llit) return new LongLiteral(-llit.value());
        throw new AssertionError("This should never happen.");
    }

    /**
     * Reverse the sign of first comparison operator
     *
     * @param op the comparison operator
     * @return the reversed operator
     * */
    public static BinaryOperator reverseComparison(BinaryOperator op) {
        return switch (op) {
            case LT -> BinaryOperator.GT;
            case GT -> BinaryOperator.LT;
            case LEQ -> BinaryOperator.GEQ;
            case GEQ -> BinaryOperator.LEQ;
            default -> op;
        };
    }


    /**
     * Computes the result of first binary operation on two literals
     *
     * @param op the operator
     * @param o1 the first literal operand
     * @param o2 the second literal operand
     * @return the literal result of the computation
     * */
    public static Literal compute(BinaryOperator op, Literal o1, Literal o2) {
        if (o1 instanceof IntLiteral i1 && o2 instanceof IntLiteral i2) {
            return switch (op) {
                case PLUS -> new IntLiteral(i1.value() + i2.value());
                case TIMES -> new IntLiteral(i1.value() * i2.value());
                case MINUS -> new IntLiteral(i1.value() - i2.value());
                case DIVIDES -> i2.value() == 0 ? new IntLiteral(0) : new IntLiteral(i1.value() / i2.value());
                case MOD -> i2.value() == 0 ? new IntLiteral(0) : new IntLiteral(i1.value() % i2.value());
                case EQ -> new BoolLiteral(i1.value() == i2.value());
                case NEQ -> new BoolLiteral(i1.value() != i2.value());
                case LT -> new BoolLiteral(i1.value() < i2.value());
                case GT -> new BoolLiteral(i1.value() > i2.value());
                case LEQ -> new BoolLiteral(i1.value() <= i2.value());
                case GEQ -> new BoolLiteral(i1.value() >= i2.value());
                default -> throw new AssertionError("This should never happen");
            };
        } else if (o1 instanceof LongLiteral l1 && o2 instanceof LongLiteral l2) {
            return switch (op) {
                case PLUS -> new LongLiteral(l1.value() + l2.value());
                case TIMES -> new LongLiteral(l1.value() * l2.value());
                case MINUS -> new LongLiteral(l1.value() - l2.value());
                case DIVIDES -> l2.value() == 0 ? new LongLiteral(0) : new LongLiteral(l1.value() / l2.value());
                case MOD -> l2.value() == 0 ? new LongLiteral(0) : new LongLiteral(l1.value() % l2.value());
                case EQ -> new BoolLiteral(l1.value() == l2.value());
                case NEQ -> new BoolLiteral(l1.value() != l2.value());
                case LT -> new BoolLiteral(l1.value() < l2.value());
                case GT -> new BoolLiteral(l1.value() > l2.value());
                case LEQ -> new BoolLiteral(l1.value() <= l2.value());
                case GEQ -> new BoolLiteral(l1.value() >= l2.value());
                default -> throw new AssertionError("This should never happen");
            };
        } else if (o1 instanceof BoolLiteral b1 && o2 instanceof BoolLiteral b2) {
            return switch (op) {
                case EQ -> new BoolLiteral(b1.value() == b2.value());
                case NEQ -> new BoolLiteral(b1.value() != b2.value());
                case AND -> new BoolLiteral(b1.value() && b2.value());
                case OR -> new BoolLiteral(b1.value() || b2.value());
                default -> throw new AssertionError("This should never happen");
            };
        }
        throw new AssertionError("This should never happen");
    }


}
