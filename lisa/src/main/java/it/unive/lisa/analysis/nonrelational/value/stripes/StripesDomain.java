package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Monomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Polynomial;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Return;
import it.unive.lisa.symbolic.value.HeapLocation;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.Variable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Class that implements the Stripes abstract domain.
 *
 * @author Alessio De Biasi
 * @author Jonathan Gobbo
 * @version 1.5.1 2021-06-30
 * @since 1.5 2021-04-17
 */
public class StripesDomain
    extends BaseLattice<StripesDomain>
    implements ValueDomain<StripesDomain> {

    /**
     * Top domain element.
     */
    @NotNull
    private static final StripesDomain TOP_ELEMENT = new StripesDomain();

    /**
     * Bottom domain element.
     */
    @NotNull
    private static final StripesDomain BOTTOM_ELEMENT = new StripesDomain();

    /**
     * Map that contains all the tracked variables associated to their constraints.
     */
    @NotNull
    @Unmodifiable
    private final Map<@NotNull Variable,
            @Unmodifiable @NotNull Set<@NotNull Constraint>> trackedConstraints;

    /**
     * Creates an empty abstract domain. This constructor is used to instantiate the domain and run
     * the analysis.
     */
    public StripesDomain() {
        this(new HashMap<>());
    }

    /**
     * Creates a new abstract domain with the specified constraints.
     *
     * @param trackedConstraints The constraints to track.
     */
    private StripesDomain(
        @Unmodifiable @NotNull final Map<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> trackedConstraints
    ) {
        // Save the map
        this.trackedConstraints = trackedConstraints;
    }

    /**
     * Returns the top element.
     *
     * @return The top element.
     */
    @Override
    @NotNull
    public StripesDomain top() {
        return StripesDomain.TOP_ELEMENT;
    }

    /**
     * Check if this domain element is the top element.
     *
     * @return {@code true} if this element is the top element, {@code false} otherwise.
     */
    @Override
    public boolean isTop() {
        //noinspection ObjectEquality
        return this.trackedConstraints.isEmpty() && (this != StripesDomain.BOTTOM_ELEMENT);
    }

    /**
     * Returns the bottom element.
     *
     * @return The bottom element.
     */
    @Override
    @NotNull
    public StripesDomain bottom() {
        return StripesDomain.BOTTOM_ELEMENT;
    }

    /**
     * Check if this domain element is the bottom element.
     *
     * @return {@code true} if this element is the bottom element, {@code false} otherwise.
     */
    @Override
    public boolean isBottom() {
        //noinspection ObjectEquality
        return this == StripesDomain.BOTTOM_ELEMENT;
    }

    /**
     * Performs the least upper bound operation between this domain element and the specified one.
     *
     * @param other The other lattice element.
     * @return The least upper bound between this domain element and the specified one.
     */
    @SuppressWarnings("FeatureEnvy")
    @Override
    @NotNull
    protected StripesDomain lubAux(final StripesDomain other) {
        // Clear the result
        final Map<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> newDomainElements =
                new HashMap<>();

        // Loop over the tracked variables
        for (final Entry<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> element :
                this.trackedConstraints.entrySet()) {
            // Extract the constraints
            @Unmodifiable
            final Set<@NotNull Constraint> elementConstraints = element.getValue();
            // Get the constraints defined on the same variable on the other element
            @Unmodifiable
            final Set<@NotNull Constraint> otherConstraints = other.trackedConstraints.getOrDefault(
                element.getKey(),
                Collections.emptySet()
            );

            // Merge the constraints
            //noinspection ObjectAllocationInLoop
            final Set<@NotNull Constraint> newConstraints = new HashSet<>();
            // Loop over the constraints associated to the variable in this domain element
            for (final Constraint elementConstraint : elementConstraints) {
                // Loop over the constraints associated to the variable in this domain element
                for (final Constraint otherConstraint : otherConstraints) {
                    // Keep the wider constraint
                    if (elementConstraint.differsOnlyOnK2(otherConstraint)) {
                        if (elementConstraint.getK2() < otherConstraint.getK2()) {
                            newConstraints.add(elementConstraint);
                        } else {
                            newConstraints.add(otherConstraint);
                        }
                        break;
                    }
                }
            }
            // Add the new constraints, if there are
            if (!newConstraints.isEmpty()) {
                newDomainElements.put(
                    element.getKey(),
                    Collections.unmodifiableSet(newConstraints)
                );
            }
        }
        // Check if there are tracked constraints
        if (newDomainElements.isEmpty()) {
            return StripesDomain.TOP_ELEMENT;
        }
        // Create the new domain element
        return new StripesDomain(Collections.unmodifiableMap(newDomainElements));
    }

    /**
     * Performs the widening operation between this domain element and the specified one.
     *
     * @param other The other lattice element
     * @return The result of the widening operation.
     */
    @Override
    @NotNull
    protected StripesDomain wideningAux(final StripesDomain other) {
        // Clear the result
        final Map<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> newDomainElements =
                new HashMap<>();

        // Loop over the tracked variables
        for (final Entry<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> element :
                this.trackedConstraints.entrySet()) {
            // Extract the constraints associated to the variable in this domain element
            final Set<@NotNull Constraint> elementConstraints = element.getValue();
            // Extract the constraints associated to the variable in the other domain element
            @Nullable
            final Set<@NotNull Constraint> otherConstraints = other.trackedConstraints.get(
                element.getKey()
            );
            // Check if the variable is tracked in both domain elements
            if (otherConstraints != null) {
                // Retain the common constraints
                //noinspection ObjectAllocationInLoop
                final Set<@NotNull Constraint> newConstraints = new HashSet<>(elementConstraints);
                newConstraints.retainAll(otherConstraints);
                newDomainElements.put(
                    element.getKey(),
                    Collections.unmodifiableSet(newConstraints)
                );
            }
        }
        // Check if there are tracked constraints
        if (newDomainElements.isEmpty()) {
            return StripesDomain.TOP_ELEMENT;
        }
        // Create the new domain element
        return new StripesDomain(Collections.unmodifiableMap(newDomainElements));
    }

    /**
     * Checks if this domain element is less or equal the specified one.
     *
     * @param other The other lattice element
     * @return {@code true} if this element is less that or equal to the other, {@code false}
     *         otherwise.
     */
    @SuppressWarnings({ "FeatureEnvy", "MethodWithMultipleReturnPoints" })
    @Override
    protected boolean lessOrEqualAux(final StripesDomain other) {
        // Loop over tracked variables
        for (final Entry<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> otherElements :
                other.trackedConstraints.entrySet()) {
            // Extract the constraints associated to the variable in this domain element
            final Set<@NotNull Constraint> otherConstraints = otherElements.getValue();
            // Extract the constraints associated to the variable in the other domain element
            @Nullable
            final Set<@NotNull Constraint> elementConstraints =
                this.trackedConstraints.get(otherElements.getKey());
            // Check if the variable is tracked in the other domain element
            if (elementConstraints != null) {
                // Loop over the constraints associated to the variable in the other domain element
                for (final Constraint otherConstraint : otherConstraints) {
                    // No constraint has to be wider than the ones in other domain element
                    if (
                        elementConstraints
                            .stream()
                            .noneMatch(
                                thisConstraint ->
                                    thisConstraint.differsOnlyOnK2(otherConstraint) &&
                                    (otherConstraint.getK2() <= thisConstraint.getK2())
                            )
                    ) {
                        return false;
                    }
                    break;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Alter the abstract state by effect of the assignment of the specified expression to the
     * specified variable.
     *
     * @param id The identifier to assign the value to
     * @param expression The expression to assign
     * @param pp The program point that where this operation is being evaluated
     * @return The abstract state modified by the assignment.
     */
    @SuppressWarnings(
        {
            "ChainOfInstanceofChecks",
            "FeatureEnvy",
            "MethodWithMultipleReturnPoints",
            "OverlyComplexMethod",
            "OverlyCoupledMethod"
        }
    )
    @Override
    @NotNull
    public StripesDomain assign(
        final Identifier id,
        final ValueExpression expression,
        final ProgramPoint pp
    ) {
        // Skip the assignment if the abstract state is bottom
        if (this.isBottom()) {
            return this;
        }
        // Consider only assignments
        if ((pp instanceof Assignment) && (id instanceof Variable assignedVariable)) {
            // Drop all the constraints that mention the assigned variable
            Map<@NotNull Variable,
                    @Unmodifiable @NotNull Set<@NotNull Constraint>> newTrackedConstraints =
                this.drop(assignedVariable);
            // Simplify the assigned expression
            final Polynomial resultingPolynomial = Simplifier.simplify(expression, 2);
            // Check if its has been simplified correctly
            if (resultingPolynomial.isValid() && !resultingPolynomial.isConstantPolynomial()) {
                // Extract the monomials
                @NotNull
                final Monomial firstMonomial = resultingPolynomial.getMonomial(0);
                @Nullable
                final Monomial secondMonomial = (resultingPolynomial.getSize() > 1)
                    ? resultingPolynomial.getMonomial(1)
                    : null;
                // Extract the constant value
                final int constant = resultingPolynomial.getConstantCoefficient();
                // Check if the simplified expression is in normalized form
                if (
                    (!firstMonomial.isNull()) &&
                    (
                        (secondMonomial == null) ||
                        (firstMonomial.getCoefficient() == secondMonomial.getCoefficient())
                    ) &&
                    !assignedVariable.equals(firstMonomial.getVariable()) &&
                    !assignedVariable.equals(
                        (secondMonomial == null) ? null : secondMonomial.getVariable()
                    )
                ) {
                    // Clear the inferred constraints
                    final Collection<@NotNull FullConstraint> inferredConstraints =
                            new LinkedList<>();
                    // Add the constraints extracted from the assignment
                    inferredConstraints.add(
                        new FullConstraint(
                            assignedVariable,
                            firstMonomial.getVariable(),
                            (secondMonomial == null) ? null : secondMonomial.getVariable(),
                            firstMonomial.getCoefficient(),
                            constant - 1
                        )
                    );
                    // Infer new constraints
                    final ConstraintsComputer constraintsComputer = new ConstraintsComputer(
                        newTrackedConstraints
                    );
                    inferredConstraints.addAll(
                        constraintsComputer.inferNewAssignmentConstraints(
                            assignedVariable,
                            firstMonomial,
                            secondMonomial,
                            constant
                        )
                    );
                    // Merge the inferred constraints
                    newTrackedConstraints =
                        ConstraintsMerger.mergeConstraints(
                            newTrackedConstraints,
                            inferredConstraints
                        );
                }
                // Check if the exception case applies
                if (secondMonomial != null) {
                    // Extract the new constraints
                    newTrackedConstraints =
                        StripesDomain.handleExceptionAssignment(
                            assignedVariable,
                            newTrackedConstraints,
                            firstMonomial,
                            secondMonomial,
                            constant
                        );
                }
            }
            // Return the new domain element
            return new StripesDomain(newTrackedConstraints);
        }
        // Skip other statements
        if ((pp instanceof Return) || (id instanceof HeapLocation)) {
            return this;
        }
        return StripesDomain.TOP_ELEMENT;
    }

    /**
     * Handles the assignments in the form <pre>  x=u-v</pre>.
     *
     * @param assignedVariable Variable the expression is assigned.
     * @param newTrackedConstraints The tracked constraints without the ones that mention
     *         the assigned variable.
     * @param firstMonomial First monomial of the simplified expression.
     * @param secondMonomial Second monomial of the simplified expression.
     * @param constant Constant value of the simplified expression.
     * @return The new tracked constraints extracted from the assignment.
     */
    @SuppressWarnings("FeatureEnvy")
    private static Map<@NotNull Variable,
            @Unmodifiable @NotNull Set<@NotNull Constraint>> handleExceptionAssignment(
        final Variable assignedVariable,
        final Map<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> newTrackedConstraints,
        @NotNull final Monomial firstMonomial,
        @NotNull final Monomial secondMonomial,
        final int constant
    ) {
        /// Extract the positive and negative monomial
        final Monomial positive = (firstMonomial.getCoefficient() >= 0)
            ? firstMonomial
            : secondMonomial;
        final Monomial negative = (firstMonomial.getCoefficient() < 0)
            ? firstMonomial
            : secondMonomial;

        // Check the coefficient
        if ((positive.getCoefficient() == 1) && (negative.getCoefficient() == -1)) {
            // Clear the inferred constraints
            final Collection<FullConstraint> inferredConstraints = new LinkedList<>();
            // Add the constraints extracted from the assignment
            inferredConstraints.add(
                new FullConstraint(
                    positive.getVariable(),
                    assignedVariable,
                    negative.getVariable(),
                    1,
                    -constant - 1
                )
            );
            // Infer new constraints
            final ConstraintsComputer constraintsComputer = new ConstraintsComputer(
                newTrackedConstraints
            );
            inferredConstraints.addAll(
                constraintsComputer.inferNewAssignmentConstraints(
                    assignedVariable,
                    firstMonomial,
                    secondMonomial,
                    constant
                )
            );
            // Return the merged constraints
            return ConstraintsMerger.mergeConstraints(newTrackedConstraints, inferredConstraints);
        }
        // No new constraints have been added
        return newTrackedConstraints;
    }

    /**
     * Drops all the constraints associated to the specified variable from the current abstract
     * state and return the modified state.
     *
     * @param variable Variable the constraints to drop mention.
     * @return The new tracked constraints where all the constraints that mention the specified
     *         variable have been dropped.
     */
    @SuppressWarnings("FeatureEnvy")
    @NotNull
    private Map<@NotNull Variable, @Unmodifiable Set<@NotNull Constraint>> drop(
        @NotNull final Variable variable
    ) {
        // Clear the result
        final Map<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> newDomainElements =
                new HashMap<>();
        // Create the predicate the constraints to remove satisfy
        final Predicate<@NotNull Constraint> constraintFilter = constraint ->
            !constraint.getY().equals(variable) && !variable.equals(constraint.getZ());
        // Define the elements collector
        final Collector<@NotNull Constraint, ?,
                @NotNull Set<Constraint>> collector = Collectors.toSet();

        // Loop over the tracked constraints
        for (final Entry<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> element :
                this.trackedConstraints.entrySet()) {
            // Drops the constraints associated to the assigned variable by skipping it
            if (!element.getKey().equals(variable)) {
                // Delete all the constraints that mention the specified variable
                final Set<Constraint> newConstraints = element
                    .getValue()
                    .stream()
                    .filter(constraintFilter)
                    .collect(collector);
                // In any constraint is retained, add it to the new map
                if (!newConstraints.isEmpty()) {
                    newDomainElements.put(
                        element.getKey(),
                        Collections.unmodifiableSet(newConstraints)
                    );
                }
            }
        }
        return newDomainElements;
    }

    /**
     * Deletes an identifier and all the information associated to it in this domain element.
     *
     * @param id The identifier to forget.
     * @return The new domain element where the identifier has been forgotten.
     */
    @Override
    @NotNull
    public StripesDomain forgetIdentifier(final Identifier id) {
        // Skip if the abstract state is bottom
        if (this.isBottom()) {
            return this;
        }
        // Pedantic check
        if (id instanceof Variable variable) {
            // Drop the constraints that mention the variable to forget
            return new StripesDomain(Collections.unmodifiableMap(this.drop(variable)));
        }

        return this;
    }

    /**
     * Checks whether the expression is satisfied in this abstract domain element.
     *
     * @param expression Rhe expression whose satisfiability is to be evaluated
     * @param pp The program point that where this operation is being evaluated
     * @return Always {@link Satisfiability#UNKNOWN}.
     */
    @Override
    @NotNull
    public Satisfiability satisfies(final ValueExpression expression, final ProgramPoint pp) {
        return Satisfiability.UNKNOWN;
    }

    /**
     * Returns a copy of this domain element where the semantic of the given expression has been
     * applied.
     *
     * @param expression The expression whose semantics need to be computed
     * @param pp The program point that where this operation is being evaluated
     * @return Always {@code this}.
     */
    @Override
    @NotNull
    public StripesDomain smallStepSemantics(
        final ValueExpression expression,
        final ProgramPoint pp
    ) {
        return this;
    }

    /**
     * Returns a copy of this domain element where the given expression is assumed to be true. In
     * particular, if the expression is proven to be always false, {@link StripesDomain#bottom()} is
     * returned.
     *
     * @param expression The expression to assume to hold.
     * @param pp The program point that where this operation is being evaluated
     * @return A copy of this domain element where the given expression is assumed to be true.
     */
    @Override
    @NotNull
    public StripesDomain assume(final ValueExpression expression, final ProgramPoint pp) {
        // Check if the expression is satisfied
        final List<@NotNull FullConstraint> inferredConstraints = new LinkedList<>();
        final Satisfiability result = new ConditionChecker(this.trackedConstraints)
            .checkCondition(expression, inferredConstraints);
        // The condition is guaranteed to be always true
        if (result == Satisfiability.SATISFIED) {
            return this;
        }
        // The condition is guaranteed to be always false
        if (result == Satisfiability.NOT_SATISFIED) {
            return this.bottom();
        }

        // Infer new constraints
        final ConstraintsComputer constraintsComputer = new ConstraintsComputer(
            this.trackedConstraints
        );
        inferredConstraints.addAll(
            constraintsComputer.inferNewConditionConstraints(inferredConstraints)
        );

        // Add the constraints to the new domain
        @Unmodifiable
        final Map<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> newTrackedConstraints =
                ConstraintsMerger.mergeConstraints(
            this.trackedConstraints,
            inferredConstraints
        );
        // Return the modified domain element
        return new StripesDomain(newTrackedConstraints);
    }

    /**
     * Checks if this object is equal to the specified one.
     *
     * @param obj The other object.
     * @return {@code true} if this object is equal to the specified one, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }

        final StripesDomain otherStripeDomain = (StripesDomain) obj;
        // Compare the tracked constraints
        return this.trackedConstraints.equals(otherStripeDomain.trackedConstraints);
    }

    /**
     * Computes the has code of this element.
     *
     * @return The has code of this element.
     */
    @Override
    public int hashCode() {
        return this.trackedConstraints.hashCode();
    }

    /**
     * Returns a string representation of this domain element.
     *
     * @return A string representation of this domain element.
     */
    @Override
    @NotNull
    public String toString() {
        return this.representation();
    }

    /**
     * Returns a string representation of this domain element.
     *
     * @return A string representation of this domain element.
     */
    @Override
    @NotNull
    public @NonNls String representation() {
        // Top
        if (this.isTop()) {
            return "⊤";
        }
        // Bottom
        if (this.isBottom()) {
            return "⊥";
        }
        final StringBuilder builder = new StringBuilder("[");

        // Loop over the tracked constrains
        for (final Entry<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> elements :
                this.trackedConstraints.entrySet()) {
            builder.append(elements.getKey()).append(" → {");
            // Append all the constraints
            for (final Constraint constraints : elements.getValue()) {
                builder.append(constraints).append(", ");
            }
            // Delete the last comma if present
            if (!elements.getValue().isEmpty()) {
                builder.delete(builder.length() - 2, builder.length());
            }
            // Insert a new line
            //noinspection HardcodedLineSeparator
            builder.append("},\n");
        }
        // Delete the last comma if present
        if (builder.length() > 1) {
            builder.delete(builder.length() - 2, builder.length());
        }
        // Create the representation
        return builder.append(']').toString();
    }
}
