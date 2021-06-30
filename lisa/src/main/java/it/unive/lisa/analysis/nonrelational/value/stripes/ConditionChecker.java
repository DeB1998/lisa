package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Monomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Polynomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.PolynomialBuilder;
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
 * Class that checks if a condition is always true, is always false or neither of the two.
 *
 * @author Alessio De Biasi
 * @author Jonathan Gobbo
 * @version 1.0.1 2021-06-30
 * @since 1.5 2021-04-17
 */
@SuppressWarnings("OverlyComplexClass")
final class ConditionChecker {

    /**
     * Utility class that holds a reference to a {@link  FullConstraint}.
     *
     * @author Alessio De Biasi
     * @author Jonathan Gobbo
     * @version 1.0 2021-06-27
     * @since 1.5 2021-04-17
     */
    private static final class FullConstraintContainer {

        /**
         * Reference to the full constraint.
         */
        @Nullable
        private FullConstraint fullConstraint;

        /**
         * Creates an empty container.
         */
        private FullConstraintContainer() {
            this.fullConstraint = null;
        }

        /**
         * Returns the content of the container, or {@code null} if the container is empty.
         *
         * @return The content of the container.
         */
        @Nullable
        private FullConstraint getFullConstraint() {
            return this.fullConstraint;
        }

        /**
         * Set the content of the container.
         *
         * @param fullConstraint The new content of the container.
         */
        private void setFullConstraint(final @NotNull FullConstraint fullConstraint) {
            this.fullConstraint = fullConstraint;
        }

        /**
         * Deletes the content of the container.
         */
        private void clearConstraint() {
            this.fullConstraint = null;
        }

        /**
         * Checks if the container contains a valid reference.
         *
         * @return {@code true} if the container contains a valid reference to a full constraint,
         *         {@code false} otherwise.
         */
        private boolean isValid() {
            return this.fullConstraint != null;
        }
    }

    /**
     * Map that constants the currently tracked variables and their constraints.
     */
    @Unmodifiable
    @NotNull
    private final Map<@NotNull Variable,
            ? extends @Unmodifiable @NotNull Set<@NotNull Constraint>> trackedConstraints;

    /**
     * Creates a new condition checker over the specified tracked constraints.
     *
     * @param trackedConstraints The currently tracked constraints.
     */
    ConditionChecker(
        final @Unmodifiable @NotNull Map<@NotNull Variable,
                ? extends @Unmodifiable @NotNull Set<@NotNull Constraint>> trackedConstraints
    ) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.trackedConstraints = trackedConstraints;
    }

    /**
     * Checks if the given condition is always true, always false or neither of the two according to
     * the tracked constraints specified during construction.
     *
     * @param condition Condition to check.
     * @param inferredConstraints List that will contain the constraints inferred on the
     *         condition if it is neither always true nor always false.
     * @return {@link Satisfiability#SATISFIED} if the condition is proven to be always true, {@link
     *         Satisfiability#NOT_SATISFIED} if the condition is proven to be always false, {@link
     *         Satisfiability#UNKNOWN} otherwise.
     */
    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    @NotNull
    Satisfiability checkCondition(
        @NotNull final SymbolicExpression condition,
        @NotNull final List<FullConstraint> inferredConstraints
    ) {
        // Check the condition without complementing the operators
        return this.checkSubCondition(condition, false, inferredConstraints);
    }

    /**
     * Checks if the specified condition is always true, always false or neither of the two
     * according to the tracked constraints specified during construction.<br> This method
     * recursively checks the sub-conditions contained in the specified condition and computes the
     * final result.
     *
     * @param subCondition Condition to check.
     * @param toComplement {@code true} if the operators have to be complemented, {@code
     *         false} otherwise.
     * @param inferredConstraints List that will contain the constraints inferred on the
     *         condition if it is neither always true nor always false.
     * @return {@link Satisfiability#SATISFIED} if the condition is proven to be always true, {@link
     *         Satisfiability#NOT_SATISFIED} if the condition is proven to be always false, {@link
     *         Satisfiability#UNKNOWN} otherwise.
     */
    @SuppressWarnings(
        {
            "ChainOfInstanceofChecks",
            "NonBooleanMethodNameMayNotStartWithQuestion",
            "FeatureEnvy",
            "MethodWithMultipleReturnPoints",
            "OverlyComplexMethod"
        }
    )
    @NotNull
    private Satisfiability checkSubCondition(
        @NotNull final SymbolicExpression subCondition,
        final boolean toComplement,
        @NotNull final List<FullConstraint> inferredConstraints
    ) {
        // Logical not operator
        if (
            (subCondition instanceof UnaryExpression unaryExpression) &&
            (unaryExpression.getOperator() == UnaryOperator.LOGICAL_NOT)
        ) {
            //noinspection TailRecursion
            return this.checkSubCondition(
                    unaryExpression.getExpression(),
                    !toComplement,
                    inferredConstraints
                );
        }
        // Binary operators
        if (subCondition instanceof BinaryExpression binaryExpression) {
            final BinaryOperator operator = binaryExpression.getOperator();
            // Logical and and logical or
            if (
                (operator == BinaryOperator.LOGICAL_AND) || (operator == BinaryOperator.LOGICAL_OR)
            ) {
                // Clear the inferred constraints for both sides
                final List<FullConstraint> leftInferredConstraints = new LinkedList<>();
                final List<FullConstraint> rightInferredConstraints = new LinkedList<>();
                // Recursively check the sides
                final Satisfiability leftSatisfiability =
                    this.checkSubCondition(
                            binaryExpression.getLeft(),
                            toComplement,
                            leftInferredConstraints
                        );
                final Satisfiability rightSatisfiability =
                    this.checkSubCondition(
                            binaryExpression.getRight(),
                            toComplement,
                            rightInferredConstraints
                        );
                // Compute the logical operator
                final BinaryOperator operatorToTest = (
                        ((operator == BinaryOperator.LOGICAL_AND) && !toComplement) ||
                        ((operator == BinaryOperator.LOGICAL_OR) && toComplement)
                    )
                    ? BinaryOperator.LOGICAL_AND
                    : BinaryOperator.LOGICAL_OR;

                // Both sides satisfied
                if (
                    (leftSatisfiability == Satisfiability.SATISFIED) &&
                    (rightSatisfiability == Satisfiability.SATISFIED)
                ) {
                    return Satisfiability.SATISFIED;
                }
                // Both sides not satisfied
                if (
                    (leftSatisfiability == Satisfiability.NOT_SATISFIED) &&
                    (rightSatisfiability == Satisfiability.NOT_SATISFIED)
                ) {
                    return Satisfiability.NOT_SATISFIED;
                }

                // Compute the final result
                if (operatorToTest == BinaryOperator.LOGICAL_AND) {
                    // At least one side is not satisfied
                    if (
                        (leftSatisfiability == Satisfiability.NOT_SATISFIED) ||
                        (rightSatisfiability == Satisfiability.NOT_SATISFIED)
                    ) {
                        return Satisfiability.NOT_SATISFIED;
                    }
                    // At least one side has unknown satisfiability
                    if (leftSatisfiability == Satisfiability.UNKNOWN) {
                        inferredConstraints.addAll(leftInferredConstraints);
                    }
                    if (rightSatisfiability == Satisfiability.UNKNOWN) {
                        inferredConstraints.addAll(rightInferredConstraints);
                    }
                    return Satisfiability.UNKNOWN;
                }
                // Both sides are satisfied
                if (
                    (leftSatisfiability == Satisfiability.SATISFIED) ||
                    (rightSatisfiability == Satisfiability.SATISFIED)
                ) {
                    return Satisfiability.SATISFIED;
                }

                // At least one side is not satisfied
                if (leftSatisfiability == Satisfiability.NOT_SATISFIED) {
                    inferredConstraints.addAll(rightInferredConstraints);
                    return rightSatisfiability;
                }
                if (rightSatisfiability == Satisfiability.NOT_SATISFIED) {
                    inferredConstraints.addAll(leftInferredConstraints);
                    return leftSatisfiability;
                }
                return Satisfiability.UNKNOWN;
            }
            // Other binary operators
            // Clear the containers
            final FullConstraintContainer firstContainer = new FullConstraintContainer();
            final FullConstraintContainer secondContainer = new FullConstraintContainer();
            // CHeck the condition that does not contain any logical operator
            final Satisfiability result =
                this.checkConditionSatisfiability(
                        binaryExpression,
                        toComplement,
                        firstContainer,
                        secondContainer
                    );
            // Add the new inferred constraints if the condition has unknown satisfiability
            if (result == Satisfiability.UNKNOWN) {
                if (firstContainer.isValid()) {
                    inferredConstraints.add(firstContainer.getFullConstraint());
                }
                if (secondContainer.isValid()) {
                    inferredConstraints.add(secondContainer.getFullConstraint());
                }
            }
            // Return the satisfiability
            return result;
        }
        // All the other operators
        return Satisfiability.UNKNOWN;
    }

    /**
     * Checks the satisfiability of a condition in the form {@code E &lt;op&gt; F}, where {@code
     * &lt;op&gt;} is one of {@code >, <, >=, <=, ==, !=}.
     *
     * @param condition Condition to check.
     * @param toComplement {@code true} if the operators have to be complemented, {@code
     *         false} otherwise.
     * @param firstContainer First container for the inferred constraint.
     * @param secondContainer Second container for the inferred constraint.
     * @return {@link Satisfiability#SATISFIED} if the condition is proven to be always true, {@link
     *         Satisfiability#NOT_SATISFIED} if the condition is proven to be always false, {@link
     *         Satisfiability#UNKNOWN} otherwise.
     */
    @SuppressWarnings(
        {
            "NonBooleanMethodNameMayNotStartWithQuestion",
            "FeatureEnvy",
            "MethodWithMultipleReturnPoints"
        }
    )
    @NotNull
    private Satisfiability checkConditionSatisfiability(
        @NotNull final BinaryExpression condition,
        final boolean toComplement,
        @NotNull final FullConstraintContainer firstContainer,
        @NotNull final FullConstraintContainer secondContainer
    ) {
        // Check that the operator is one of >, <, >=, <=, ==, !=
        if (ConditionChecker.checkOperator(condition.getOperator())) {
            // Simplify both sides of the operator
            final Polynomial left = Simplifier.simplify(condition.getLeft(), 3);
            final Polynomial right = Simplifier.simplify(condition.getRight(), 3);
            // Compute the operator and its complement
            final BinaryOperator operator = ConditionChecker.complementOperator(
                condition.getOperator(),
                toComplement
            );
            final BinaryOperator complementedOperator = ConditionChecker.complementOperator(
                condition.getOperator(),
                !toComplement
            );
            // Check if the condition is always satisfied
            final boolean alwaysSatisfied =
                this.checkSimpleCondition(left, right, operator, firstContainer, secondContainer);
            // Check if the complemented condition is always satisfied
            //noinspection BooleanVariableAlwaysNegated
            final boolean alwaysNotSatisfied =
                this.checkSimpleCondition(left, right, complementedOperator, null, null);
            // Compute the result
            if (!alwaysSatisfied && !alwaysNotSatisfied) {
                return Satisfiability.UNKNOWN;
            }
            // Delete the inferred constraints
            firstContainer.clearConstraint();
            secondContainer.clearConstraint();
            if (alwaysSatisfied) {
                return Satisfiability.SATISFIED;
            }
            return Satisfiability.NOT_SATISFIED;
        }
        // The operator the condition uses is not supported
        return Satisfiability.UNKNOWN;
    }

    /**
     * Checks if the specified condition is proven to be always true.
     *
     * @param left The polynomial resulting from the simplification of the left side of the
     *         condition.
     * @param right The polynomial resulting from the simplification of the right side of
     *         the condition.
     * @param operator Operator of the condition.
     * @param firstContainer First container for the inferred constraint.
     * @param secondContainer Second container for the inferred constraint.
     * @return {@code true} if the specified condition is proven to be always true, {@code false}
     *         otherwise.
     */
    @SuppressWarnings({ "MethodWithMultipleReturnPoints", "OverlyComplexMethod" })
    private boolean checkSimpleCondition(
        @NotNull final Polynomial left,
        @NotNull final Polynomial right,
        @NotNull final BinaryOperator operator,
        @Nullable final FullConstraintContainer firstContainer,
        @Nullable final FullConstraintContainer secondContainer
    ) {
        // Handle the equality and inequality operators
        if (
            (operator == BinaryOperator.COMPARISON_EQ) || (operator == BinaryOperator.COMPARISON_NE)
        ) {
            // Compute the operators to use
            // Note that: a == b means a >= b && a <= b
            //     while: a != b means a > b || a < b
            final BinaryOperator operatorToUse = (operator == BinaryOperator.COMPARISON_EQ)
                ? BinaryOperator.COMPARISON_GE
                : BinaryOperator.COMPARISON_GT;
            // Verify the two sub-conditions
            final boolean first = this.checkInequality(left, right, operatorToUse, firstContainer);
            final boolean second =
                this.checkInequality(right, left, operatorToUse, secondContainer);
            // Compute the final result
            if (operator == BinaryOperator.COMPARISON_EQ) {
                return first && second;
            }
            // Clear the containers in the case of inequality
            if (
                (firstContainer != null) &&
                firstContainer.isValid() &&
                (secondContainer != null) &&
                secondContainer.isValid()
            ) {
                firstContainer.clearConstraint();
                secondContainer.clearConstraint();
            }
            return first || second;
        }
        // Handle the other comparison operators
        return this.checkInequality(left, right, operator, firstContainer);
    }

    /**
     * Checks if the specified condition is proven to be always true. This method handles only
     * {@code >, <, >=, <=} operators.
     *
     * @param left The polynomial resulting from the simplification of the left side of the
     *         condition.
     * @param right The polynomial resulting from the simplification of the right side of *
     *         the condition.
     * @param operator Operator of the condition.
     * @param constraintContainer Container that will contain the constraint extracted from
     *         the condition is it is not proven to be always true.
     * @return {@code true} if the specified condition is proven to be always true, {@code false}
     *         otherwise.
     */
    private boolean checkInequality(
        @NotNull Polynomial left,
        @NotNull Polynomial right,
        @NotNull BinaryOperator operator,
        @Nullable final FullConstraintContainer constraintContainer
    ) {
        // Turn <= and < into >= and > respectively
        if (
            (operator == BinaryOperator.COMPARISON_LE) || (operator == BinaryOperator.COMPARISON_LT)
        ) {
            // Swap the sides
            final Polynomial temp = left;
            left = right;
            right = temp;
            operator =
                (operator == BinaryOperator.COMPARISON_LE)
                    ? BinaryOperator.COMPARISON_GE
                    : BinaryOperator.COMPARISON_GT;
        }
        // Turn >= into >
        if (operator == BinaryOperator.COMPARISON_GE) {
            // Subtract one to the right side
            right = right.subtract(new PolynomialBuilder(3).setConstantCoefficient(1).build());
        }
        // Check the condition
        return this.checkPolynomial(left.subtract(right), constraintContainer);
    }

    /**
     * Checks if the condition {@code polynomialToCheck > 0} is always true or not.
     *
     * @param polynomialToCheck Polynomial to check.
     * @param constraintContainer Container that will contain the constraint extracted from
     *         the condition is it is not proven to be always true.
     * @return {@code true} if the specified condition is proven to be always true, {@code false}
     *         otherwise.
     */
    @SuppressWarnings({ "FeatureEnvy", "OverlyComplexMethod", "MethodWithMultipleReturnPoints" })
    private boolean checkPolynomial(
        @NotNull final Polynomial polynomialToCheck,
        @Nullable final FullConstraintContainer constraintContainer
    ) {
        // Invalid polynomials cannot be verified
        if (!polynomialToCheck.isValid() || (polynomialToCheck.getSize() < 2)) {
            if (constraintContainer != null) {
                constraintContainer.clearConstraint();
            }
            return false;
        }
        // Normalize the polynomial
        final Polynomial normalizedPolynomial = ConditionChecker.normalizePolynomial(
            polynomialToCheck
        );
        // Extract the monomials
        final Monomial aMonomial = normalizedPolynomial.getMonomial(0);
        final Monomial bMonomial = normalizedPolynomial.getMonomial(1);
        @Nullable
        final Monomial cMonomial = (normalizedPolynomial.getSize() == 3)
            ? normalizedPolynomial.getMonomial(2)
            : null;
        // Extract the variables and the coefficients
        final Variable aVariable = aMonomial.getVariable();
        final Variable bVariable = bMonomial.getVariable();
        @Nullable
        final Variable cVariable = (cMonomial == null) ? null : cMonomial.getVariable();
        final int aCoefficient = aMonomial.getCoefficient();
        final int bCoefficient = bMonomial.getCoefficient();
        final int cCoefficient = (cMonomial == null) ? 0 : cMonomial.getCoefficient();
        final int k2 = -normalizedPolynomial.getConstantCoefficient();
        // Clear the constraints to check
        final List<FullConstraint> constraintsToCheck = new LinkedList<>();
        // Try to understand what are the variables x, y and z
        // Variable x is the first monomial
        if ((aCoefficient == 1) && ((bCoefficient == cCoefficient) || (cMonomial == null))) {
            constraintsToCheck.add(
                new FullConstraint(aVariable, bVariable, cVariable, -bCoefficient, k2)
            );
        }
        // Variable x is the second monomial
        if ((bCoefficient == 1) && ((aCoefficient == cCoefficient) || (cMonomial == null))) {
            constraintsToCheck.add(
                new FullConstraint(bVariable, aVariable, cVariable, -aCoefficient, k2)
            );
        }
        // Variable x is the third monomial
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
        }
        // Infer a new constraint only if one constraint needed to be checked
        if (constraintContainer != null) {
            if (constraintsToCheck.size() == 1) {
                constraintContainer.setFullConstraint(constraintsToCheck.get(0));
            } else {
                constraintContainer.clearConstraint();
            }
        }
        return false;
    }

    /**
     * Checks if there exists a constraint {@code (x, y, k}<sub>{@code 1}</sub>{@code ,
     * k}<sub>{@code 2}</sub> {@code )} among the tracked ones which tells that the specified
     * constraint {@code (x, y, k}<sub>{@code 1}</sub>{@code , k}<sub>{@code 3}</sub> {@code )} is
     * always true, i.e., such that {@code k}<sub>{@code 3}</sub> <= {@code k}<sub>{@code 2}</sub>.
     *
     * @param constraintToCheck Constraint to check.
     * @return {@code true} is such constraint exists, {@code false} otherwise
     */
    @SuppressWarnings("FeatureEnvy")
    private boolean checkConstraint(@NotNull final FullConstraint constraintToCheck) {
        // Extract the constraints associated to x
        final Set<@NotNull Constraint> constraints =
            this.trackedConstraints.get(constraintToCheck.getX());
        // Check the presence of the constraint
        return (
            (constraints != null) &&
            constraints
                .stream()
                .anyMatch(
                    constraint ->
                        constraintToCheck.differsOnlyOnK2(constraint) &&
                        (constraintToCheck.getK2() <= constraint.getK2())
                )
        );
    }

    /**
     * Utility method that normalizes a polynomial by dividing all the coefficients with their gcd.
     *
     * @param polynomialToNormalize Polynomial to normalize.
     * @return The normalized polynomial.
     */
    @SuppressWarnings("FeatureEnvy")
    @NotNull
    private static Polynomial normalizePolynomial(@NotNull final Polynomial polynomialToNormalize) {
        // Extract the coefficients
        final int aCoefficient = Math.abs(polynomialToNormalize.getMonomial(0).getCoefficient());
        final int bCoefficient = Math.abs(polynomialToNormalize.getMonomial(1).getCoefficient());
        final int cCoefficient = (polynomialToNormalize.getSize() == 3)
            ? Math.abs(polynomialToNormalize.getMonomial(1).getCoefficient())
            : 0;
        final int constant = Math.abs(polynomialToNormalize.getConstantCoefficient());
        // Compute the gcd
        int gcd = ConditionChecker.greatestCommonDivisor(aCoefficient, bCoefficient);
        if (cCoefficient != 0) {
            gcd = ConditionChecker.greatestCommonDivisor(cCoefficient, gcd);
        }
        gcd = ConditionChecker.greatestCommonDivisor(constant, gcd);
        // Divide the polynomial by the gcd
        return polynomialToNormalize.divide(gcd);
    }

    /**
     * Utility method that checks if the specified binary operator is a comparison operator.
     *
     * @param operator Operator to check.
     * @return {@code true} if the specified operator is a comparison operator, {@code false}
     *         otherwise.
     */
    private static boolean checkOperator(@NotNull final BinaryOperator operator) {
        // CHeck the operator
        return (
            (operator == BinaryOperator.COMPARISON_EQ) ||
            (operator == BinaryOperator.COMPARISON_NE) ||
            (operator == BinaryOperator.COMPARISON_GT) ||
            (operator == BinaryOperator.COMPARISON_GE) ||
            (operator == BinaryOperator.COMPARISON_LT) ||
            (operator == BinaryOperator.COMPARISON_LE)
        );
    }

    /**
     * Utility method that computes the complement of an operator.
     *
     * @param operator Operator to complement.
     * @param toComplement {@code true} if the operator needs to be complemented, {@code
     *         false} otherwise.
     * @return The complemented operator.
     */
    @NotNull
    private static BinaryOperator complementOperator(
        @NotNull final BinaryOperator operator,
        final boolean toComplement
    ) {
        if (!toComplement) {
            return operator;
        }
        // Complement the operator
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

    /**
     * Utility method that computes the gcd between two numbers.
     *
     * @param firstNumber First number.
     * @param secondNumber Second number.
     * @return The computed gcd.
     */
    private static int greatestCommonDivisor(int firstNumber, int secondNumber) {
        // Clear the result
        int result = firstNumber;
        // Compute the gcd
        do {
            final int modulo = firstNumber % secondNumber;
            if (modulo == 0) {
                result = secondNumber;
            } else {
                firstNumber = secondNumber;
                secondNumber = modulo;
            }
        } while (result > 1);

        return result;
    }
}
