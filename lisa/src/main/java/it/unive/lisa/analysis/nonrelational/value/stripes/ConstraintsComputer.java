package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Monomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Polynomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.PolynomialBuilder;
import it.unive.lisa.symbolic.value.Variable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-26
 * @since version date
 */
class ConstraintsComputer {

    private static class EqualityContainer<T> {

        private final T data;
        private final boolean equal;

        public EqualityContainer(final T data, final boolean equal) {
            this.data = data;
            this.equal = equal;
        }

        public T getData() {
            return this.data;
        }

        public boolean isEqual() {
            return this.equal;
        }

        @Override
        public String toString() {
            return "[" + this.data + ", " + ((this.equal) ? "=" : ">") + ']';
        }
    }

    private final Map<Variable, Set<Constraint>> oldConstraints;

    public ConstraintsComputer(final Map<Variable, Set<Constraint>> oldConstraints) {
        this.oldConstraints = oldConstraints;
    }

    public List<FullConstraint> computeNewAssignmentConstraints(
        final Variable x,
        final Monomial firstMonomial,
        @Nullable final Monomial secondMonomial,
        final int constant
    ) {
        final List<FullConstraint> newConstraints = new LinkedList<>();

        final Variable firstVariable = firstMonomial.getVariable();
        final int firstCoefficient = firstMonomial.getCoefficient();

        final Variable secondVariable = (secondMonomial == null)
            ? null
            : secondMonomial.getVariable();
        final int secondCoefficient = (secondMonomial == null)
            ? 0
            : secondMonomial.getCoefficient();

        if ((firstCoefficient == secondCoefficient) || (secondMonomial == null)) {
            // x = y + k
            // y = x - k

            if ((firstCoefficient == 1) && (secondMonomial == null)) {
                newConstraints.add(new FullConstraint(firstVariable, x, null, 1, -constant - 1));
            }
            newConstraints.addAll(
                this.computeNewConstraints(
                        x,
                        firstVariable,
                        secondVariable,
                        firstCoefficient,
                        constant,
                        true
                    )
            );
            newConstraints.addAll(
                this.substituteInOtherConstraints(
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

    public List<FullConstraint> computeNewConditionConstraints(
        @Unmodifiable final List<FullConstraint> conditionConstraints
    ) {
        final List<FullConstraint> result = new LinkedList<>();

        for (final FullConstraint conditionConstraint : conditionConstraints) {
            result.addAll(
                this.computeNewConstraints(
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

    private List<FullConstraint> substituteInOtherConstraints(
        final Variable x,
        final Variable y,
        @Nullable final Variable z,
        final int k1,
        final int k2
    ) {
        final List<FullConstraint> result = new LinkedList<>();
        for (final Entry<Variable, Set<Constraint>> entry : this.oldConstraints.entrySet()) {
            for (final Constraint constraint : entry.getValue()) {
                final int constraintK1 = constraint.getK1();
                final int constraintK2 = constraint.getK2() + 1;
                if (z == null) {
                    if (k1 == 1) {
                        if (constraint.getY().equals(y)) {
                            result.add(
                                new FullConstraint(
                                    entry.getKey(),
                                    x,
                                    constraint.getZ(),
                                    constraintK1,
                                    constraintK2 - (constraint.getK1() * k2) - 1 // x = y --> x > -1
                                    // a = y --> a > y-1
                                    // x = a --> x-a >-1
                                )
                            );
                            if ((constraint.getK1() == 1) || (constraint.getK1() == -1)) {
                                result.add(
                                    new FullConstraint(
                                        x,
                                        entry.getKey(),
                                        constraint.getZ(),
                                        constraint.getK1(),
                                        k2 - (constraint.getK1() * constraintK2) - 1
                                    )
                                );
                            }
                        }
                        if (y.equals(constraint.getZ())) {
                            result.add(
                                new FullConstraint(
                                    entry.getKey(),
                                    constraint.getY(),
                                    y,
                                    constraint.getK1(),
                                    (constraintK2 + (constraint.getK1() * k2)) - 1
                                )
                            );
                        }
                    } else {
                        if (constraint.getZ() == null) {
                            if (constraint.getY().equals(y)) {
                                if ((constraint.getK1() % k1) == 0) {
                                    result.add(
                                        new FullConstraint(
                                            entry.getKey(),
                                            x,
                                            null,
                                            constraint.getK1() / k1,
                                            constraintK2 - ((constraint.getK1() / k1) * k2) - 1
                                        )
                                    );
                                } else if ((k1 % constraint.getK1()) == 0) {
                                    result.add( // b = c-1 m -2c > 0
                                        // b = c-1 --> m=2(c-1)+3 --> m = 2c+1 ----> m -2c > 0
                                        new FullConstraint(
                                            x,
                                            entry.getKey(),
                                            null,
                                            k1 / constraint.getK1(),
                                            k2 - ((k1 / constraint.getK1()) * constraintK2) - 1
                                        )
                                    );
                                }
                            }
                        }
                    }
                } else if (
                    (y.equals(constraint.getY()) && z.equals(constraint.getZ())) ||
                    (z.equals(constraint.getY()) && y.equals(constraint.getZ()))
                ) {
                    if ((constraint.getK1() % k1) == 0) {
                        result.add(
                            new FullConstraint(
                                entry.getKey(),
                                x,
                                null,
                                constraint.getK1() / k1,
                                constraintK2 - ((constraint.getK1() / k1) * k2) - 1
                            )
                        );
                    }
                    if ((k1 % constraint.getK1()) == 0) {
                        result.add(
                            new FullConstraint(
                                x,
                                entry.getKey(),
                                null,
                                k1 / constraint.getK1(),
                                k2 - ((k1 / constraint.getK1()) * constraintK2) - 1
                            )
                        );
                    }
                }
            }
        }

        return result;
    }

    private List<FullConstraint> computeNewConstraints(
        final Variable x,
        final Variable y,
        @Nullable final Variable z,
        final int k1,
        final int k2,
        boolean equalSign
    ) {
        final List<FullConstraint> newConstraints = new LinkedList<>();
        final Queue<EqualityContainer<FullConstraint>> constraintsToExplore = new LinkedList<>();
        constraintsToExplore.add(
            new EqualityContainer<>(new FullConstraint(x, y, z, k1, k2), equalSign)
        );

        while (!constraintsToExplore.isEmpty()) {
            final EqualityContainer<FullConstraint> next = constraintsToExplore.remove();
            final FullConstraint nextConstraint = next.getData();
            final boolean constraintEqualSign = next.isEqual();
            final Variable nextX = nextConstraint.getX();
            final Variable nextY = nextConstraint.getY();
            final Variable nextZ = nextConstraint.getZ();
            final int nextK1 = nextConstraint.getK1();
            final int nextK2 = nextConstraint.getK2();

            this.substituteVariables(
                    nextX,
                    nextY,
                    nextZ,
                    false,
                    nextK1,
                    nextK2,
                    constraintEqualSign,
                    constraintsToExplore,
                    newConstraints
                );
            this.substituteVariables(
                    nextX,
                    nextZ,
                    nextY,
                    false,
                    nextK1,
                    nextK2,
                    constraintEqualSign,
                    constraintsToExplore,
                    newConstraints
                );
            this.substituteVariables(
                    nextX,
                    nextY,
                    nextZ,
                    true,
                    nextK1,
                    nextK2,
                    constraintEqualSign,
                    constraintsToExplore,
                    newConstraints
                );
        }

        return newConstraints;
    }

    private void substituteVariables(
        final Variable x,
        final Variable y,
        @Nullable final Variable z,
        final boolean substituteZ,
        final int k1,
        final int k2,
        final boolean equalSign,
        final @NotNull Collection<? super EqualityContainer<FullConstraint>> constraintsToExplore,
        final @NotNull Collection<? super FullConstraint> newConstraints
    ) {
        final Set<Constraint> yConstraints = this.oldConstraints.get(y);
        if (yConstraints != null) {
            final Polynomial basePolynomial = new PolynomialBuilder(3)
                .addMonomial(1, x)
                .setConstantCoefficient(-k2)
                .build();
            if ((z == null) || (this.oldConstraints.get(z) == null)) {
                for (final Constraint yConstraint : yConstraints) {
                    this.polynomialSubstitution(
                            x,
                            yConstraint,
                            null,
                            basePolynomial,
                            k1,
                            equalSign,
                            constraintsToExplore,
                            newConstraints
                        );
                }
            } else {
                final Set<Constraint> zConstraints = this.oldConstraints.get(z);
                for (final Constraint yConstraint : yConstraints) {
                    for (final Constraint zConstraint : zConstraints) {
                        this.polynomialSubstitution(
                                x,
                                yConstraint,
                                zConstraint,
                                basePolynomial,
                                k1,
                                equalSign,
                                constraintsToExplore,
                                newConstraints
                            );
                    }
                }
            }
        }
    }

    private void polynomialSubstitution(
        final Variable x,
        final Constraint yConstraint,
        @Nullable final Constraint zConstraint,
        final Polynomial basePolynomial,
        final int k1,
        final boolean equalSign,
        final @NotNull Collection<? super EqualityContainer<FullConstraint>> constraintsToExplore,
        final @NotNull Collection<? super FullConstraint> newConstraints
    ) {
        final EqualityContainer<Polynomial> yPolynomial =
            this.buildPolynomialFromConstraint(x, yConstraint);
        final EqualityContainer<Polynomial> zPolynomial;
        if (zConstraint == null) {
            zPolynomial = new EqualityContainer<>(new Polynomial(3, 0), true);
        } else {
            zPolynomial = this.buildPolynomialFromConstraint(x, zConstraint);
        }
        final Polynomial result = basePolynomial
            .subtract(yPolynomial.getData().multiply(k1))
            .subtract(zPolynomial.getData().multiply(k1));
        final boolean newEqualSign = equalSign && yPolynomial.isEqual() && zPolynomial.isEqual();
        if (result.isValid() && ((result.getSize() == 2) || (result.getSize() == 3))) {
            final Monomial firstMonomial = result.getMonomial(0);
            final Monomial secondMonomial = result.getMonomial(1);
            @Nullable
            final Monomial thirdMonomial = (result.getSize() == 3) ? result.getMonomial(2) : null;
            final int constantCoefficient = result.getConstantCoefficient();
            if (firstMonomial.getVariable().equals(x)) {
                ConstraintsComputer.extractConstraints(
                    x,
                    firstMonomial,
                    secondMonomial,
                    thirdMonomial,
                    constantCoefficient,
                    constraintsToExplore,
                    newConstraints,
                    newEqualSign
                );
            } else if (secondMonomial.getVariable().equals(x)) {
                ConstraintsComputer.extractConstraints(
                    x,
                    secondMonomial,
                    firstMonomial,
                    thirdMonomial,
                    constantCoefficient,
                    constraintsToExplore,
                    newConstraints,
                    newEqualSign
                );
            } else if ((thirdMonomial != null) && thirdMonomial.equals(x)) {
                ConstraintsComputer.extractConstraints(
                    x,
                    thirdMonomial,
                    firstMonomial,
                    secondMonomial,
                    constantCoefficient,
                    constraintsToExplore,
                    newConstraints,
                    newEqualSign
                );
            }
        }
    }

    private static void extractConstraints(
        final Variable x,
        final Monomial firstMonomial,
        final Monomial secondMonomial,
        @Nullable final Monomial thirdMonomial,
        final int constantCoefficient,
        final @NotNull Collection<? super EqualityContainer<FullConstraint>> constraintsToExplore,
        final @NotNull Collection<? super FullConstraint> newConstraints,
        final boolean equalSign
    ) {
        if (
            (firstMonomial.getCoefficient() == 1) &&
            (
                (thirdMonomial == null) ||
                (secondMonomial.getCoefficient() == thirdMonomial.getCoefficient())
            )
        ) {
            final FullConstraint newConstraint = new FullConstraint(
                firstMonomial.getVariable(),
                secondMonomial.getVariable(),
                (thirdMonomial == null) ? null : thirdMonomial.getVariable(),
                -secondMonomial.getCoefficient(),
                -constantCoefficient
            );
            if (!firstMonomial.getVariable().equals(x)) {
                constraintsToExplore.add(new EqualityContainer<>(newConstraint, equalSign));
            }
            newConstraints.add(
                new FullConstraint(
                    firstMonomial.getVariable(),
                    secondMonomial.getVariable(),
                    (thirdMonomial == null) ? null : thirdMonomial.getVariable(),
                    -secondMonomial.getCoefficient(),
                    (equalSign) ? -constantCoefficient - 1 : -constantCoefficient
                )
            );
        }
    }

    private EqualityContainer<Polynomial> buildPolynomialFromConstraint(
        final Variable x,
        final Constraint constraint
    ) {
        boolean equalSign = false;
        if (constraint.getK1() == 1) {
            final Set<Constraint> constraintsToCheck = this.oldConstraints.get(constraint.getY());
            if (constraintsToCheck != null) {
                for (final Constraint constraintToCheck : constraintsToCheck) {
                    if (constraintToCheck.getY().equals(x)) {
                        equalSign = true;
                        break;
                    }
                }
            }
        }
        return new EqualityContainer<>(
            new PolynomialBuilder(3)
                .addMonomial(constraint.getK1(), constraint.getY())
                .addMonomial(constraint.getK1(), constraint.getZ())
                .setConstantCoefficient(constraint.getK2() + ((equalSign) ? 1 : 0))
                .build(),
            equalSign
        );
    }
    /*
    private void substituteVariables(
        final Variable x,
        final Variable y,
        @Nullable final Variable z,
        boolean substituteZ,
        final int k1,
        final int k2,
        final @NotNull Collection<? super FullConstraint> constraintsToExplore,
        final @NotNull Collection<? super FullConstraint> newConstraints
    ) {
        final Set<Constraint> yConstraints = this.oldConstraints.get(y);
        if (yConstraints != null) {
            final Polynomial basePolynomial = new PolynomialBuilder(3)
                .addMonomial(1, x)
                .setConstantCoefficient(-k2)
                .build();
            final Polynomial zero = new PolynomialBuilder(3).setConstantCoefficient(0).build();
            Set<Constraint> zConstraints = null;
            if ((z != null) && substituteZ) {
                zConstraints = this.oldConstraints.get(z);
            }
            if (zConstraints == null) {
                zConstraints = new HashSet<>();
            }

            for (final Constraint yConstraint : yConstraints) {
                Iterator<Constraint> zConstraintIterator = zConstraints.iterator();
                boolean onlyOneConstraintToProcess = zConstraints.isEmpty();

                while (zConstraintIterator.hasNext() || onlyOneConstraintToProcess) {
                    Constraint zConstraint = (zConstraintIterator.hasNext())
                        ? zConstraintIterator.next()
                        : null;
                    final Polynomial yPolynomial = ConstraintsComputer
                        .buildPolynomialFromConstraint(yConstraint)
                        .multiply(k1);
                    final Polynomial zPolynomial;
                    if (zConstraint == null) {
                        if (substituteZ) {
                            if (z == null) {
                                zPolynomial = zero;
                            } else {
                                zPolynomial = new Polynomial(3, z, k1);
                            }
                        } else {
                            zPolynomial = new Polynomial(3, z, k1);
                        }
                    } else {
                        zPolynomial =
                            ConstraintsComputer
                                .buildPolynomialFromConstraint(zConstraint)
                                .multiply(k1);
                    }
                    final Polynomial result = basePolynomial
                        .subtract(yPolynomial)
                        .subtract(zPolynomial);
                    if (result.isValid() && ((result.getSize() == 2) || (result.getSize() == 3))) {
                        final Monomial firstMonomial = result.getMonomial(0);
                        final Monomial secondMonomial = result.getMonomial(1);
                        @Nullable
                        final Monomial thirdMonomial = (result.getSize() == 3)
                            ? result.getMonomial(2)
                            : null;
                        final int constantCoefficient = result.getConstantCoefficient();
                        if (firstMonomial.getVariable().equals(x)) {
                            ConstraintsComputer.extractConstraint(
                                x,
                                firstMonomial,
                                secondMonomial,
                                thirdMonomial,
                                constantCoefficient,
                                constraintsToExplore,
                                newConstraints
                            );
                        } else if (secondMonomial.getVariable().equals(x)) {
                            ConstraintsComputer.extractConstraint(
                                x,
                                secondMonomial,
                                firstMonomial,
                                thirdMonomial,
                                constantCoefficient,
                                constraintsToExplore,
                                newConstraints
                            );
                        } else if ((thirdMonomial != null) && thirdMonomial.equals(x)) {
                            ConstraintsComputer.extractConstraint(
                                x,
                                thirdMonomial,
                                firstMonomial,
                                secondMonomial,
                                constantCoefficient,
                                constraintsToExplore,
                                newConstraints
                            );
                        }
                    }
                    if (zConstraints.isEmpty()) {
                        onlyOneConstraintToProcess = false;
                    }
                }
            }
        }
    }

    private static void extractConstraint(
        final Variable x,
        final Monomial firstMonomial,
        final Monomial secondMonomial,
        @Nullable final Monomial thirdMonomial,
        final int constantCoefficient,
        final @NotNull Collection<? super FullConstraint> constraintsToExplore,
        final @NotNull Collection<? super FullConstraint> newConstraints
    ) {
        if (
            (firstMonomial.getCoefficient() == 1) &&
            (
                (thirdMonomial == null) ||
                (secondMonomial.getCoefficient() == thirdMonomial.getCoefficient())
            )
        ) {
            final FullConstraint newConstraint = new FullConstraint(
                firstMonomial.getVariable(),
                secondMonomial.getVariable(),
                (thirdMonomial == null) ? null : thirdMonomial.getVariable(),
                -secondMonomial.getCoefficient(),
                -constantCoefficient
            );
            if (!firstMonomial.getVariable().equals(x)) {
                constraintsToExplore.add(newConstraint);
            }
            newConstraints.add(newConstraint);
        }
    }

    private static Polynomial buildPolynomialFromConstraint(final Constraint constraint) {
        return new PolynomialBuilder(3)
            .addMonomial(constraint.getK1(), constraint.getY())
            .addMonomial(constraint.getK1(), constraint.getZ())
            .setConstantCoefficient(constraint.getK2())
            .build();
    }*/
}
