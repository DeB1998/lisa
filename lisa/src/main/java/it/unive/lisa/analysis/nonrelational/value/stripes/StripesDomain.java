package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
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
    private final Map<@NotNull Variable, @Unmodifiable @NotNull Set<Constraint>> domainElements;

    private StripesDomain(
        @NotNull final Map<@NotNull Variable, @NotNull Set<Constraint>> domainElements
    ) {
        this.domainElements = domainElements;
    }

    public StripesDomain() {
        this.domainElements = new HashMap<>();
    }

    @Override
    public boolean isTop() {
        return this.domainElements.isEmpty();
    }

    @Override
    public StripesDomain top() {
        return StripesDomain.TOP;
    }

    // isBottom mancante: NON è una svista

    @Override
    public StripesDomain bottom() {
        return StripesDomain.BOTTOM;
    }

    @Override
    protected StripesDomain lubAux(final StripesDomain other) throws SemanticException { // [z -> {(y, x, 20, 4), (y, /, 0, 14)}]
        final Map<Variable, Set<Constraint>> newDomainElements = new HashMap<>();

        for (final Entry<Variable, Set<Constraint>> element : this.domainElements.entrySet()) {
            // element = [z -> {(y, x, 20, 4)}]
            final Set<Constraint> elementConstraints = element.getValue();
            @Nullable
            final Set<Constraint> otherElementConstraints = other.domainElements.get(
                element.getKey()
            );

            // otherElementConstraints = {(y, x, 20, 4), (y, /, 0, 14)}
            if (otherElementConstraints != null) {
                final Set<Constraint> newConstraints = new HashSet<>();
                for (final Constraint c : elementConstraints) {
                    for (final Constraint otherC : otherElementConstraints) {
                        if (c.isSameConstraint(otherC)) {
                            if (c.getK2() < otherC.getK2()) {
                                newConstraints.add(c);
                            } else {
                                newConstraints.add(otherC);
                            }
                        }
                    }
                }
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

    @Override
    protected StripesDomain wideningAux(final StripesDomain other) throws SemanticException {
        final Map<Variable, Set<Constraint>> newDomainElements = new HashMap<>();

        for (final Entry<Variable, Set<Constraint>> element : this.domainElements.entrySet()) {
            // element = [z -> {(y, x, 20, 4)}]
            final Set<Constraint> elementConstraints = element.getValue();
            @Nullable
            final Set<Constraint> otherElementConstraints = other.domainElements.get(
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
    // s1 <= other
    @Override
    protected boolean lessOrEqualAux(final StripesDomain other) throws SemanticException {
        for (final Entry<Variable, Set<Constraint>> otherElements : other.domainElements.entrySet()) {
            final Set<Constraint> otherConstraints = otherElements.getValue();
            @Nullable
            final Set<Constraint> elementConstraints =
                this.domainElements.get(otherElements.getKey());
            if (elementConstraints != null) { // this.domainElements.containsKey(otherElements.getKey())
                for (final Constraint c : otherConstraints) {
                    if (
                        elementConstraints
                            .stream()
                            .noneMatch(e -> e.isSameConstraint(c) && (c.getK2() <= e.getK2()))
                    ) {
                        return false;
                    }
                    break;
                }
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
        if ((pp instanceof Assignment) && (id instanceof Variable variable)) {
            final Map<@NotNull Variable, @NotNull Set<Constraint>> newDomainElements =
                this.drop(variable);

            final SimplificationResult simplificationResult = Simplifier.simplify(expression);

            if (simplificationResult != null) {
                final Variable firstIdentifier = simplificationResult.getFirstIdentifier();
                final int firstIdentifierCount = simplificationResult.getFirstIdentifierCount();
                final Variable secondIdentifier = simplificationResult.getSecondIdentifier();
                final int secondIdentifierCount = simplificationResult.getSecondIdentifierCount();
                final int constant = simplificationResult.getConstant();
                if (
                    (firstIdentifier != null) &&
                    (firstIdentifierCount != 0) &&
                    (
                        (secondIdentifier == null) ||
                        (firstIdentifierCount == secondIdentifierCount)
                    ) &&
                    !id.equals(firstIdentifier) &&
                    !id.equals(secondIdentifier)
                ) {
                    // assign x = y
                    final Set<Constraint> oldConstraints = newDomainElements.get(id);
                    final Set<Constraint> newConstraints = (oldConstraints == null)
                        ? new HashSet<>()
                        : new HashSet<>(oldConstraints);

                    newConstraints.add(
                        new Constraint(
                            firstIdentifier,
                            secondIdentifier,
                            firstIdentifierCount,
                            constant - 1
                        )
                    );

                    newDomainElements.put(variable, Collections.unmodifiableSet(newConstraints));
                    return new StripesDomain(newDomainElements);
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
    private Map<Variable, Set<Constraint>> drop(final Variable id) {
        final Map<Variable, Set<Constraint>> newDomainElements = new HashMap<>();
        final Predicate<Constraint> constraintFilter = constraint ->
            !constraint.getX().equals(id) && !id.equals(constraint.getY());
        final Collector<Constraint, ?, Set<Constraint>> collector = Collectors.toSet();

        for (final Entry<Variable, Set<Constraint>> element : this.domainElements.entrySet()) {
            if (!element.getKey().equals(id) && (element.getValue() != null)) { // Drops the constraints associated to the assigned variable
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

    // "x + y + 3 + 5 * (x + 2(x + y))"
    // "x + y + 3 + 5*x + 10*x + 10*y"
    // "16x + 11y + 3"

    @Override
    public StripesDomain forgetIdentifier(final Identifier id) throws SemanticException {
        //this.

        return this; //TODO:
    }

    @Override
    public Satisfiability satisfies(final ValueExpression expression, final ProgramPoint pp)
        throws SemanticException {
        return Satisfiability.UNKNOWN; //TODO:
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
        return this;
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

    @SuppressWarnings("ObjectEquality")
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

        for (final Entry<Identifier, Set<Constraint>> otherElements : this.domainElements.entrySet()) {
            builder.append(otherElements.getKey()).append(" → {");

            if (otherElements.getValue() == null) {
                builder.delete(builder.length() - 2, builder.length()).append(" ⊤, ");
            } else {
                for (final Constraint constraints : otherElements.getValue()) {
                    builder.append(constraints.toString()).append(", ");
                }
                if (!otherElements.getValue().isEmpty()) {
                    builder.delete(builder.length() - 2, builder.length());
                }
                builder.append("}, ");
            }
        }

        if (builder.length() > 1) {
            builder.delete(builder.length() - 2, builder.length());
        }
        return builder.append(']').toString();
    }
}
