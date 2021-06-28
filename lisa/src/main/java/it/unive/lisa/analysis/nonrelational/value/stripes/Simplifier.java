package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Polynomial;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.UnaryOperator;
import it.unive.lisa.symbolic.value.Variable;
import org.jetbrains.annotations.NotNull;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-05-16
 * @since version date
 */
public final class Simplifier {

    private Simplifier() {}

    @NotNull
    public static Polynomial simplify(
        final SymbolicExpression expression,
        final int monomialCount
    ) {
        //noinspection ChainOfInstanceofChecks
        if (
            (expression instanceof Constant constant) &&
            (constant.getValue() instanceof Integer value)
        ) {
            return new Polynomial(monomialCount, value);
        }
        if (expression instanceof Variable variable) {
            return new Polynomial(monomialCount, variable, 1);
        }
        if (
            (expression instanceof UnaryExpression unaryExpression) &&
            (unaryExpression.getOperator() == UnaryOperator.NUMERIC_NEG)
        ) {
            final Polynomial result = Simplifier.simplify(
                unaryExpression.getExpression(),
                monomialCount
            );
            return result.invert();
        }
        if (expression instanceof BinaryExpression binaryExpression) {
            final Polynomial left = Simplifier.simplify(binaryExpression.getLeft(), monomialCount);
            final Polynomial right = Simplifier.simplify(
                binaryExpression.getRight(),
                monomialCount
            );

            if (!left.isValid()) {
                return left;
            }
            if (!right.isValid()) {
                return right;
            }

            // result;
            switch (binaryExpression.getOperator()) {
                case NUMERIC_DIV:
                    if (!right.isConstantPolynomial()) {
                        return Polynomial.INVALID;
                    }
                    return left.divide(right.getConstantCoefficient());
                case NUMERIC_MOD:
                    if (!right.isConstantPolynomial()) {
                        return Polynomial.INVALID;
                    }
                    return left.modulo(right.getConstantCoefficient());
                case NUMERIC_MUL:
                    if (left.isConstantPolynomial()) {
                        return right.multiply(left.getConstantCoefficient());
                    }
                    if (right.isConstantPolynomial()) {
                        return left.multiply(right.getConstantCoefficient());
                    }
                    return Polynomial.INVALID;
                case NUMERIC_ADD:
                    return left.add(right);
                case NUMERIC_SUB:
                    return left.subtract(right);
                default:
                    return Polynomial.INVALID;
            }
        }

        return Polynomial.INVALID;
    }
}
