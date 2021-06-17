package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Monomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Polynomial;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Return;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.HeapLocation;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.Variable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
        return this.domainElements.isEmpty() && (this != BOTTOM);
    }

    @Override
    public StripesDomain top() {
        return StripesDomain.TOP;
    }

    @Override
    public boolean isBottom() {
        return this == BOTTOM; //this.domainElements.isEmpty() && this.isBottom;
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
                        if (c.isSameConstraint(otherC)) {
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

            if (elementConstraints != null) { // this.domainElements.containsKey(otherElements
                // .getKey())
                for (final Constraint otherConstraint : otherConstraints) {
                    if (
                        elementConstraints
                            .stream()
                            .noneMatch(
                                thisConstraint ->
                                    thisConstraint.isSameConstraint(otherConstraint) &&
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

        if ((pp instanceof Assignment) && (id instanceof Variable variable)) {
            final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newDomainElements =
                this.drop(variable);

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
                /*final Variable firstIdentifier = simplificationResult.getFirstIdentifier();
                final int firstIdentifierCount = simplificationResult.getFirstIdentifierCount();
                final Variable secondIdentifier = simplificationResult.getSecondIdentifier();
                final int secondIdentifierCount = simplificationResult.getSecondIdentifierCount();*/
                final int constant = resultingPolynomial.getConstant();
                if (
                    (firstMonomial != null) &&
                    (firstMonomial.getCoefficient() != 0) &&
                    (
                        (secondMonomial == null) ||
                        (firstMonomial.getCoefficient() == secondMonomial.getCoefficient())
                    ) &&
                    !id.equals(firstMonomial.getVariable()) &&
                    !id.equals((secondMonomial == null) ? null : secondMonomial.getVariable())
                ) {
                    // assign x = y
                    final Set<@NotNull Constraint> oldConstraints = newDomainElements.get(id);
                    final Set<@NotNull Constraint> newConstraints = (oldConstraints == null)
                        ? new HashSet<>()
                        : new HashSet<>(oldConstraints);

                    newConstraints.add(
                        new Constraint(
                            firstMonomial.getVariable(),
                            (secondMonomial == null) ? null : secondMonomial.getVariable(),
                            firstMonomial.getCoefficient(),
                            constant - 1
                        )
                    );
                    
                    if ((secondMonomial == null) && (firstMonomial.getCoefficient() == 1)) {
                        Constraint c = new Constraint(variable, null, 1, -constant - 1);

                        newDomainElements.merge(
                            firstMonomial.getVariable(),
                            Collections.singleton(c),
                            (o, n) -> {
                                Set<@NotNull Constraint> a = new HashSet<>(o);
                                a.addAll(n);
                                return a;
                            }
                        );

                        for (Entry<@NotNull Variable, @NotNull Set<@NotNull Constraint>> entry : newDomainElements.entrySet()) {
                            for (@NotNull Constraint element : entry.getValue()) {
                                if (
                                    entry.getKey().equals(firstMonomial.getVariable()) &&
                                    !element.getX().equals(variable) &&
                                    (firstMonomial.getCoefficient() == 1)
                                ) {
                                    c =
                                        new Constraint(
                                            element.getX(),
                                            element.getY(),
                                            element.getK1(),
                                            element.getK2()
                                        );

                                    newDomainElements.merge(
                                        variable,
                                        Collections.singleton(c),
                                        (o, n) -> {
                                            Set<@NotNull Constraint> a = new HashSet<>(o);
                                            a.addAll(n);
                                            return a;
                                        }
                                    );
                                }
                                // (1a)
                                // y = 2*z   ---> {y -> [(z, bot, 2, -1)]}
                                // x = y     ---> {y -> [(z, bot, 2, -1), (x, bot, 1, -1)],
                                //                 x -> [(y, bot, 1, -1), (z, bot, 2, -1)]}

                            }
                        }

                        for (Entry<@NotNull Variable, @NotNull Set<@NotNull Constraint>> entry : newDomainElements.entrySet()) {
                            for (@NotNull Constraint element : entry.getValue()) {
                                if (element.getX().equals(firstMonomial.getVariable())) {
                                    c =
                                        new Constraint(
                                            variable,
                                            element.getY(),
                                            element.getK1(),
                                            element.getK2()
                                        );

                                    newDomainElements.merge(
                                        entry.getKey(),
                                        Collections.singleton(c),
                                        (o, n) -> {
                                            Set<@NotNull Constraint> a = new HashSet<>(o);
                                            a.addAll(n);
                                            return a;
                                        }
                                    );
                                }
                            }
                        }
                        // (1b)
                        // a = 3*b --> {a -> [(b, bot, 3, -1)]}
                        // c = b   --> {c -> [(b, bot, 1, -1)], a -> [(c, bot, 3, -1), (b, bot, 3, -1)]}
                    }

                    if (secondMonomial != null) {
                        if (
                            (firstMonomial.getCoefficient() == 1) &&
                            (secondMonomial.getCoefficient() == 1)
                        ) {
                            for (Entry<@NotNull Variable, @NotNull Set<@NotNull Constraint>> entry : this.domainElements.entrySet()) {
                                for (@NotNull Constraint element : entry.getValue()) {
                                    if (element.getX().equals(variable)) {
                                        Constraint c = new Constraint(
                                            firstMonomial.getVariable(),
                                            secondMonomial.getVariable(),
                                            element.getK1(),
                                            element.getK2()
                                        );
                                        newDomainElements.merge(
                                            entry.getKey(),
                                            Collections.singleton(c),
                                            (o, n) -> {
                                                Set<@NotNull Constraint> a = new HashSet<>(o);
                                                a.addAll(n);
                                                return a;
                                            }
                                        );
                                    }
                                }
                            }
                        }
                        // (2)
                        // x = 0
                        // v1 = 2*x          --> {v1 -> [(x, bot, 2, -1)]}
                        // x = u+v           --> {x -> [(u, v, 1, -1)], v1 -> [(u,v,2, -1)]}
                    }

                    /* *************************
                    if (secondMonomial != null) {
                        newConstraints.add(
                            new Constraint(
                                secondMonomial.getVariable(),
                                firstMonomial.getVariable(),
                                secondMonomial.getCoefficient(),
                                constant - 1
                            )
                        );
                        int a = 1;
                    }
                    * ****************************/
                    // x -> (v, u, 1, -1) ----> x - 1*(v+u) > -1
                    // x -> (u, v, 1, -1) -----> x- 1*(u+v) > -1
                    /*
                    );*/

                    //newDomainElements.put(variable, Collections.unmodifiableSet(newConstraints));

                    newDomainElements.merge(
                        variable,
                        Collections.unmodifiableSet(newConstraints),
                        (o, n) -> {
                            Set<@NotNull Constraint> a = new HashSet<>(o);
                            a.addAll(n);
                            return a;
                        }
                    );
                    // {x=[(z, ⊥, 2, -1)], y=[(z, ⊥, 2, -1), (x, ⊥, 1, -1)]}
                    return new StripesDomain(newDomainElements);
                }
                if ((firstMonomial != null) && (secondMonomial != null)) {
                    if (
                        (
                            (firstMonomial.getCoefficient() == -1) &&
                            (secondMonomial.getCoefficient() == 1)
                        ) ||
                        (
                            (secondMonomial.getCoefficient() == -1) &&
                            (firstMonomial.getCoefficient() == 1)
                        )
                    ) {
                        Monomial positive = (firstMonomial.getCoefficient() == 1)
                            ? firstMonomial
                            : secondMonomial;
                        Monomial negative = (firstMonomial.getCoefficient() == -1)
                            ? firstMonomial
                            : secondMonomial;
                        Constraint c = new Constraint(variable, negative.getVariable(), 1, -1);
                        newDomainElements.merge(
                            positive.getVariable(),
                            Collections.singleton(c),
                            (o, n) -> {
                                Set<@NotNull Constraint> a = new HashSet<>(o);
                                a.addAll(n);
                                return a;
                            }
                        );
                    }
                }
            }
            return new StripesDomain(newDomainElements);
        }
        if ((pp instanceof Return) || (id instanceof HeapLocation)) {
            return this;
        }
        return StripesDomain.TOP;
        // (0)
        // x = y ---> x - y > -1      y - x > - 1
        // x -> (y, bot, 1, -1)       y -> (x, bot, 1, -1)

        // x = y+z --> x = z+y

        // x = 3*y

        // (1a)
        // y = 2*z -----------> [y-2*z > -1]
        // x = y   -----------> [x-y > -1 , x-2*z > -1]

        /* NO!
        // y = 2*z -----------> [y > 2*z - 1]
        // x = 4*y   -----------> [x > 4*y-1 , x > ??????????????????????????????????????]
        // a > 2*b    b > 2*c ---> a > 4*c ?????? ---> NO
        // a = 200
        // b = 90
        // c = 40
        **/

        // (1b)
        // a = 3*b --> {a -> [(b, bot, 3, -1)]}
        // c = b   --> {c -> [(b, bot, 1, -1)], a -> [(c, bot, 3, -1), (b, bot, 3, -1)]}

        // (2)
        // x = 0
        // v1 = 2*x          --> v1 -> [(x, bot, 2, -1)] ------> v1-2*x > -1
        // x = u+v           --> x -> [(u, v, 1, -1)]    ------> x-1*(u+v) > -1
        // --> v1 = u+v      --> v1 -> [(u,v,2, -1)]     ------> v1-2*(u+v) > -1

        // (3*)
        // x = u-v
        // x - u + v = 0 --> -u = -x -v   --> u = x + v ----> u-1*(x+v) > -1

        // x > u+v ---> x -1*(u+v) > -1
    }

    @NotNull
    private Map<@NotNull Variable, Set<@NotNull Constraint>> drop(final Variable id) {
        final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newDomainElements = new HashMap<>();
        final Predicate<@NotNull Constraint> constraintFilter = constraint ->
            !constraint.getX().equals(id) && !id.equals(constraint.getY());
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

    // "x + y + 3 + 5 * (x + 2(x + y))"
    // "x + y + 3 + 5*x + 10*x + 10*y"
    // "16x + 11y + 3"

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
        // y < 4x ---> 4x > y
        // y >= 4x ---> y > 4x -1
        // y = 16 x = 4
        // y == 4x --> y >= 4x && y <= 4x

        // y != x --> y < x || y > x   -----> x > y || y > x   x -> (y, _|_, 1, -1)   y -> (x,
        // _|_, 1, -1)

        /*if (expression instanceof BinaryExpression binaryExpression) {
            final Polynomial leftPolynomial = Simplifier.simplify(binaryExpression.getLeft(), 3);
            final Polynomial rightPolynomial = Simplifier.simplify(binaryExpression.getRight(), 3);

            if (binaryExpression.getOperator() == BinaryOperator.COMPARISON_GT) {
                leftPolynomial.subtract(rightPolynomial);
                // ASSUNZIONI:
                // Tutte e tre le variabili = x, y, z
                // k1 != -1 && k1 != 1
                final int k2 = -leftPolynomial.getConstant();
                if (leftPolynomial.isValid() && (leftPolynomial.getSize() == 3)) {
                    @Nullable
                    Monomial x = null;
                    @Nullable
                    Monomial y = null;
                    @Nullable
                    Monomial z = null;
                    for (int i = 0; i < leftPolynomial.getSize(); i++) {
                        final Monomial monomial = leftPolynomial.getMonomial(i);
                        if (monomial.getCoefficient() == 1) {
                            x = monomial;
                        } else {
                            if ((y != null) && (z == null)) {
                                z = monomial;
                            } else if (y == null) {
                                y = monomial;
                            } else {
                                return this.top();
                            }
                        }
                    }
                    if (
                        (x != null) &&
                        (y != null) &&
                        ((z == null) || (y.getCoefficient() == z.getCoefficient()))
                    ) {
                        for (final Entry<Variable, Set<Constraint>> constraints : this.domainElements.entrySet()) {
                            if (constraints.getKey().equals(x.getVariable())) {
                                for (final Constraint constraint : constraints.getValue()) {
                                    // x -> {(y, _|_, k1, k2)}

                                    // x - k1 * (y+z) - k2 > 0

                                    if (
                                        constraint.getX().equals(y.getVariable()) &&
                                        z != null &&
                                        Objects.equals(constraint.getY(), z.getVariable()) &&
                                        constraint.getK1() == y.getCoefficient() &&
                                        k2 <= constraint.getK2()
                                    ) {
                                        return this;
                                    }
                                }
                            }
                        }
                        return this.bottom();
                    }
                }
                // x -> {(y, z, k1, k2)}

                // a*x + b*y + c*z + d

                // a == 1
                // b == c
                // exist x and y
                // x + y
                // x - (-1) * (y) - ??? > 0
                // y - (-1) * (x) - ??? > 0

                // x − k1 * (y + [z]) - k2 > 0

                final int a = 1;
            }
            final int a = 1;
        }
        return this;
        /* if (expression instanceof BinaryExpression binaryExpression) {
            if (binaryExpression.getOperator() == BinaryOperator.COMPARISON_GT) {
                SimplificationResult leftSimplification = Simplifier.simplify(binaryExpression
                .getLeft());
                SimplificationResult rightSimplification = Simplifier.simplify(binaryExpression
                .getRight());
    
                if ((leftSimplification == null) && (rightSimplification == null)) {
                    return this;
                }
                if (leftSimplification != null && rightSimplification != null) {
                    Variable x = null;
                    Variable y = null;
                    Variable z = null;
                    int k1 = 0;
                    int k2 = 0;
    
                    // Assunzione 1: Siamo già nella forma normalizzata
                    x = leftSimplification.getFirstIdentifier();
                    y =
                    k2 = rightSimplification.getConstant();
                }
            }
        }
        
        satisfies(expression, pp);*/

        // (x + y > 0) && (a + b > 0)

        Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newConstraints = new HashMap<>();
        final Satisfiability result = Normalizer.normalizeCondition(
            expression,
            this.domainElements,
            newConstraints
        );
        if (result == Satisfiability.SATISFIED) {
            return this;
        }
        if (result == Satisfiability.NOT_SATISFIED) {
            return bottom();
        }
        Map<@NotNull Variable, @Unmodifiable @NotNull Set<@NotNull Constraint>> newDomainElements = new HashMap<>(
            this.domainElements
        );
        Utils.mergeConstraints(newDomainElements, newConstraints);
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
            builder.append("}, ");
        }

        if (builder.length() > 1) {
            builder.delete(builder.length() - 2, builder.length());
        }
        return builder.append(']').toString();
    }
}
