package it.unive.lisa.analysis.nonrelational.value;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.UnaryOperator;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.Variable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-17
 * @since version date
 */
public class StripesDomain
    extends BaseLattice<StripesDomain>
    implements NonRelationalValueDomain<StripesDomain> {

    private static final class Constraint {

        @NotNull
        private final Identifier x;

        @Nullable
        private final Identifier y;

        private final int k1;

        private final int k2;

        private Constraint(
            @NotNull final Identifier x,
            @Nullable final Identifier y,
            final int k1,
            final int k2
        ) {
            this.x = x;
            this.y = y;

            this.k1 = k1;
            this.k2 = k2;
        }

        @NotNull
        Identifier getX() {
            return this.x;
        }

        @Nullable
        Identifier getY() {
            return this.y;
        }

        int getK1() {
            return this.k1;
        }

        int getK2() {
            return this.k2;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if ((o == null) || (this.getClass() != o.getClass())) {
                return false;
            }

            final Constraint that = (Constraint) o;

            return (
                (this.k1 == that.k1) &&
                (this.k2 == that.k2) &&
                this.x.equals(that.x) &&
                (Objects.equals(this.y, that.y))
            );
        }

        @Override
        public int hashCode() {
            int result = this.x.hashCode();
            result = (31 * result) + ((this.y != null) ? this.y.hashCode() : 0);
            result = (31 * result) + this.k1;
            result = (31 * result) + this.k2;
            return result;
        }

        boolean isSameConstraint(@NotNull final Constraint other) {
            return (
                this.x.equals(other.x) && (this.k1 == other.k1) && Objects.equals(this.y, other.y)
            );
        }
    }

    private static final StripesDomain TOP = new StripesDomain();

    private static final StripesDomain BOTTOM = new StripesDomain();

    @NotNull
    private final Set<Constraint> constraints;

    private StripesDomain(@NotNull final Set<Constraint> constraints) {
        this.constraints = constraints;
    }

    public StripesDomain() {
        this.constraints = new HashSet<>();
    }

    @Override
    public StripesDomain top() {
        return StripesDomain.TOP;
    }

    @Override
    public StripesDomain bottom() {
        return StripesDomain.BOTTOM;
    }

    @Override
    protected StripesDomain lubAux(final StripesDomain other) throws SemanticException {
        
            final Set<Constraint> newConstraints =
                this.constraints.stream()
                    .flatMap(
                        constraint ->
                            other.constraints
                                .stream()
                                .filter(constraint::isSameConstraint)
                                .map(
                                    otherConstraint ->
                                        new Constraint(
                                            constraint.getX(),
                                            constraint.getY(),
                                            constraint.getK1(),
                                            Math.min(constraint.getK2(), otherConstraint.getK2())
                                        )
                                )
                    )
                    .collect(Collectors.toSet());

            if (!newConstraints.isEmpty()) {
                return new StripesDomain(newConstraints);
            }
        
        return StripesDomain.TOP;
    }

    @Override
    protected StripesDomain wideningAux(final StripesDomain other) throws SemanticException {
        

        final Set<Constraint> newConstraints =
            this.constraints.stream()
                .filter(other.constraints::contains)
                .collect(Collectors.toSet());
        return new StripesDomain(newConstraints);
    }

    @Override
    public StripesDomain glb(final StripesDomain other) throws SemanticException {
        if (
            (other == null) ||
            this.isBottom() ||
            other.isTop() ||
            (this == other) ||
            this.equals(other) ||
            this.lessOrEqual(other)
        ) {
            return this;
        }

        if (other.isBottom() || this.isTop() || other.lessOrEqual(this)) {
            return other;
        }

        
            final Set<Constraint> newConstraints =
                this.constraints.stream()
                    .filter(
                        constraint ->
                            other.constraints.stream().anyMatch(constraint::isSameConstraint)
                    )
                    .collect(Collectors.toSet());

            newConstraints.addAll(
                this.constraints.stream()
                    .filter(
                        constraint ->
                            // No element in s2 such that =s1
                            other.constraints.stream().noneMatch(constraint::isSameConstraint)
                    )
                    .collect(Collectors.toUnmodifiableSet())
            );
            newConstraints.addAll(
                other.constraints
                    .stream()
                    .filter(
                        constraint ->
                            // No element in s2 such that =s1
                            this.constraints.stream().noneMatch(constraint::isSameConstraint)
                    )
                    .collect(Collectors.toUnmodifiableSet())
            );

            if (!newConstraints.isEmpty()) {
                return new StripesDomain(newConstraints);
            }
        

        return StripesDomain.TOP;
    }

    // this <= other
    // s1 <= other
    @Override
    protected boolean lessOrEqualAux(final StripesDomain other) throws SemanticException {
        return other.constraints
            .stream()
            .allMatch(
                c2 ->
                    this.constraints.stream()
                        .anyMatch(c1 -> c1.isSameConstraint(c2) && (c1.getK2() <= c2.getK2()))
            );
    }

    @Override
    public boolean tracksIdentifiers(final Identifier id) {
        return !id.getDynamicType().isPointerType();
    }

    @Override
    public boolean canProcess(final SymbolicExpression expression) {
        // BinaryExpression Identifier UnaryExpression
        return (
            (expression instanceof BinaryExpression) ||
            (expression instanceof UnaryExpression) ||
            (expression instanceof Identifier)
        );
    }

    @Override
    public StripesDomain eval(
        final ValueExpression expression,
        final ValueEnvironment<StripesDomain> environment,
        final ProgramPoint pp
    ) throws SemanticException {
        
        if (pp instanceof Assignment) {
            final Assignment ass = (Assignment) pp;
            if (expression instanceof Identifier) {
                //StripesDomain existing = environment.getState(ass.getLeft());
                return new StripesDomain(Collections.singleton(new Constraint((Identifier) expression, null, 1, -1)));
                /*
                 * x: {[y, z, 1, 3], [y, _, 1, 0]}
                 */
            }
        }
        return StripesDomain.TOP;
    }

    @Override
    public Satisfiability satisfies(
        final ValueExpression expression,
        final ValueEnvironment<StripesDomain> environment,
        final ProgramPoint pp
    ) throws SemanticException {
        return Satisfiability.UNKNOWN;
    }

    @Override
    public ValueEnvironment<StripesDomain> assume(
        final ValueEnvironment<StripesDomain> environment,
        final ValueExpression expression,
        final ProgramPoint pp
    ) throws SemanticException {
        return environment;
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

        return this.constraints.equals(that.constraints);
    }
    
    @Override
    public int hashCode() {
        
        return this.constraints.hashCode();
    }
    
    @Override
    public String toString() {
        return this.representation();
    }

    @SuppressWarnings("ObjectEquality")
    @Override
    public @NonNls String representation() {
        if (this == StripesDomain.TOP) {
            return "T";
        }
        if (this == StripesDomain.BOTTOM) {
            return "_|_";
        }
        // z - k1*(x+y) > k2
        return "[%s]".formatted(
                
                (this.constraints.isEmpty())
                    ? ""
                    : this.constraints
                        .stream()
                        .reduce(
                            "",
                            (a, elem) ->
                                "{x: %s, y: %s, k1: %d, k2: %d}".formatted(
                                        elem.getX(),
                                        elem.getY(),
                                        elem.getK1(),
                                        elem.getK2()
                                    ),
                            "%s, %s"::formatted
                        )
            );
    }
}
