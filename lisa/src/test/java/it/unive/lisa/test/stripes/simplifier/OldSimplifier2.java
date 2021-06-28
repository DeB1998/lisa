package it.unive.lisa.test.stripes.simplifier;

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
public final class OldSimplifier2 {
    
    private OldSimplifier2() {
    
    }
    
    @NotNull
    public static OldPolynomial simplify(
        final SymbolicExpression expression,
        final int monomialCount
    ) {
        //noinspection ChainOfInstanceofChecks
        if ((expression instanceof Constant constant) &&
               (constant.getValue() instanceof Integer value)) {
            return new OldPolynomial(monomialCount, value);
        }
        if (expression instanceof Variable variable) {
            return new OldPolynomial(monomialCount, variable, 1);
        }
        if ((expression instanceof UnaryExpression unaryExpression) &&
                (unaryExpression.getOperator() == UnaryOperator.NUMERIC_NEG)) {

            final OldPolynomial result = OldSimplifier2.simplify(
                unaryExpression.getExpression(),
                monomialCount
            );
            result.invert();
            return result;
        }
        if (expression instanceof BinaryExpression binaryExpression) {
            final OldPolynomial left = OldSimplifier2.simplify(binaryExpression.getLeft(), monomialCount);
            final OldPolynomial right = OldSimplifier2.simplify(
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
                    if (!right.isConstant()) {
                        return OldPolynomial.INVALID;
                    }
                    left.divide(right.getConstant());
                    return left;
                case NUMERIC_MOD:
                    if (!right.isConstant()) {
                        return OldPolynomial.INVALID;
                    }
                    left.modulo(right.getConstant());
                    return left;
                case NUMERIC_MUL:
                    if (left.isConstant()) {
                        right.multiply(left.getConstant());
                        return right;
                    }
                    if (right.isConstant()) {
                        left.multiply(right.getConstant());
                        return left;
                    }
                    return OldPolynomial.INVALID;
                case NUMERIC_ADD:
                    left.add(right);
                    return left;
                case NUMERIC_SUB:
                    left.subtract(right);
                    return left;
                default:
                    return OldPolynomial.INVALID;
            }
        }

        return OldPolynomial.INVALID;
    }
}
