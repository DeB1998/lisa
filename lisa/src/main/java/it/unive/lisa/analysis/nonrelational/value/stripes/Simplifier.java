package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Polynomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.PolynomialBuilder;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.UnaryOperator;
import it.unive.lisa.symbolic.value.Variable;
import org.jetbrains.annotations.NotNull;

/**
 * Class that allows for simplifying an expression.
 *
 * @author DeB
 * @version 1.0 2021-05-16
 * @since version date
 */
final class Simplifier {

    /**
     * Private constructor that forbids the instantiation of this class that exposes only static
     * methods.
     */
    private Simplifier() {}

    /**
     * Simplifies the specify expression. The resulting polynomial represents the expression where
     * multiple occurrences of the same variable are collapsed.<br> All the expressions can be
     * simplified but only the ones containing only integer values give a valid result.<br> This
     * simplifier handles sums, subtractions, inversions, multiplications, divisions and modulo
     * operations. If the expression contains other operations, the result will be the invalid
     * polynomial.<br> Note that:
     * <ul>
     *     <li>Division and modulo operations apply only between an expression and a constant.
     *     The expression must be dividable by the constant, i.e., the division gives remainder 0
     *     on all non-constant values;</li>
     *     <li>Multiplication can be performed only between an expression and a constant or
     *     vice-versa;</li>
     *     <li>Two expressions can be added or subtracted only if the resulting expression can be
     *     represented with at most {@code monomialsCount} monomials;</li>
     *     <li>The negation operation is always allowed.</li>
     * </ul>
     *
     * @param expression Expression to simplify.
     * @param monomialsCount Maximum number of monomials the simplified expression can
     *         contain, constant excluded.
     * @return The polynomial resulting from the simplification, or {@link Polynomial#INVALID} if
     *         the simplification cannot be performed.
     */
    @SuppressWarnings({ "FeatureEnvy", "MethodWithMultipleReturnPoints", "OverlyComplexMethod" })
    @NotNull
    public static Polynomial simplify(
        final SymbolicExpression expression,
        final int monomialsCount
    ) {
        // Check the type of expression
        //noinspection ChainOfInstanceofChecks
        if (
            (expression instanceof Constant constant) &&
            (constant.getValue() instanceof Integer value)
        ) {
            // Create a constant polynomial
            return new PolynomialBuilder(monomialsCount).setConstantCoefficient(value).build();
        }
        // Variables
        if (expression instanceof Variable variable) {
            // Create a polynomial with only one monomial
            return new PolynomialBuilder(monomialsCount).addMonomial(1, variable).build();
        }
        // Negation operator
        if (
            (expression instanceof UnaryExpression unaryExpression) &&
            (unaryExpression.getOperator() == UnaryOperator.NUMERIC_NEG)
        ) {
            // Recursively simplify the expression
            final Polynomial result = Simplifier.simplify(
                unaryExpression.getExpression(),
                monomialsCount
            );
            // Invert the result
            return result.negate();
        }
        // Sum, subtract, multiply, divide and modulo operators
        if (expression instanceof BinaryExpression binaryExpression) {
            // Recursively simplify the expression on the left and on the right
            final Polynomial left = Simplifier.simplify(binaryExpression.getLeft(), monomialsCount);
            final Polynomial right = Simplifier.simplify(
                binaryExpression.getRight(),
                monomialsCount
            );
            // Check if both expressions has been correctly simplified
            if (!left.isValid()) {
                return left;
            }
            if (!right.isValid()) {
                return right;
            }

            // Check the operator
            switch (binaryExpression.getOperator()) {
                // Division
                case NUMERIC_DIV:
                    // Only division by constant values are allowed
                    if (!right.isConstantPolynomial()) {
                        return Polynomial.INVALID;
                    }
                    // Perform the division
                    return left.divide(right.getConstantCoefficient());
                // Modulo
                case NUMERIC_MOD:
                    // Only modulo by constant values is allowed
                    if (!right.isConstantPolynomial()) {
                        return Polynomial.INVALID;
                    }
                    // Perform the modulo
                    return left.modulo(right.getConstantCoefficient());
                // Multiplication
                case NUMERIC_MUL:
                    // Only multiplication by constant values are allowed
                    if (left.isConstantPolynomial()) {
                        return right.multiply(left.getConstantCoefficient());
                    }
                    if (right.isConstantPolynomial()) {
                        return left.multiply(right.getConstantCoefficient());
                    }
                    // The operation cannot be performed
                    return Polynomial.INVALID;
                // Sum
                case NUMERIC_ADD:
                    // Sum the polynomials
                    return left.sum(right);
                // Subtraction
                case NUMERIC_SUB:
                    // Subtract the polynomials
                    return left.subtract(right);
                // Other operators
                default:
                    return Polynomial.INVALID;
            }
        }

        // The expression cannot be simplified
        return Polynomial.INVALID;
    }
}
