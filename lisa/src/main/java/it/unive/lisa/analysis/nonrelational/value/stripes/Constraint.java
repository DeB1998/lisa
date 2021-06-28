package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.symbolic.value.Variable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-05-01
 * @since version date
 */
final class Constraint {

    @NotNull
    private final Variable y;

    @Nullable
    private final Variable z;

    private final int k1;

    private final int k2;

    Constraint(@NotNull final Variable y, @Nullable final Variable z, final int k1, final int k2) {
        this.y = y;
        this.z = z;

        this.k1 = k1;
        this.k2 = k2;
    }

    @NotNull
    Variable getY() {
        return this.y;
    }

    @Nullable
    Variable getZ() {
        return this.z;
    }

    int getK1() {
        return this.k1;
    }

    int getK2() {
        return this.k2;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }

        final Constraint otherConstraint = (Constraint) obj;

        return (
            (this.k1 == otherConstraint.k1) &&
            (this.k2 == otherConstraint.k2) &&
            this.y.equals(otherConstraint.y) &&
            (Objects.equals(this.z, otherConstraint.z))
        );
    }

    @Override
    public int hashCode() {
        int result = this.y.hashCode();
        result = (31 * result) + ((this.z != null) ? this.z.hashCode() : 0);
        result = (31 * result) + this.k1;
        result = (31 * result) + this.k2;
        return result;
    }

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

    boolean differsOnlyOnK2(@NotNull final Constraint other) {
        return (
            (this.y.equals(other.y) || this.y.equals(other.z)) &&
            (Objects.equals(this.z, other.z) || other.y.equals(this.z)) &&
            (this.k1 == other.k1)
        );
    }
}
