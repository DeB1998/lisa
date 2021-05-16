package it.unive.lisa.test.stripes.simplifier;

import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.UnaryOperator;
import it.unive.lisa.symbolic.value.Variable;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-05-02
 * @since version date
 */
public final class OldSimplifier {
    
    private OldSimplifier() {
    
    }
    
    @SuppressWarnings("ChainOfInstanceofChecks")
    @Nullable
    public static SimplificationResult simplify(final SymbolicExpression expression) {
        
        if ((expression instanceof Constant constant) &&
                (constant.getValue() instanceof Integer value)) {
            return new SimplificationResult(value);
        }
        if (expression instanceof Variable variable) {
            return new SimplificationResult(variable, 1, null, 0, 0);
        }
        if ((expression instanceof UnaryExpression unaryExpression) &&
                (unaryExpression.getOperator() == UnaryOperator.NUMERIC_NEG)) {
            final SimplificationResult result = OldSimplifier.simplify(
                unaryExpression.getExpression()
            );
            if (result != null) {
                result.setFirstIdentifierCount(-result.getFirstIdentifierCount());
                result.setSecondIdentifierCount(-result.getSecondIdentifierCount());
                result.setConstant(-result.getConstant());
                return result;
            }
        }
        if (expression instanceof BinaryExpression binaryExpression) {

            final SimplificationResult left = OldSimplifier.simplify(binaryExpression.getLeft());
            final SimplificationResult right = OldSimplifier.simplify(binaryExpression.getRight());

            if ((left != null) && (right != null)) {
                final Variable leftFirstIdentifier = left.getFirstIdentifier();
                final int leftFirstCount = left.getFirstIdentifierCount();
                final Variable leftSecondIdentifier = left.getSecondIdentifier();
                final int leftSecondCount = left.getSecondIdentifierCount();
                final int leftConstant = left.getConstant();

                final Variable rightFirstIdentifier = right.getFirstIdentifier();
                final int rightFirstCount = right.getFirstIdentifierCount();
                final Variable rightSecondIdentifier = right.getSecondIdentifier();
                final int rightSecondCount = right.getSecondIdentifierCount();
                final int rightConstant = right.getConstant();

                // result;
                return switch (binaryExpression.getOperator()) {
                    case NUMERIC_DIV, NUMERIC_MOD -> OldSimplifier.handleDivision(
                        binaryExpression,
                        left,
                        leftFirstCount,
                        leftSecondCount,
                        leftConstant,
                        rightFirstIdentifier,
                        rightSecondIdentifier,
                        rightConstant
                    );
                    case NUMERIC_MUL -> OldSimplifier.handleMultiplication(
                        left,
                        right,
                        leftFirstIdentifier,
                        leftFirstCount,
                        leftSecondCount,
                        leftConstant,
                        rightFirstIdentifier,
                        rightFirstCount,
                        rightSecondCount,
                        rightConstant
                    );
                    case NUMERIC_ADD, NUMERIC_SUB -> OldSimplifier.handleSumOrSub(
                        binaryExpression,
                        left,
                        right,
                        leftFirstIdentifier,
                        leftFirstCount,
                        leftSecondIdentifier,
                        leftSecondCount,
                        leftConstant,
                        rightFirstIdentifier,
                        rightFirstCount,
                        rightSecondIdentifier,
                        rightSecondCount,
                        rightConstant
                    );
                    default -> null;
                };
            }
        }

        return null;
    }

    @Nullable
    private static SimplificationResult handleSumOrSub(
        final BinaryExpression binaryExpression,
        final SimplificationResult left,
        final SimplificationResult right,
        final Variable leftFirstIdentifier,
        final int leftFirstCount,
        final Variable leftSecondIdentifier,
        final int leftSecondCount,
        final int leftConstant,
        final Variable rightFirstIdentifier,
        int rightFirstCount,
        final Variable rightSecondIdentifier,
        int rightSecondCount,
        int rightConstant
    ) {
        if (binaryExpression.getOperator() == BinaryOperator.NUMERIC_SUB) {
            rightFirstCount = -rightFirstCount;
            rightSecondCount = -rightSecondCount;
            rightConstant = -rightConstant;
        }
        if (leftFirstIdentifier == null) {
            right.setConstant(rightConstant + leftConstant);
            return right;
        }
        if (rightFirstIdentifier == null) {
            left.setConstant(leftConstant + rightConstant);
            return left;
        }
        if (leftSecondIdentifier != null) {
            if (
                leftFirstIdentifier.equals(rightFirstIdentifier) &&
                (
                    (rightSecondIdentifier == null) ||
                    leftSecondIdentifier.equals(rightSecondIdentifier)
                )
            ) {
                left.setFirstIdentifierCount(leftFirstCount + rightFirstCount);
                left.setSecondIdentifierCount(leftSecondCount + rightSecondCount);
                left.setConstant(leftConstant + rightConstant);
                return left;
            }
            if (
                (
                    (rightSecondIdentifier == null) ||
                    leftFirstIdentifier.equals(rightSecondIdentifier)
                ) &&
                leftSecondIdentifier.equals(rightFirstIdentifier)
            ) {
                left.setFirstIdentifierCount(leftFirstCount + rightSecondCount);
                left.setSecondIdentifierCount(leftSecondCount + rightFirstCount);
                left.setConstant(leftConstant + rightConstant);
                return left;
            }
            return null;
        }
        // a sinistra ho una sola variabile sicuramente
        if (rightSecondIdentifier != null) {
            if (rightFirstIdentifier.equals(leftFirstIdentifier)) {
                right.setFirstIdentifierCount(rightFirstCount + leftFirstCount);
            } else if (rightSecondIdentifier.equals(leftFirstIdentifier)) {
                right.setSecondIdentifierCount(rightSecondCount + leftFirstCount);
            } else {
                return null;
            }
            right.setConstant(rightConstant + leftConstant);
            return right;
        }
        // A destra e sinistra c'è una sola variabile
        if (leftFirstIdentifier.equals(rightFirstIdentifier)) {
            left.setFirstIdentifierCount(leftFirstCount + rightFirstCount);
        } else {
            left.setSecondIdentifier(rightFirstIdentifier);
            left.setSecondIdentifierCount(rightFirstCount);
        }
        left.setConstant(leftConstant + rightConstant);
        return left;
    }

    @Nullable
    private static SimplificationResult handleMultiplication(
        final SimplificationResult left,
        final SimplificationResult right,
        final Variable leftFirstIdentifier,
        final int leftFirstCount,
        final int leftSecondCount,
        final int leftConstant,
        final Variable rightFirstIdentifier,
        final int rightFirstCount,
        final int rightSecondCount,
        final int rightConstant
    ) {
        final int constant;
        final int firstCount;
        final int secondCount;
        final int otherConstant;
        final SimplificationResult result;

        if (leftFirstIdentifier == null) {
            constant = leftConstant;
            firstCount = rightFirstCount;
            secondCount = rightSecondCount;
            otherConstant = rightConstant;
            result = right;
        } else if (rightFirstIdentifier == null) {
            constant = rightConstant;
            firstCount = leftFirstCount;
            secondCount = leftSecondCount;
            otherConstant = leftConstant;
            result = left;
        } else {
            return null;
        }

        result.setFirstIdentifierCount(firstCount * constant);
        result.setSecondIdentifierCount(secondCount * constant);
        result.setConstant(otherConstant * constant);

        return result;
    }

    @Nullable
    private static SimplificationResult handleDivision(
        final BinaryExpression binaryExpression,
        final SimplificationResult left,
        final int leftFirstCount,
        final int leftSecondCount,
        final int leftConstant,
        final Variable rightFirstIdentifier,
        final Variable rightSecondIdentifier,
        final int rightConstant
    ) {
        final boolean isDivision = binaryExpression.getOperator() == BinaryOperator.NUMERIC_DIV;
        if (
            (rightFirstIdentifier == null) &&
            (rightSecondIdentifier == null) &&
            (rightConstant != 0) &&
            ((leftFirstCount % rightConstant) == 0) &&
            ((leftSecondCount % rightConstant) == 0)
        ) {
            left.setFirstIdentifierCount(isDivision ? (leftFirstCount / rightConstant) : 0);
            left.setSecondIdentifierCount(isDivision ? (leftSecondCount / rightConstant) : 0);
            if (isDivision && ((leftConstant % rightConstant) == 0)) {
                left.setConstant(leftConstant / rightConstant);
            } else {
                left.setConstant(leftConstant % rightConstant);
            }

            return left;
        }
        return null;
    }
}
