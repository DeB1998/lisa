package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.symbolic.value.Identifier;
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
    private final Identifier x;

    @Nullable
    private final Identifier y;

    private final int k1;

    private final int k2;

    Constraint(
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
            this.x.equals(otherConstraint.x) &&
            (Objects.equals(this.y, otherConstraint.y))
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

    @Override
    public String toString() {
        return "(" + this.x + ", " + ((this.y == null) ? "⊥" : this.y) + ", " + this.k1 + ", " + this.k2 + ')';
    }

    boolean isSameConstraint(@NotNull final Constraint other) {
        return (this.x.equals(other.x) && (this.k1 == other.k1) && Objects.equals(this.y, other.y));
    }
}
