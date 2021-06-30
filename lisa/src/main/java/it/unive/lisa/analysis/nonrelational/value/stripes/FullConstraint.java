package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.symbolic.value.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class that holds a constraint and the variables the constraint is associated to.
 *
 * @author Alessio De Biasi
 * @author Jonathan Gobbo
 * @version 1.1 2021-06-27
 * @since 1.5 2021-04-17
 */
final class FullConstraint {

    /**
     * Variable the constraint is associated to.
     */
    @SuppressWarnings("FieldNamingConvention")
    @NotNull
    private final Variable x;

    /**
     * Constraint associated to the variable.
     */
    @NotNull
    private final Constraint constraint;

    /**
     * Associates a variable with a constraint.
     *
     * @param x Variable the constraint is associated to.
     * @param y Variable {@code y} of the constraint.
     * @param z Vhe variable {@code z} of the constraint.
     * @param k1 Constant {@code k}<sub>{@code 1}</sub> of the constraint.
     * @param k2 Constant {@code k}<sub>{@code 2}</sub> of the constraint.
     */
    FullConstraint(
        @NotNull final Variable x,
        @NotNull final Variable y,
        @Nullable final Variable z,
        final int k1,
        final int k2
    ) {
        this.x = x;
        this.constraint = new Constraint(y, z, k1, k2);
    }

    /**
     * Returns the variable the constraint is associated to.
     *
     * @return The variable the constraint is associated to.
     */
    @NotNull
    public Variable getX() {
        return this.x;
    }

    /**
     * Returns the variable {@code y} of the constraint.
     *
     * @return The variable {@code y} of the constraint.
     */
    @NotNull
    public Variable getY() {
        return this.constraint.getY();
    }

    /**
     * Returns the variable {@code z} of the constraint.
     *
     * @return The variable {@code z} of the constraint.
     */
    @Nullable
    public Variable getZ() {
        return this.constraint.getZ();
    }

    /**
     * Returns the constant {@code k}<sub>{@code 1}</sub> of the constraint.
     *
     * @return The constant {@code k}<sub>{@code 1}</sub> of the constraint.
     */
    int getK1() {
        return this.constraint.getK1();
    }

    /**
     * Returns the constant {@code k}<sub>{@code 2}</sub> of the constraint.
     *
     * @return The constant {@code k}<sub>{@code 2}</sub> of the constraint.
     */
    public int getK2() {
        return this.constraint.getK2();
    }

    /**
     * Returns the constraint associated to the variable.
     *
     * @return The constraint associated to the variable.
     */
    @NotNull
    Constraint getConstraint() {
        return this.constraint;
    }

    /**
     * Check if the associated constraint and the specified one differ only on constant {@code
     * k}<sub>{@code 2}</sub>. This is equivalent as:
     * <pre>  this.getConstraint().differsOnlyOnK2(otherConstraint);</pre>
     *
     * @param otherConstraint The constraint to check.
     * @return {@code true} if the constraint associated to the variable and the specified
     *         constraint differ only on constant {@code k}<sub>{@code 2}</sub>, {@code false}
     *         otherwise.
     */
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    boolean differsOnlyOnK2(@NotNull final Constraint otherConstraint) {
        return this.constraint.differsOnlyOnK2(otherConstraint);
    }

    /**
     * Creates a string representation of the association.
     *
     * @return A string representation of the association.
     */
    @Override
    public String toString() {
        return this.x + " → " + this.constraint;
    }
}
