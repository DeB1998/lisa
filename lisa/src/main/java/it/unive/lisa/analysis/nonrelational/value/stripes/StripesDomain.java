package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.UnaryOperator;
import it.unive.lisa.symbolic.value.ValueExpression;
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
    private final Map<Identifier, @Unmodifiable @NotNull Set<Constraint>> domainElements;

    private StripesDomain(@NotNull final Map<Identifier, Set<Constraint>> domainElements) {
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
        final Map<Identifier, Set<Constraint>> newDomainElements = new HashMap<>();

        for (final Entry<Identifier, Set<Constraint>> element : this.domainElements.entrySet()) {
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
        /*final Set<Constraint> newConstraints =
            this.domainElements.stream()
                .flatMap(
                    element ->
                        other.domainElements
                            .stream()
                            .filter(constraint.getConstraints()::isSameConstraint)
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

        return StripesDomain.TOP;*/
    }

    @Override
    protected StripesDomain wideningAux(final StripesDomain other) throws SemanticException {
        final Map<Identifier, Set<Constraint>> newDomainElements = new HashMap<>();

        for (final Entry<Identifier, Set<Constraint>> element : this.domainElements.entrySet()) {
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
        /*final Set<Constraint> newConstraints =
            this.domainElements.stream()
                .filter(other.domainElements::contains)
                .collect(Collectors.toSet());
        return new StripesDomain(newConstraints);*/
    }

    /*@Override
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
                    constraint -> other.constraints.stream().anyMatch(constraint::isSameConstraint)
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
    }*/

    // this <= other
    // s1 <= other
    @Override
    protected boolean lessOrEqualAux(final StripesDomain other) throws SemanticException {
        for (final Entry<Identifier, Set<Constraint>> otherElements : other.domainElements.entrySet()) {
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
        /*return other.domainElements
            .stream()
            .allMatch(
                c2 ->
                    this.domainElements.stream()
                        .anyMatch(c1 -> c1.isSameConstraint(c2) && (c1.getK2() <= c2.getK2()))
            );*/
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    @Override
    public StripesDomain assign(
        final Identifier id,
        final ValueExpression expression,
        final ProgramPoint pp
    ) throws SemanticException {
        if (pp instanceof Assignment) {
            final Map<Identifier, @Nullable Set<Constraint>> newDomainElements = this.drop(id);

            final SimplificationResult simplificationResult = Simplifier.simplify(expression);

            if (simplificationResult != null) {
                final Identifier firstIdentifier = simplificationResult.getFirstIdentifier();
                final int firstIdentifierCount = simplificationResult.getFirstIdentifierCount();
                final Identifier secondIdentifier = simplificationResult.getSecondIdentifier();
                final int secondIdentifierCount = simplificationResult.getSecondIdentifierCount();
                final int constant = simplificationResult.getConstant();
                if (
                    (firstIdentifier != null) &&
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

                    newDomainElements.put(id, Collections.unmodifiableSet(newConstraints));
                    return new StripesDomain(newDomainElements);
                }
            }
            //newDomainElements.put(id, null);
            return new StripesDomain(newDomainElements);
            /*if (expression instanceof Identifier) {
                
            } else if (expression instanceof BinaryExpression) {
                final Identifier[] identifiers = new Identifier[] { null, null };
                final int[] constants = new int[] { 0, 0, 0 };
                //normalize(expression,identifiers, constants);
            }*/
        }
        return StripesDomain.TOP;
    }

    @NotNull
    private Map<Identifier, Set<Constraint>> drop(final Identifier id) {
        final Map<Identifier, Set<Constraint>> newDomainElements = new HashMap<>();
        final Predicate<Constraint> constraintFilter = constraint ->
            !constraint.getX().equals(id) && !id.equals(constraint.getY());
        final Collector<Constraint, ?, Set<Constraint>> collector = Collectors.toSet();

        for (final Entry<Identifier, Set<Constraint>> element : this.domainElements.entrySet()) {
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

    /*private static void boh(final ValueExpression expression) {
        StripesDomain.rec(expression);
    }

    /*private static final class Pair<A, B> {
        private final A a;
        private final B b;
    
        public Pair(final A a, final B b) {
        
            this.a = a;
            this.b = b;
        }
    
        public A getA() {
        
            return this.a;
        }
    
        public B getB() {
        
            return this.b;
        }
    }*/

    /*
    @SuppressWarnings("ChainOfInstanceofChecks")
    private static @Nullable Set<Pair<Identifier, Integer>> rec(ValueExpression expression) {
        Set<Pair<Identifier, Integer>> s = new HashSet<>();
        if (expression instanceof UnaryExpression unaryExpression) {
            // s.add(new Pair<>(unaryExpression.getExpression().))
            int a = 1;
        } else if (expression instanceof BinaryExpression binaryExpression) {
            Set<Pair<Identifier, Integer>> leftSet, rightSet;
            if (binaryExpression.getLeft() instanceof BinaryExpression leftExpression) {
                leftSet = rec(leftExpression);
            } else if (binaryExpression.getLeft() instanceof Identifier leftIdentifier){
                s.add(new Pair<>(leftIdentifier, -1));
            }
            if (binaryExpression.getRight() instanceof BinaryExpression rightExpression) {
                rightSet = rec(rightExpression);
            } else if (binaryExpression.getRight() instanceof Identifier rightIdentifier){
                s.add(new Pair<>(rightIdentifier, -1));
            }
        }
        
        return null;
    }
    */

    /*private static class NormalizationResult {
    
        private final Identifier[] identifiers;
        private final int[] constants;
        
        private NormalizationResult(final Identifier[] identifiers) {
    
            this.identifiers = new Identifier[]{null, null};
            this.constants = new int[]{0, 0, 0};
        }
        
        public void incrementOccurrence(Identifier identifier, int occurrencesToAdd) {
            
            if ((this.identifiers[0] == null) || this.identifiers[0].equals(identifier)) {
                this.identifiers[0] = identifier;
                this.constants[0] += occurrencesToAdd;
            } else if ((this.identifiers[1] == null) || this.identifiers[1].equals(identifier)) {
                this.identifiers[1] = identifier;
                this.constants[1] += occurrencesToAdd;
            }
        }
        
        public void incrementConstant(int valueToAdd) {
            this.constants[2] += valueToAdd;
        }
        
        public Identifier getFirstIdentifier() {
            return this.identifiers[0];
        }
        
        public int getFirstIdentifierOccurrences() {
            return this.constants[0];
        }
    
        public Identifier getSecondIdentifier() {
            return this.identifiers[1];
        }
    
        public int getSecondIdentifierOccurrences() {
            return this.constants[1];
        }
        
        public int getConstant() {
            return this.constants[2];
        }
    }
    
    /*private static void normalize(SymbolicExpression expression, NormalizationResult result) {
        
        if (expression instanceof Identifier identifier) {
            result.incrementOccurrence(identifier, 1);
        } else if ((expression instanceof UnaryExpression unaryExpression) && (unaryExpression.getOperator() == UnaryOperator.NUMERIC_NEG)) {
            result.incrementOccurrence((Identifier) unaryExpression.getExpression(), -1);
        } else if (expression instanceof BinaryExpression binaryExpression) {
            SymbolicExpression leftExpression = binaryExpression.getLeft();
            SymbolicExpression rightExpression = binaryExpression.getRight();
            int constant = 0;
            
            switch (binaryExpression.getOperator()) {
                case NUMERIC_ADD:
                    if ((leftExpression instanceof Constant leftConstant) && (leftConstant.getValue() instanceof Integer leftIntegerConstant)) {
                        constant = leftIntegerConstant;
                    } else {
                        normalize(leftExpression, result);
                    }
                    if ((rightExpression instanceof Constant rightConstant) && (rightConstant.getValue() instanceof Integer rightIntegerConstant)) {
                        constant += rightIntegerConstant;
                    } else {
                        normalize(leftExpression, result);
                    }
                    
                    result.incrementConstant(constant);
                    break;
                case NUMERIC_SUB:
                    if ((leftExpression instanceof Constant leftConstant) && (leftConstant.getValue() instanceof Integer leftIntegerConstant)) {
                        constant = leftIntegerConstant;
                    } else {
                        normalize(leftExpression, result);
                    }
                    if ((rightExpression instanceof Constant rightConstant) && (rightConstant.getValue() instanceof Integer rightIntegerConstant)) {
                        constant -= rightIntegerConstant;
                    } else {
                        normalize(leftExpression, result);
                    }
        
                    result.incrementConstant(constant);
                    break;
                case NUMERIC_MUL:
                    // 3*5*x
                    // 3*(x+y)
                    if ((leftExpression instanceof Constant constant) && (constant.getValue() instanceof Integer integerConstant)
                            && (rightExpression instanceof Identifier identifier)
                    ) {
                        result.incrementOccurrence(identifier,integerConstant);
                        return true;
                    }
                    if ((rightExpression instanceof Constant constant) && (constant.getValue() instanceof Integer integerConstant)
                            && (leftExpression instanceof Identifier identifier)
                    ) {
                        result.incrementOccurrence(identifier,integerConstant);
                        return true;
                    }
                    return normalize(leftExpression,result) && normalize(rightExpression,result);
                case NUMERIC_DIV -> {}
                case NUMERIC_MOD -> {}
            }
        }
        
        
        return false;
    }*/

    // "x + y + 3 + 5 * (x + 2(x + y))"
    // "x + y + 3 + 5*x + 10*x + 10*y"
    // "16x + 11y + 3"
    /*
    private static Map<Identifier, Integer> rec(final ValueExpression expression) {
        
        // x = y
        // x = -y
        // x = y + z
        // x = 3 + y --> x - 1*y > 2
        // x = 3 * y
        
        int k2;
        final Map<Identifier, Integer> m = new HashMap<>();
        
        if (expression instanceof Identifier id) {
            m.put(id, 1);
        } else if ((expression instanceof UnaryExpression ue) && (ue.getOperator() == UnaryOperator.NUMERIC_NEG)) {
            m.put((Identifier) ue.getExpression(), -1);
        }
        // Ci sono al massimo 2 variabili?
        // Se si, queste due variabili, sono diverse dalla variabile alla quale l'espressione viene assegnata?
        // Se si, quante "volte" appaiono le due variabili?
        //       - Quanto vale la costante k_2?
        
        return Collections.emptyMap();
    }*/

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

    /*@Override
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
                return new StripesDomain(
                    Collections.singleton(new Constraint((Identifier) expression, null, 1, -1))
                );
                
                 * x: {[y, z, 1, 3], [y, _, 1, 0]}
                 
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
    }*/

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

        // z - k1*(x+y) > k2
        /*return "[%s]".formatted(
                (this.domainElements.isEmpty())
                    ? ""
                    : this.domainElements.stream()
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
            );*/

        if (builder.length() > 1) {
            builder.delete(builder.length() - 2, builder.length());
        }
        return builder.append(']').toString();
    }
}
