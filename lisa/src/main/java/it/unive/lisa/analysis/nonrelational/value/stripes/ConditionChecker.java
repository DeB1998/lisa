package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Monomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Polynomial;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.UnaryOperator;
import it.unive.lisa.symbolic.value.Variable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-17
 * @since version date
 */
class ConditionChecker {

    private static class FullConstraintContainer {

        @Nullable
        private FullConstraint fullConstraint;

        public FullConstraintContainer() {
            this.fullConstraint = null;
        }

        public @Nullable FullConstraint getFullConstraint() {
            return this.fullConstraint;
        }

        public void setFullConstraint(final @NotNull FullConstraint fullConstraint) {
            this.fullConstraint = fullConstraint;
        }

        public void clearConstraint() {
            this.fullConstraint = null;
        }

        public boolean isValid() {
            return this.fullConstraint != null;
        }
    }

    @Unmodifiable
    private final Map<Variable, Set<Constraint>> oldConstraints;

    public ConditionChecker(@Unmodifiable final Map<Variable, Set<Constraint>> oldConstraints) {
        this.oldConstraints = oldConstraints;
    }

    public Satisfiability checkCondition(
        SymbolicExpression condition,
        List<FullConstraint> newConstraints
    ) {
        return this.checkSubCondition(condition, false, newConstraints);
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private Satisfiability checkSubCondition(
        final SymbolicExpression subCondition,
        final boolean toComplement,
        @NotNull final List<FullConstraint> newConstraints
    ) {
        if (
            (subCondition instanceof UnaryExpression unaryExpression) &&
            (unaryExpression.getOperator() == UnaryOperator.LOGICAL_NOT)
        ) {
            return this.checkSubCondition(
                    unaryExpression.getExpression(),
                    !toComplement,
                    newConstraints
                );
        }
        if (subCondition instanceof BinaryExpression binaryExpression) {
            final BinaryOperator operator = binaryExpression.getOperator();
            if (
                (operator == BinaryOperator.LOGICAL_AND) || (operator == BinaryOperator.LOGICAL_OR)
            ) {
                final List<FullConstraint> leftNewConstraints = new LinkedList<>();
                final List<FullConstraint> rightNewConstraints = new LinkedList<>();
                final Satisfiability leftSatisfiability =
                    this.checkSubCondition(
                            binaryExpression.getLeft(),
                            toComplement,
                            leftNewConstraints
                        );
                final Satisfiability rightSatisfiability =
                    this.checkSubCondition(
                            binaryExpression.getRight(),
                            toComplement,
                            rightNewConstraints
                        );

                final BinaryOperator operatorToTest = (
                        ((operator == BinaryOperator.LOGICAL_AND) && !toComplement) ||
                        ((operator == BinaryOperator.LOGICAL_OR) && toComplement)
                    )
                    ? BinaryOperator.LOGICAL_AND
                    : BinaryOperator.LOGICAL_OR;

                if (operatorToTest == BinaryOperator.LOGICAL_AND) {
                    if (
                        (leftSatisfiability == Satisfiability.SATISFIED) &&
                        (rightSatisfiability == Satisfiability.SATISFIED)
                    ) {
                        return Satisfiability.SATISFIED;
                    }
                    if (
                        (leftSatisfiability == Satisfiability.NOT_SATISFIED) ||
                        (rightSatisfiability == Satisfiability.NOT_SATISFIED)
                    ) {
                        return Satisfiability.NOT_SATISFIED;
                    }

                    if (leftSatisfiability == Satisfiability.UNKNOWN) {
                        newConstraints.addAll(leftNewConstraints);
                    }
                    if (rightSatisfiability == Satisfiability.UNKNOWN) {
                        newConstraints.addAll(rightNewConstraints);
                    }
                    return Satisfiability.UNKNOWN;
                }
                if (
                    leftSatisfiability == Satisfiability.SATISFIED ||
                    rightSatisfiability == Satisfiability.SATISFIED
                ) {
                    return Satisfiability.SATISFIED;
                }
                if (leftSatisfiability == Satisfiability.NOT_SATISFIED) {
                    newConstraints.addAll(rightNewConstraints);
                }
                if (rightSatisfiability == Satisfiability.NOT_SATISFIED) {
                    newConstraints.addAll(leftNewConstraints);
                }
                return Satisfiability.UNKNOWN;
            }
            FullConstraintContainer firstFullConstraintContainer = new FullConstraintContainer();
            FullConstraintContainer secondFullConstraintContainer = new FullConstraintContainer();
            Satisfiability result =
                this.checkSimpleCondition(
                        binaryExpression,
                        toComplement,
                        firstFullConstraintContainer,
                        secondFullConstraintContainer
                    );
            if (result == Satisfiability.UNKNOWN) {
                if (firstFullConstraintContainer.isValid()) {
                    newConstraints.add(firstFullConstraintContainer.getFullConstraint());
                }
                if (secondFullConstraintContainer.isValid()) {
                    newConstraints.add(secondFullConstraintContainer.getFullConstraint());
                }
            }
            return result;
        }
        return Satisfiability.UNKNOWN;
    }

    private Satisfiability checkSimpleCondition(
        final BinaryExpression expression,
        final boolean toComplement,
        @NotNull final FullConstraintContainer firstConstraintContainer,
        @NotNull final FullConstraintContainer secondConstraintContainer
    ) {
        if (ConditionChecker.checkOperator(expression.getOperator())) {
            final Polynomial left = Simplifier.simplify(expression.getLeft(), 3);
            final Polynomial right = Simplifier.simplify(expression.getRight(), 3);
            final BinaryOperator operator = ConditionChecker.complementOperator(
                expression.getOperator(),
                toComplement
            );
            final BinaryOperator complementedOperator = ConditionChecker.complementOperator(
                expression.getOperator(),
                !toComplement
            );

            final boolean alwaysSatisfied =
                this.checkSimpleExpression(
                        left,
                        right,
                        operator,
                        firstConstraintContainer,
                        secondConstraintContainer
                    );
            final boolean alwaysNotSatisfied =
                this.checkSimpleExpression(left, right, complementedOperator, null, null);
            if (!alwaysSatisfied && !alwaysNotSatisfied) {
                return Satisfiability.UNKNOWN;
            }
            firstConstraintContainer.clearConstraint();
            secondConstraintContainer.clearConstraint();
            if (alwaysSatisfied) {
                return Satisfiability.SATISFIED;
            }
            return Satisfiability.NOT_SATISFIED;
        }
        return Satisfiability.UNKNOWN;
    }

    private boolean checkSimpleExpression(
        final Polynomial left,
        final Polynomial right,
        final BinaryOperator operator,
        @Nullable final FullConstraintContainer firstConstraintContainer,
        @Nullable final FullConstraintContainer secondConstraintContainer
    ) {
        if (
            (operator == BinaryOperator.COMPARISON_EQ) || (operator == BinaryOperator.COMPARISON_NE)
        ) {
            final BinaryOperator operatorToUse = (operator == BinaryOperator.COMPARISON_EQ)
                ? BinaryOperator.COMPARISON_GE
                : BinaryOperator.COMPARISON_GT;
            final boolean first =
                this.checkInequality(left, right, operatorToUse, firstConstraintContainer);
            final boolean second =
                this.checkInequality(right, left, operatorToUse, secondConstraintContainer);
            if (operator == BinaryOperator.COMPARISON_EQ) {
                return first && second;
            }
            if (
                (firstConstraintContainer != null) &&
                firstConstraintContainer.isValid() &&
                (secondConstraintContainer != null) &&
                secondConstraintContainer.isValid()
            ) {
                firstConstraintContainer.clearConstraint();
                secondConstraintContainer.clearConstraint();
            }
            return first || second;
        }
        return this.checkInequality(left, right, operator, firstConstraintContainer);
    }

    private boolean checkInequality(
        Polynomial left,
        Polynomial right,
        BinaryOperator operator,
        @Nullable final FullConstraintContainer newConstraintContainer
    ) {
        if (
            (operator == BinaryOperator.COMPARISON_LE) || (operator == BinaryOperator.COMPARISON_LT)
        ) {
            final Polynomial temp = left;
            left = right;
            right = temp;
            operator =
                (operator == BinaryOperator.COMPARISON_LE)
                    ? BinaryOperator.COMPARISON_GE
                    : BinaryOperator.COMPARISON_GT;
        }

        if (operator == BinaryOperator.COMPARISON_GE) {
            right = right.subtract(new Polynomial(3, 1));
        }
        return this.checkPolynomial(left.subtract(right), newConstraintContainer);
    }

    private boolean checkPolynomial(
        @NotNull final Polynomial polynomialToCheck,
        @Nullable final FullConstraintContainer newConstraintContainer
    ) {
        if (!polynomialToCheck.isValid() || (polynomialToCheck.getSize() < 2)) {
            if (newConstraintContainer != null) {
                newConstraintContainer.clearConstraint();
            }
            return false;
        }
        final Polynomial normalizedPolynomial = ConditionChecker.normalizePolynomial(
            polynomialToCheck
        );
        final Monomial aMonomial = normalizedPolynomial.getMonomial(0);
        final Monomial bMonomial = normalizedPolynomial.getMonomial(1);
        @Nullable
        final Monomial cMonomial = (normalizedPolynomial.getSize() == 3)
            ? normalizedPolynomial.getMonomial(2)
            : null;
        final Variable aVariable = aMonomial.getVariable();
        final Variable bVariable = bMonomial.getVariable();
        @Nullable
        final Variable cVariable = (cMonomial == null) ? null : cMonomial.getVariable();
        final int aCoefficient = aMonomial.getCoefficient();
        final int bCoefficient = bMonomial.getCoefficient();
        final int cCoefficient = (cMonomial == null) ? 0 : cMonomial.getCoefficient();
        final int k2 = -normalizedPolynomial.getConstantCoefficient();

        final List<FullConstraint> constraintsToCheck = new LinkedList<>();
        if ((aCoefficient == 1) && ((bCoefficient == cCoefficient) || (cMonomial == null))) {
            constraintsToCheck.add(
                new FullConstraint(aVariable, bVariable, cVariable, -bCoefficient, k2)
            );
        }
        if ((bCoefficient == 1) && ((aCoefficient == cCoefficient) || (cMonomial == null))) {
            constraintsToCheck.add(
                new FullConstraint(bVariable, aVariable, cVariable, -aCoefficient, k2)
            );
        }
        if ((cCoefficient == 1) && (aCoefficient == bCoefficient)) {
            constraintsToCheck.add(
                new FullConstraint(cVariable, aVariable, bVariable, -aCoefficient, k2)
            );
        }

        // Check the constraints
        for (final FullConstraint constraintToCheck : constraintsToCheck) {
            if (this.checkConstraint(constraintToCheck)) {
                return true;
            }
        } // i->(arr_len, _, 1, >= -1)
        if (newConstraintContainer != null) {
            if (constraintsToCheck.size() == 1) {
                newConstraintContainer.setFullConstraint(constraintsToCheck.get(0));
            } else {
                newConstraintContainer.clearConstraint();
            }
        }
        return false;
    }

    private boolean checkConstraint(final FullConstraint constraintToCheck) {
        final Set<Constraint> constraints = this.oldConstraints.get(constraintToCheck.getX());
        if (constraints != null) {
            for (final Constraint constraint : constraints) {
                if (
                    constraintToCheck.differsOnlyOnK2(constraint) &&
                    (constraintToCheck.getK2() <= constraint.getK2())
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Polynomial normalizePolynomial(final Polynomial polynomialToNormalize) {
        final int aCoefficient = Math.abs(polynomialToNormalize.getMonomial(0).getCoefficient());
        final int bCoefficient = Math.abs(polynomialToNormalize.getMonomial(1).getCoefficient());
        final int cCoefficient = (polynomialToNormalize.getSize() == 3)
            ? Math.abs(polynomialToNormalize.getMonomial(1).getCoefficient())
            : 0;
        int gcd = ConditionChecker.gcd(aCoefficient, bCoefficient);
        if (cCoefficient != 0) {
            gcd = ConditionChecker.gcd(cCoefficient, gcd);
        }
        gcd = ConditionChecker.gcd(polynomialToNormalize.getConstantCoefficient(), gcd);
        return polynomialToNormalize.divide(gcd);
    }

    private static boolean checkOperator(final BinaryOperator operator) {
        return (
            (operator == BinaryOperator.COMPARISON_EQ) ||
            (operator == BinaryOperator.COMPARISON_NE) ||
            (operator == BinaryOperator.COMPARISON_GT) ||
            (operator == BinaryOperator.COMPARISON_GE) ||
            (operator == BinaryOperator.COMPARISON_LT) ||
            (operator == BinaryOperator.COMPARISON_LE)
        );
    }

    private static BinaryOperator complementOperator(
        final BinaryOperator operator,
        final boolean toComplement
    ) {
        if (!toComplement) {
            return operator;
        }
        return switch (operator) {
            case COMPARISON_EQ -> BinaryOperator.COMPARISON_NE;
            case COMPARISON_NE -> BinaryOperator.COMPARISON_EQ;
            case COMPARISON_GT -> BinaryOperator.COMPARISON_LE;
            case COMPARISON_GE -> BinaryOperator.COMPARISON_LT;
            case COMPARISON_LT -> BinaryOperator.COMPARISON_GE;
            case COMPARISON_LE -> BinaryOperator.COMPARISON_GT;
            default -> throw new IllegalArgumentException(
                "Unable to complement binary operator" + operator
            );
        };
    }

    private static int gcd(int a, int b) {
        int mcd = a;
        do {
            final int modulo = a % b;
            if (modulo == 0) {
                mcd = b;
            } else {
                a = b;
                b = modulo;
            }
        } while (mcd > 1);

        return mcd;
    }
}
