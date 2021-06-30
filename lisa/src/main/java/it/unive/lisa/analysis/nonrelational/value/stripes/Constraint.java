package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.symbolic.value.Variable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class that represents a constraint {@code (y, z, k}<sub>{@code 1}</sub>, {@code k}<sub>{@code
 * 2}</sub>{@code )}.
 *
 * @author Alessio De Biasi
 * @author Jonathan Gobbo
 * @version 1.2 2021-06-27
 * @since 1.5 2021-04-17
 */
@SuppressWarnings("FieldNamingConvention")
final class Constraint {

    /**
     * Variable {@code y} of the constraint.
     */
    @NotNull
    private final Variable y;

    /**
     * Variable {@code z} of the constraint. {@code null} means that the variable is absent.
     */
    @Nullable
    private final Variable z;

    /**
     * Multiplicative constant of {@code (y [+z])}.
     */
    private final int k1;

    /**
     * Constant that is greater than {@code x - this}.
     */
    private final int k2;

    /**
     * Creates a new constraint.
     *
     * @param y Variable {@code y}.
     * @param z Variable {@code z}. {@code null} means that the variable is absent.
     * @param k1 Multiplicative constant of {@code (y [+z])}.
     * @param k2 Constant that is greater than {@code x - this}.
     */
    Constraint(@NotNull final Variable y, @Nullable final Variable z, final int k1, final int k2) {
        // Save the fields
        this.y = y;
        this.z = z;
        this.k1 = k1;
        this.k2 = k2;
    }

    /**
     * Returns the variable {@code y}.
     *
     * @return The variable {@code y}.
     */
    @NotNull
    Variable getY() {
        return this.y;
    }

    /**
     * Returns the variable {@code z}. {@code null} means that the variable is absent.
     *
     * @return The variable {@code z} or {@code null} if the variable is absent.
     */
    @Nullable
    Variable getZ() {
        return this.z;
    }

    /**
     * Returns the multiplicative constant of {@code (y [+z])}.
     *
     * @return the multiplicative constant of {@code (y [+z])}.
     */
    int getK1() {
        return this.k1;
    }

    /**
     * Returns the constant that is greater than {@code x - this}.
     *
     * @return The constant that is greater than {@code x - this}.
     */
    int getK2() {
        return this.k2;
    }

    /**
     * Checks if this object represents the same constraint equal to the specified one.<br> Note
     * that two constraints are the same even if they mention the same variables but in different
     * order. Therefore, the constraint {@code (y, z, 3, 5)} is equal to the constraint {@code (z,
     * y, 3, 5)}.
     *
     * @param obj Object to test equality with.
     * @return {@code true} if this object represents the same constraint as the specified one,
     *         {@code false} otherwise.
     */
    @SuppressWarnings("FeatureEnvy")
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }

        final Constraint otherConstraint = (Constraint) obj;
        // Check the two constraints
        return (
            (this.y.equals(otherConstraint.y) || this.y.equals(otherConstraint.getZ())) &&
            (Objects.equals(this.z, otherConstraint.z) || otherConstraint.y.equals(this.z)) &&
            (this.k1 == otherConstraint.k1) &&
            (this.k2 == otherConstraint.k2)
        );
    }

    /**
     * Computes the hash code of this object.
     *
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        // Two constraints that mention the same variables are the same constraint
        final int yHashCode = this.y.hashCode();
        final int zHashCode = (this.z != null) ? this.z.hashCode() : 0;

        int result = (yHashCode + zHashCode);
        result = (31 * result) + (yHashCode + zHashCode);
        result = (31 * result) + this.k1;
        result = (31 * result) + this.k2;
        return result;
    }

    /**
     * Creates a string representation of this constraint.
     *
     * @return A string representation of this constraint.
     */
    @NotNull
    @Override
    public String toString() {
        return (
            "(" +
            this.y +
            ", " +
            ((this.z == null) ? "⊥" : this.z) +
            ", " +
            this.k1 +
            ", " +
            this.k2 +
            ')'
        );
    }

    /**
     * Checks whether the specified constraint differs from this one only for the value of {@code
     * k}<sub>{@code 2}</sub>.<br> Note that two constraints differs only on the value of {@code
     * k}<sub>{@code 2}</sub> if they mention the same variables regardless of their order.
     * Therefore the constraint {@code (y, z, 3, 5)} and the constraint {@code (z, y, 3, 8)} differ
     * only on the value of {@code k}<sub>{@code 2}</sub>.
     *
     * @param other The constraint to check.
     * @return {@code true} if the specified constraint differs only on the value of {@code k2},
     *         {@code false} otherwise.
     */
    @SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion", "FeatureEnvy" })
    boolean differsOnlyOnK2(@NotNull final Constraint other) {
        return (
            (this.y.equals(other.y) || this.y.equals(other.z)) &&
            (Objects.equals(this.z, other.z) || other.y.equals(this.z)) &&
            (this.k1 == other.k1)
        );
    }
}
