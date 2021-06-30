package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Monomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Polynomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.PolynomialBuilder;
import it.unive.lisa.symbolic.value.Variable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Utility class that infers new constraints on assignments and conditions.
 *
 * @author Alessio De Biasi
 * @author Jonathan Gobbo
 * @version 1.1 2021-06-30
 * @since 1.5 2021-04-17
 */
final class ConstraintsComputer {

    /**
     * Utility class that represents a pair {@code (boolean, T)}.
     *
     * @param <T> The type of the non-boolean element.
     * @author Alessio De Biasi
     * @author Jonathan Gobbo
     * @version 1.1 2021-06-30
     * @since 1.5 2021-04-17
     */
    private static final class BooleanPair<T> {

        /**
         * First element of the pair.
         */
        private final boolean first;

        /**
         * Second element of the pair.
         */
        @NotNull
        private final T second;

        /**
         * Creates a new pair {@code (boolean, T)}.
         *
         * @param first Boolean value.
         * @param second Non-boolean value.
         */
        private BooleanPair(final boolean first, final @NotNull T second) {
            this.second = second;
            this.first = first;
        }

        /**
         * Returns the boolean value.
         *
         * @return The boolean value.
         */
        @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
        private boolean getFirst() {
            return this.first;
        }

        /**
         * Returns the non-boolean value.
         *
         * @return The non-boolean value.
         */
        @NotNull
        private T getSecond() {
            return this.second;
        }

        /**
         * Creates a string representation of this pair.
         *
         * @return A string representation of this pair.
         */
        @Override
        @NotNull
        public String toString() {
            return "[" + this.second + ", " + ((this.first) ? "=" : ">") + ']';
        }
    }

    /**
     * Map that constants the currently tracked variables and their constraints.
     */
    @NotNull
    @Unmodifiable
    private final Map<@NotNull Variable, @Unmodifiable @NotNull Set<@NotNull Constraint>> trackedConstraints;

    /**
     * Creates a new object that infers new constraints on assignments or conditions based on the
     * specified tracked constraints.
     *
     * @param trackedConstraints The currently tracked constraints.
     */
    ConstraintsComputer(
        @NotNull @Unmodifiable final Map<@NotNull Variable, @Unmodifiable @NotNull Set<@NotNull Constraint>> trackedConstraints
    ) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.trackedConstraints = trackedConstraints;
    }

    /**
     * Infers new constraints for assignments.
     *
     * @param x Variable being assigned.
     * @param firstMonomial First monomial of the assigned expression.
     * @param secondMonomial Second monomial of the assigned expression.
     * @param constant Constant value of the assigned expression.
     * @return The inferred constraints.
     */
    @SuppressWarnings("FeatureEnvy")
    @NotNull
    List<@NotNull FullConstraint> inferNewAssignmentConstraints(
        @NotNull final Variable x,
        @NotNull final Monomial firstMonomial,
        @Nullable final Monomial secondMonomial,
        final int constant
    ) { // d+e --> 1*(d+e)
        // -d-e
        // Clear the result
        final List<@NotNull FullConstraint> newConstraints = new LinkedList<>();
        // Extract variables and coefficients
        final Variable firstVariable = firstMonomial.getVariable();
        final int firstCoefficient = firstMonomial.getCoefficient();
        final Variable secondVariable = (secondMonomial == null)
            ? null
            : secondMonomial.getVariable();
        final int secondCoefficient = (secondMonomial == null)
            ? 0
            : secondMonomial.getCoefficient();

        // CHeck if the assignment is correct
        if ((firstCoefficient == secondCoefficient) || (secondMonomial == null)) {
            // Flip the sides of the assignment
            if (secondMonomial == null) {
                if (firstCoefficient == 1) {
                    //noinspection SuspiciousNameCombination
                    newConstraints.add(
                        new FullConstraint(firstVariable, x, null, 1, -constant - 1)
                    );
                } else if (firstCoefficient == -1) {
                    newConstraints.add(
                        new FullConstraint(firstVariable, x, null, -1, constant - 1)
                    );
                }
            }
            // Infer new constraints for inequality chains
            newConstraints.addAll(
                this.computeInequalityChains(
                        x,
                        firstVariable,
                        secondVariable,
                        firstCoefficient,
                        constant,
                        true
                    )
            );
            // Infer new constraints for expression substitution
            newConstraints.addAll(
                this.computeExpressionSubstitution(
                        x,
                        firstVariable,
                        secondVariable,
                        firstCoefficient,
                        constant
                    )
            );
        }

        return newConstraints;
    }

    /**
     * Infers new constraints for conditions.
     *
     * @param conditionConstraints Constraints extracted from the evaluation of the
     *         condition.
     * @return The inferred constraints.
     */
    @SuppressWarnings("FeatureEnvy")
    @NotNull
    List<FullConstraint> inferNewConditionConstraints(
        @NotNull @Unmodifiable final Iterable<@NotNull FullConstraint> conditionConstraints
    ) {
        // Clear the result
        final List<@NotNull FullConstraint> result = new LinkedList<>();

        // Try to infer new constraints
        for (final FullConstraint conditionConstraint : conditionConstraints) {
            // Infer inequality chains
            result.addAll(
                this.computeInequalityChains(
                        conditionConstraint.getX(),
                        conditionConstraint.getY(),
                        conditionConstraint.getZ(),
                        conditionConstraint.getK1(),
                        conditionConstraint.getK2(),
                        false
                    )
            );
        }

        return result;
    }

    /**
     * Infer new constraints by:
     * <ul>
     *     <li>Substituting the assigned expression with multiples of the expressions assigned to
     *     other variables;</li>
     *     <li>Substituting multiples of the assigned expression on expressions assigned to other
     *     variables.</li>
     * </ul>
     *
     * @param x Variable {@code x} of the normalized form.
     * @param y Variable {@code y} of the normalized form.
     * @param z Variable {@code z} of the normalized form.
     * @param k1 Constant {@code k}<sub>{@code 1}</sub> of the normalized form.
     * @param k2 Constant {@code k}<sub>{@code 2}</sub> of the normalized form.
     * @return The inferred constraints.
     */
    @SuppressWarnings(
        { "ObjectAllocationInLoop", "FeatureEnvy", "OverlyComplexMethod", "OverlyNestedMethod" }
    )
    @NotNull
    private List<@NotNull FullConstraint> computeExpressionSubstitution(
        @NotNull final Variable x,
        @NotNull final Variable y,
        @Nullable final Variable z,
        final int k1,
        final int k2
    ) {
        // Clear the result
        final List<@NotNull FullConstraint> result = new LinkedList<>();
        // Loop over the tracked variables
        for (final Entry<@NotNull Variable, @Unmodifiable Set<@NotNull Constraint>> entry : this.trackedConstraints.entrySet()) {
            // Loop over the associated constraints
            for (final Constraint otherConstraint : entry.getValue()) {
                final int otherConstraintK1 = otherConstraint.getK1();
                final int otherConstraintK2 = otherConstraint.getK2() + 1;
                // Check if the variable z is present in the expression
                if (z == null) {
                    if (k1 == 1) {
                        if (otherConstraint.getY().equals(y)) {
                            // Substitute the assigned variables in assignments of the same
                            // expression
                            //noinspection SuspiciousNameCombination
                            result.add(
                                new FullConstraint(
                                    entry.getKey(),
                                    x,
                                    otherConstraint.getZ(),
                                    otherConstraintK1,
                                    otherConstraintK2 - (otherConstraintK1 * k2) - 1
                                )
                            );
                            // Substitute the expression on other assignments
                            if ((otherConstraintK1 == 1) || (otherConstraintK1 == -1)) {
                                result.add(
                                    new FullConstraint(
                                        x,
                                        entry.getKey(),
                                        otherConstraint.getZ(),
                                        otherConstraintK1,
                                        k2 - (otherConstraintK1 * otherConstraintK2) - 1
                                    )
                                );
                            }
                        }
                        // Substitute y with the z variable on the constraint
                        if (y.equals(otherConstraint.getZ())) {
                            result.add(
                                new FullConstraint(
                                    entry.getKey(),
                                    otherConstraint.getY(),
                                    y,
                                    otherConstraintK1,
                                    (otherConstraintK2 + (otherConstraintK1 * k2)) - 1
                                )
                            );
                        }
                    } else if (otherConstraint.getZ() == null) {
                        // The variable z is not present in the constraint being analyzed
                        // Check that variables y are the same
                        if (otherConstraint.getY().equals(y)) {
                            this.substituteOnExpression(
                                    x,
                                    entry.getKey(),
                                    k1,
                                    otherConstraintK1,
                                    k2,
                                    otherConstraintK2,
                                    otherConstraint,
                                    result
                                );
                        }
                    }
                } else if (
                    (y.equals(otherConstraint.getY()) && z.equals(otherConstraint.getZ())) ||
                    (z.equals(otherConstraint.getY()) && y.equals(otherConstraint.getZ()))
                ) {
                    this.substituteOnExpression(
                            x,
                            entry.getKey(),
                            k1,
                            otherConstraintK1,
                            k2,
                            otherConstraintK2,
                            otherConstraint,
                            result
                        );
                }
            }
        }

        return result;
    }

    /**
     * Adds new constraints by:
     * <ul>
     *     <li>Substituting the assigned expression with multiples of the expressions
     *           assigned to other variables;</li>
     *     <li>Substituting multiples of the assigned expression on expressions assigned to
     *           other variables.</li>
     * </ul>
     *
     * @param x Variable {@code x} of the normalized form.
     * @param otherConstraintX Variable {@code x} of the other constraint.
     * @param k1 Constant {@code k}<sub>{@code 1}</sub> of the normalized form.
     * @param otherConstraintK1 Constant {@code k}<sub>{@code 1}</sub> of the other
     *         constraint.
     * @param k2 Constant {@code k}<sub>{@code 2}</sub> of the normalized form.
     * @param otherConstraintK2 Constant {@code k}<sub>{@code 2}</sub> of the other
     *         constraint.
     * @param otherConstraint The other constraint.
     * @param result The collection the new constraints will be added.
     */
    @SuppressWarnings({ "MethodWithTooManyParameters", "SuspiciousNameCombination" })
    private void substituteOnExpression(
        @NotNull final Variable x,
        @NotNull final Variable otherConstraintX,
        final int k1,
        final int otherConstraintK1,
        final int k2,
        final int otherConstraintK2,
        @NotNull final Constraint otherConstraint,
        final @NotNull Collection<? super @NotNull FullConstraint> result
    ) {
        // The constraint is a multiple of a previous one
        if ((otherConstraintK1 % k1) == 0) {
            result.add(
                new FullConstraint(
                    otherConstraintX,
                    x,
                    null,
                    otherConstraintK1 / k1,
                    otherConstraintK2 - ((otherConstraintK1 / k1) * k2) - 1
                )
            );
        }
        // A previous constraint is a multiple of the new one
        if (
            ((k1 % otherConstraintK1) == 0) && this.isAnEquality(otherConstraintX, otherConstraint)
        ) {
            result.add(
                new FullConstraint(
                    x,
                    otherConstraintX,
                    null,
                    k1 / otherConstraint.getK1(),
                    k2 - ((k1 / otherConstraintK1) * otherConstraintK2) - 1
                )
            );
        }
    }

    /**
     * Computes new constraints following the tracked inequalities.
     *
     * @param x Variable {@code x} of the normalized form.
     * @param y Variable {@code y} of the normalized form.
     * @param z Variable {@code z} of the normalized form.
     * @param k1 Constant {@code k}<sub>{@code 1}</sub> of the normalized form.
     * @param k2 Constant {@code k}<sub>{@code 2}</sub> of the normalized form.
     * @param isAnEquality {@code true} if the normalized form can be refined by changing
     *         the {@code >} sign with the {@code =} one.
     * @return The inferred constraints.
     */
    @SuppressWarnings("FeatureEnvy")
    @NotNull
    private List<@NotNull FullConstraint> computeInequalityChains(
        @NotNull final Variable x,
        @NotNull final Variable y,
        @Nullable final Variable z,
        final int k1,
        final int k2,
        final boolean isAnEquality
    ) {
        // Queue of the inequalities to explore
        final Queue<@NotNull BooleanPair<@NotNull FullConstraint>> constraintsToExplore = new LinkedList<>();
        // Add the initial constraint
        constraintsToExplore.add(
            new BooleanPair<>(isAnEquality, new FullConstraint(x, y, z, k1, k2))
        );
        // Clear the result
        final List<@NotNull FullConstraint> inferredConstraints = new LinkedList<>();
        // loop over all the parts of the chain
        while (!constraintsToExplore.isEmpty()) {
            // Extract the next part of the chain
            final BooleanPair<@NotNull FullConstraint> next = constraintsToExplore.remove();
            // Extract its components
            final boolean isEqualitySign = next.getFirst();
            final FullConstraint nextConstraint = next.getSecond();
            final Variable nextX = nextConstraint.getX();
            final Variable nextY = nextConstraint.getY();
            final Variable nextZ = nextConstraint.getZ();
            final int nextK1 = nextConstraint.getK1();
            final int nextK2 = nextConstraint.getK2();

            // Substitute variable y of the normalized form with its constraints
            this.substituteVariables(
                    nextX,
                    nextY,
                    nextZ,
                    false,
                    nextK1,
                    nextK2,
                    isEqualitySign,
                    constraintsToExplore,
                    inferredConstraints
                );
            // Substitute variable z of the normalized form with its constraints
            this.substituteVariables(
                    nextX,
                    nextZ,
                    nextY,
                    false,
                    nextK1,
                    nextK2,
                    isEqualitySign,
                    constraintsToExplore,
                    inferredConstraints
                );
            // Substitute variables y and z of the normalized form with their constraints
            this.substituteVariables(
                    nextX,
                    nextY,
                    nextZ,
                    true,
                    nextK1,
                    nextK2,
                    isEqualitySign,
                    constraintsToExplore,
                    inferredConstraints
                );
        }

        return inferredConstraints;
    }

    /**
     * Substitute variable y and optionally z with their constraints.
     *
     * @param x Variable {@code x} of the normalized form.
     * @param y Variable {@code y} of the normalized form.
     * @param z Variable {@code z} of the normalized form.
     * @param substituteZ {@code true} if {@code z} will be substituted with its
     *         constraints, {@code false} otherwise.
     * @param k1 Constant {@code k}<sub>{@code 1}</sub> of the normalized form.
     * @param k2 Constant {@code k}<sub>{@code 2}</sub> of the normalized form.
     * @param isAnEquality {@code true} if the normalized form can be refined by changing
     *         the {@code >} sign with the {@code =} one.
     * @param constraintsToExplore Collection where the inferred constraints will be added
     *         in order to be explored as part of the inequality chain.
     * @param inferredConstraints Collection where the inferred constraints will be added.
     */
    @SuppressWarnings({ "FeatureEnvy", "MethodWithTooManyParameters", "OverlyComplexMethod" })
    private void substituteVariables(
        @NotNull final Variable x,
        @Nullable final Variable y,
        @Nullable final Variable z,
        final boolean substituteZ,
        final int k1,
        final int k2,
        final boolean isAnEquality,
        final @NotNull Collection<? super BooleanPair<FullConstraint>> constraintsToExplore,
        final @NotNull Collection<? super FullConstraint> inferredConstraints
    ) {
        // Extract the constraints associated to y
        @Unmodifiable
        final Set<@NotNull Constraint> yConstraints = this.trackedConstraints.get(y);
        if ((yConstraints != null) && (y != null)) {
            // Create the polynomial x-k2
            final PolynomialBuilder basePolynomialBuilder = new PolynomialBuilder(3)
                .addMonomial(1, x)
                .setConstantCoefficient(-k2);
            // Add z if it is not to substitute
            if (!substituteZ || (this.trackedConstraints.get(z) == null)) {
                basePolynomialBuilder.addMonomial(-k1, z);
            }
            // Build the base polynomial where the substituted variables will be summed
            final Polynomial basePolynomial = basePolynomialBuilder.build();
            // Check what are the variables to substitute
            if ((z == null) || (this.trackedConstraints.get(z) == null) || !substituteZ) {
                // Loop over the constraints associated to y
                for (final Constraint yConstraint : yConstraints) {
                    // Substitute only y
                    this.polynomialSubstitution(
                            x,
                            y,
                            z,
                            yConstraint,
                            null,
                            k1,
                            isAnEquality,
                            basePolynomial,
                            constraintsToExplore,
                            inferredConstraints
                        );
                }
            } else {
                // Get the constraints associated to z
                @Unmodifiable
                final Set<@NotNull Constraint> zConstraints = this.trackedConstraints.get(z);
                // Loop over the constraints associated to y
                for (final Constraint yConstraint : yConstraints) {
                    // Loop over the constraints associated to z
                    for (final Constraint zConstraint : zConstraints) {
                        // Substitute y and z
                        this.polynomialSubstitution(
                                x,
                                y,
                                z,
                                yConstraint,
                                zConstraint,
                                k1,
                                isAnEquality,
                                basePolynomial,
                                constraintsToExplore,
                                inferredConstraints
                            );
                    }
                }
            }
        }
    }

    /**
     * Substitutes variable y and optional z with one of their constraints and checks if the
     * resulting polynomial is still in normalized form.
     *
     * @param x Variable {@code x} of the normalized form.
     * @param yConstraint The constraint to substitute to variable {@code y} of the
     *         normalized form.
     * @param zConstraint The constraint to substitute to variable {@code z} of the
     *         normalized form.
     * @param k1 Constant {@code k}<sub>{@code 1}</sub> of the normalized form.
     * @param isAnEquality {@code true} if the normalized form can be refined by changing
     *         the {@code >} sign with the {@code =} one.
     * @param basePolynomial The polynomial the variable-to-constraint substitution will be
     *         summed.
     * @param constraintsToExplore Collection where the inferred constraints will be added
     *         in order to be explored as part of the inequality chain.
     * @param inferredConstraints Collection where the inferred constraints will be added.
     */
    @SuppressWarnings({ "FeatureEnvy", "OverlyComplexMethod", "MethodWithTooManyParameters" })
    private void polynomialSubstitution(
        @NotNull final Variable x,
        @NotNull final Variable y,
        @Nullable final Variable z,
        @NotNull final Constraint yConstraint,
        @Nullable final Constraint zConstraint,
        final int k1,
        final boolean isAnEquality,
        @NotNull final Polynomial basePolynomial,
        final @NotNull Collection<@NotNull ? super BooleanPair<@NotNull FullConstraint>> constraintsToExplore,
        final @NotNull Collection<@NotNull ? super FullConstraint> inferredConstraints
    ) {
        if (k1 < 0 && !isAnEquality) {
            return;
        }
        // Build the polynomial from the constraint to substitute in place of y
        //noinspection SuspiciousNameCombination
        final BooleanPair<@NotNull Polynomial> yPolynomial =
            this.buildPolynomialFromConstraint(y, yConstraint);
        final BooleanPair<@NotNull Polynomial> zPolynomial;
        // Build the polynomial from the constraint to substitute in place of z
        if ((zConstraint == null) || (z == null)) {
            // Polynomial 0
            zPolynomial =
                new BooleanPair<>(true, new PolynomialBuilder(3).setConstantCoefficient(0).build());
        } else {
            zPolynomial = this.buildPolynomialFromConstraint(z, zConstraint);
        }
        if (k1 < 0 && (!yPolynomial.getFirst() || !zPolynomial.getFirst())) {
            return;
        }
        // Create the final polynomial
        final Polynomial result = basePolynomial
            .subtract(yPolynomial.getSecond().multiply(k1))
            .subtract(zPolynomial.getSecond().multiply(k1));

        // Check if the result is still normalized
        if (result.isValid() && ((result.getSize() == 2) || (result.getSize() == 3))) {
            // Extract the monomials and the constant
            final Monomial firstMonomial = result.getMonomial(0);
            final Monomial secondMonomial = result.getMonomial(1);
            @Nullable
            final Monomial thirdMonomial = (result.getSize() == 3) ? result.getMonomial(2) : null;
            final int constantCoefficient = result.getConstantCoefficient();
            // Check if the result can be refined
            final boolean isResultAnEquality =
                isAnEquality && yPolynomial.getFirst() && zPolynomial.getFirst();
            // Infer new constraints based on where variable x is
            if (firstMonomial.getVariable().equals(x)) {
                // The first monomial is the variable x
                ConstraintsComputer.extractConstraints(
                    x,
                    firstMonomial,
                    secondMonomial,
                    thirdMonomial,
                    constantCoefficient,
                    isResultAnEquality,
                    constraintsToExplore,
                    inferredConstraints
                );
            } else if (secondMonomial.getVariable().equals(x)) {
                // The second monomial is the variable x
                ConstraintsComputer.extractConstraints(
                    x,
                    secondMonomial,
                    firstMonomial,
                    thirdMonomial,
                    constantCoefficient,
                    isResultAnEquality,
                    constraintsToExplore,
                    inferredConstraints
                );
            } else if ((thirdMonomial != null) && thirdMonomial.getVariable().equals(x)) {
                // The third monomial is the variable x
                ConstraintsComputer.extractConstraints(
                    x,
                    thirdMonomial,
                    firstMonomial,
                    secondMonomial,
                    constantCoefficient,
                    isResultAnEquality,
                    constraintsToExplore,
                    inferredConstraints
                );
            }
        }
    }

    /**
     * Infers new constraints from the specified monomials.
     *
     * @param x The variable {@code x} the new constraint will be associated to.
     * @param firstMonomial First monomial.
     * @param secondMonomial Second monomial.
     * @param thirdMonomial Third monomial.
     * @param constantCoefficient Constant value.
     * @param isAnEquality {@code true} if the normalized form can be refined by changing
     *         the {@code >} sign with the {@code =} one.
     * @param constraintsToExplore Collection where the inferred constraints will be added
     *         in order to be explored as part of the inequality chain.
     * @param inferredConstraints Collection where the inferred constraints will be added.
     */
    @SuppressWarnings({ "FeatureEnvy", "MethodWithTooManyParameters" })
    private static void extractConstraints(
        @NotNull final Variable x,
        @NotNull final Monomial firstMonomial,
        @NotNull final Monomial secondMonomial,
        @Nullable final Monomial thirdMonomial,
        final int constantCoefficient,
        final boolean isAnEquality,
        final @NotNull Collection<@NotNull ? super BooleanPair<FullConstraint>> constraintsToExplore,
        final @NotNull Collection<@NotNull ? super FullConstraint> inferredConstraints
    ) {
        // Check if the coefficients are correct
        if (
            (firstMonomial.getCoefficient() == 1) &&
            (
                (thirdMonomial == null) ||
                (secondMonomial.getCoefficient() == thirdMonomial.getCoefficient())
            )
        ) {
            // Avoid loops due to equalities
            if (!firstMonomial.getVariable().equals(x)) {
                // Add the new constraint to the ones to explore in order to continue the chain
                constraintsToExplore.add(
                    new BooleanPair<>(
                        isAnEquality,
                        new FullConstraint(
                            firstMonomial.getVariable(),
                            secondMonomial.getVariable(),
                            (thirdMonomial == null) ? null : thirdMonomial.getVariable(),
                            -secondMonomial.getCoefficient(),
                            -constantCoefficient
                        )
                    )
                );
            }

            // Add the inferred constraint properly refined
            inferredConstraints.add(
                new FullConstraint(
                    firstMonomial.getVariable(),
                    secondMonomial.getVariable(),
                    (thirdMonomial == null) ? null : thirdMonomial.getVariable(),
                    -secondMonomial.getCoefficient(),
                    (isAnEquality) ? (-constantCoefficient - 1) : -constantCoefficient
                )
            );
        }
    }

    /**
     * Utility method that build a polynomial from the specified constraint.
     *
     * @param x The variable the constraint is associated to.
     * @param constraint Constraint to build the polynomial from.
     * @return A boolean pair where:
     *         <ul>
     *             <li>The boolean value is {@code true} if variable {@code x} is equal to the
     *             constraint, {@code false} if variable {@code x} is simply greater than the
     *             constraint;</li>
     *             <li>The non-boolean value is the polynomial.</li>
     *         </ul>
     */
    @SuppressWarnings("FeatureEnvy")
    @NotNull
    private BooleanPair<@NotNull Polynomial> buildPolynomialFromConstraint(
        @NotNull final Variable x,
        @NotNull final Constraint constraint
    ) {
        boolean isAnEquality = isAnEquality(x, constraint);
        // Build the polynomial
        return new BooleanPair<>(
            isAnEquality,
            new PolynomialBuilder(3)
                .addMonomial(constraint.getK1(), constraint.getY())
                .addMonomial(constraint.getK1(), constraint.getZ())
                .setConstantCoefficient(constraint.getK2() + ((isAnEquality) ? 1 : 0))
                .build()
        );
    }

    private boolean isAnEquality(@NotNull Variable x, @NotNull Constraint constraint) {
        // Check if the constraint refers to an equality
        boolean isAnEquality = false;
        if (
            ((constraint.getK1() == 1) || (constraint.getK1() == -1)) && (constraint.getZ() == null)
        ) {
            // Compute the original constant
            final int originalConstant = (constraint.getK1() == 1)
                ? (-(constraint.getK2() + 1) - 1)
                : ((-constraint.getK2() - 1) + 1);
            // Extract the constraints associated to y
            @Unmodifiable
            final Set<@NotNull Constraint> constraintsToCheck =
                this.trackedConstraints.get(constraint.getY());
            if (constraintsToCheck != null) {
                // Check if there exists a constraint that mention x with the opposite constant
                isAnEquality =
                    constraintsToCheck
                        .stream()
                        .anyMatch(
                            constraintToCheck ->
                                constraintToCheck.getY().equals(x) &&
                                (constraintToCheck.getK2() == originalConstant)
                        );
            }
        }
        return isAnEquality;
    }
}
