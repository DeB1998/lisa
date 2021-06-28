package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-17
 * @since version date
 */
public class StripesDomain
    extends BaseLattice<StripesDomain>
    implements ValueDomain<StripesDomain> {

    private static final StripesDomain TOP = new StripesDomain();

    private static final StripesDomain BOTTOM = new StripesDomain();

    @NotNull
    @Unmodifiable
    private final Map<@NotNull Variable, @Unmodifiable @NotNull Set<@NotNull Constraint>> domainElements;

    private StripesDomain(
        @Unmodifiable @NotNull final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> domainElements
    ) {
        this.domainElements = domainElements;
    }

    public StripesDomain() {
        this.domainElements = new HashMap<>();
    }

    @Override
    public boolean isTop() {
        return this.domainElements.isEmpty() && (this != StripesDomain.BOTTOM);
    }

    @Override
    public StripesDomain top() {
        return StripesDomain.TOP;
    }

    @Override
    public boolean isBottom() {
        return this == StripesDomain.BOTTOM; //this.domainElements.isEmpty() && this.isBottom;
    }

    @Override
    public StripesDomain bottom() {
        return StripesDomain.BOTTOM;
    }

    @Override
    protected StripesDomain lubAux(final StripesDomain other) throws SemanticException { // [z ->
        // {(y, x, 20, 4), (y, /, 0, 14)}]
        final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newDomainElements = new HashMap<>();

        for (final Entry<@NotNull Variable, @NotNull Set<@NotNull Constraint>> element : this.domainElements.entrySet()) {
            // element = [z -> {(y, x, 20, 4)}]
            final Set<@NotNull Constraint> elementConstraints = element.getValue();
            @Nullable
            final Set<@NotNull Constraint> otherElementConstraints = other.domainElements.get(
                element.getKey()
            );

            // otherElementConstraints = {(y, x, 20, 4), (y, /, 0, 14)}
            if (otherElementConstraints != null) {
                final Set<@NotNull Constraint> newConstraints = new HashSet<>();
                for (final Constraint c : elementConstraints) {
                    for (final Constraint otherC : otherElementConstraints) {
                        if (c.differsOnlyOnK2(otherC)) {
                            if (c.getK2() < otherC.getK2()) {
                                newConstraints.add(c);
                            } else {
                                newConstraints.add(otherC);
                            }
                            break;
                        }
                    }
                }
                if (!newConstraints.isEmpty()) {
                    newDomainElements.put(
                        element.getKey(),
                        Collections.unmodifiableSet(newConstraints)
                    );
                }
            }
        }

        if (newDomainElements.isEmpty()) {
            return StripesDomain.TOP;
        }
        return new StripesDomain(Collections.unmodifiableMap(newDomainElements));
    }

    @Override
    protected StripesDomain wideningAux(final StripesDomain other) throws SemanticException {
        final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newDomainElements = new HashMap<>();

        for (final Entry<@NotNull Variable, @NotNull Set<@NotNull Constraint>> element : this.domainElements.entrySet()) {
            // element = [z -> {(y, x, 20, 4)}]
            final Set<@NotNull Constraint> elementConstraints = element.getValue();
            @Nullable
            final Set<@NotNull Constraint> otherElementConstraints = other.domainElements.get(
                element.getKey()
            );
            if (otherElementConstraints != null) {
                final Set<Constraint> newConstraints = new HashSet<>(elementConstraints);
                newConstraints.retainAll(otherElementConstraints);
                newDomainElements.put(
                    element.getKey(),
                    Collections.unmodifiableSet(newConstraints)
                );
            }
        }

        if (newDomainElements.isEmpty()) {
            return StripesDomain.TOP;
        }
        return new StripesDomain(Collections.unmodifiableMap(newDomainElements));
    }

    // this <= other
    @Override
    protected boolean lessOrEqualAux(final StripesDomain other) throws SemanticException {
        for (final Entry<@NotNull Variable, @NotNull Set<@NotNull Constraint>> otherElements : other.domainElements.entrySet()) {
            final Set<@NotNull Constraint> otherConstraints = otherElements.getValue();
            @Nullable
            final Set<@NotNull Constraint> elementConstraints =
                this.domainElements.get(otherElements.getKey());

            if (elementConstraints != null) {
                for (final Constraint otherConstraint : otherConstraints) {
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

    @SuppressWarnings("ChainOfInstanceofChecks")
    @Override
    public StripesDomain assign(
        final Identifier id,
        final ValueExpression expression,
        final ProgramPoint pp
    ) throws SemanticException {
        if (this.isBottom()) {
            return this;
        }

        if ((pp instanceof Assignment) && (id instanceof Variable assignedVariable)) {
            Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newDomainElements =
                this.drop(assignedVariable);

            final Polynomial resultingPolynomial = Simplifier.simplify(expression, 2);

            if (resultingPolynomial.isValid()) {
                @Nullable
                final Monomial firstMonomial = (resultingPolynomial.getSize() > 0)
                    ? resultingPolynomial.getMonomial(0)
                    : null;
                @Nullable
                final Monomial secondMonomial = (resultingPolynomial.getSize() > 1)
                    ? resultingPolynomial.getMonomial(1)
                    : null;
                final int constant = resultingPolynomial.getConstantCoefficient();
                if (
                    (firstMonomial != null) &&
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
                    // assign x = y
                    final Set<@NotNull Constraint> oldConstraints = newDomainElements.get(id);
                    final List<@NotNull FullConstraint> newConstraints = new LinkedList<>();

                    newConstraints.add(
                        new FullConstraint(
                            assignedVariable,
                            firstMonomial.getVariable(),
                            (secondMonomial == null) ? null : secondMonomial.getVariable(),
                            firstMonomial.getCoefficient(),
                            constant - 1
                        )
                    );
                    final ConstraintsComputer constraintsComputer = new ConstraintsComputer(
                        newDomainElements
                    );
                    newConstraints.addAll(
                        constraintsComputer.computeNewAssignmentConstraints(
                            assignedVariable,
                            firstMonomial,
                            secondMonomial,
                            constant
                        )
                    );

                    newDomainElements = Utils.mergeConstraints(newDomainElements, newConstraints);
                }
                if ((firstMonomial != null) && (secondMonomial != null)) {
                    final Monomial positive = (firstMonomial.getCoefficient() >= 0)
                        ? firstMonomial
                        : secondMonomial;
                    final Monomial negative = (firstMonomial.getCoefficient() < 0)
                        ? firstMonomial
                        : secondMonomial;

                    if ((positive.getCoefficient() == 1) && (negative.getCoefficient() == -1)) {
                        
                        final List<FullConstraint> newConstraints = new LinkedList<>();
                        
                        newConstraints.add(new FullConstraint(
                                positive.getVariable(),
                                assignedVariable,
                                negative.getVariable(),
                                1,
                                -constant -1
                        ));
    
                        final ConstraintsComputer constraintsComputer = new ConstraintsComputer(
                                newDomainElements
                        );
                        
                        newConstraints.addAll(
                                constraintsComputer.computeNewAssignmentConstraints(
                                        assignedVariable,
                                        firstMonomial,
                                        secondMonomial,
                                        constant
                                )
                        );
    
                        newDomainElements = Utils.mergeConstraints(newDomainElements, newConstraints);
                    }
                }
            }
            return new StripesDomain(newDomainElements);
        }
        if ((pp instanceof Return) || (id instanceof HeapLocation)) {
            return this;
        }
        return StripesDomain.TOP;
    }

    @NotNull
    private Map<@NotNull Variable, Set<@NotNull Constraint>> drop(final Variable id) {
        final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newDomainElements = new HashMap<>();
        final Predicate<@NotNull Constraint> constraintFilter = constraint ->
            !constraint.getY().equals(id) && !id.equals(constraint.getZ());
        final Collector<@NotNull Constraint, ?, @NotNull Set<Constraint>> collector = Collectors.toSet();

        for (final Entry<@NotNull Variable, @NotNull Set<@NotNull Constraint>> element : this.domainElements.entrySet()) {
            if (!element.getKey().equals(id)) { // Drops the constraints associated to the
                // assigned variable
                final Set<Constraint> newConstraints = element
                    .getValue()
                    .stream()
                    .filter(constraintFilter)
                    .collect(collector);
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

    @Override
    public StripesDomain forgetIdentifier(final Identifier id) throws SemanticException {
        if (this.isBottom()) {
            return this;
        }
        if (id instanceof Variable variable) {
            final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newDomainElements = new HashMap<>(
                this.domainElements
            );

            newDomainElements.remove(variable);

            return new StripesDomain(Collections.unmodifiableMap(newDomainElements));
        }

        return this;
    }

    @Override
    public Satisfiability satisfies(final ValueExpression expression, final ProgramPoint pp)
        throws SemanticException {
        return Satisfiability.UNKNOWN;
    }

    @Override
    public StripesDomain smallStepSemantics(
        final ValueExpression expression,
        final ProgramPoint pp
    ) throws SemanticException {
        return this;
    }

    @Override
    public StripesDomain assume(final ValueExpression expression, final ProgramPoint pp)
        throws SemanticException {
        // Check if the expression is satisfied
        final List<FullConstraint> newConstraints = new LinkedList<>();
        final Satisfiability result = new ConditionChecker(this.domainElements)
            .checkCondition(expression, newConstraints);
        // The condition is guaranteed to be always true
        if (result == Satisfiability.SATISFIED) {
            return this;
        }
        // The condition is guaranteed to be always false
        if (result == Satisfiability.NOT_SATISFIED) {
            return this.bottom();
        }

        // Compute new constraints
        final ConstraintsComputer constraintsComputer = new ConstraintsComputer(
            this.domainElements
        );
        newConstraints.addAll(constraintsComputer.computeNewConditionConstraints(newConstraints));

        // Add the constraints to the new domain
        final Map<@NotNull Variable, @Unmodifiable @NotNull Set<@NotNull Constraint>> newDomainElements = Utils.mergeConstraints(
            this.domainElements,
            newConstraints
        );
        return new StripesDomain(newDomainElements);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (this.getClass() != o.getClass())) {
            return false;
        }

        final StripesDomain that = (StripesDomain) o;

        return this.domainElements.equals(that.domainElements);
    }

    @Override
    public int hashCode() {
        return this.domainElements.hashCode();
    }

    @Override
    public String toString() {
        return this.representation();
    }

    @Override
    public @NonNls String representation() {
        if (this.isTop()) {
            return "⊤";
        }
        if (this.isBottom()) {
            return "⊥";
        }
        final StringBuilder builder = new StringBuilder("[");

        // [z -> {(y, x, 20, 4), (y, /, 0, 14)}, y -> {(x, z, 3, 1)}]

        for (final Entry<@NotNull Variable, @NotNull Set<@NotNull Constraint>> otherElements : this.domainElements.entrySet()) {
            builder.append(otherElements.getKey()).append(" → {");

            for (final Constraint constraints : otherElements.getValue()) {
                builder.append(constraints).append(", ");
            }
            if (!otherElements.getValue().isEmpty()) {
                builder.delete(builder.length() - 2, builder.length());
            }
            builder.append("},\n");
        }
        if (builder.length() > 1) {
            builder.delete(builder.length() - 2, builder.length());
        }
        return builder.append(']').toString();
    }
    /*
    private List<FullConstraint> computeNewConstraints(
        @NotNull final Variable x,
        @NotNull final Variable y,
        @Nullable final Variable z,
        final int k1,
        final int k2
    ) {
        final List<FullConstraint> result = new LinkedList<>();

        if (k1 == -1) {
            if (z == null) {
                result.add(new FullConstraint(y, x, null, -1, k2));
            } else {
                result.add(new FullConstraint(y, x, z, -1, k2));
                result.add(new FullConstraint(z, x, y, -1, k2));
            }
        }
        if (z == null) {
            final Set<Constraint> yConstraints = this.domainElements.get(y);
            if (yConstraints != null) {
                for (final Constraint yConstraint : yConstraints) {
                    result.add(
                        new FullConstraint(
                            x,
                            yConstraint.getY(),
                            yConstraint.getZ(),
                            k1 * yConstraint.getK2(),
                            k2 + (k1 * yConstraint.getK2())
                        )
                    );
                }
            }
        } else {
            final Set<Constraint> yConstraints = this.domainElements.get(y);
            final Set<Constraint> zConstraints = this.domainElements.get(z);
            if ((yConstraints != null) && (zConstraints != null)) {
                for (final Constraint yConstraint : yConstraints) {
                    for (final Constraint zConstraint : zConstraints) {
                        if (
                            yConstraint.getY().equals(zConstraint.getY()) &&
                            Objects.equals(yConstraint.getZ(), zConstraint.getZ())
                        ) {
                            result.add(
                                new FullConstraint(
                                    x,
                                    yConstraint.getY(),
                                    yConstraint.getZ(),
                                    k1 * (yConstraint.getK1() + zConstraint.getK1()),
                                    (k1 * yConstraint.getK2()) + (k1 * zConstraint.getK2()) + k2
                                )
                            );
                        } else if (
                            !yConstraint.getY().equals(zConstraint.getY()) &&
                            (yConstraint.getZ() == null) &&
                            (zConstraint.getZ() != null) &&
                            (yConstraint.getK1() == zConstraint.getK1())
                        ) {
                            result.add(
                                new FullConstraint(
                                    x,
                                    yConstraint.getY(),
                                    zConstraint.getY(),
                                    k1 * yConstraint.getK1(),
                                    (k1 * yConstraint.getK2()) + (k1 * zConstraint.getK2()) + k2
                                )
                            );
                        }
                    }
                }
            }
        }

        return result;
    }*/
}
