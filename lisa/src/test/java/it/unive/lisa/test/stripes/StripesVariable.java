package it.unive.lisa.test.stripes;

import it.unive.lisa.test.stripes.cfg.program.Variable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public class StripesVariable implements Variable<StripesVariable> {

    public static final StripesVariable TOP = new StripesVariable("⊤");

    public static final StripesVariable BOTTOM = new StripesVariable("⊥");

    private static class Constraint {

        @NotNull
        private final String y;

        @Nullable
        private final String z;

        private final int k1;
        private final int k2;

        public Constraint(
            @NotNull final String y,
            @Nullable final String z,
            final int k1,
            final int k2
        ) {
            this.y = y;
            this.z = z;
            this.k1 = k1;
            this.k2 = k2;
        }

        public @NotNull String getY() {
            return this.y;
        }

        public @Nullable String getZ() {
            return this.z;
        }

        public int getK1() {
            return this.k1;
        }

        public int getK2() {
            return this.k2;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;

            final Constraint that = (Constraint) o;

            if (this.k1 != that.k1) return false;
            if (this.k2 != that.k2) return false;

            if (this.y.equals(that.y)) {
                return Objects.equals(this.z, that.z);
            }
            if (this.y.equals(that.z)) {
                return Objects.equals(this.z, that.y);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = this.y.hashCode();
            result = 31 * result + (this.z != null ? this.z.hashCode() : 0);
            result = 31 * result + this.k1;
            result = 31 * result + this.k2;
            return result;
        }

        @Override
        public String toString() {
            return "(" + this.y + ", " + this.z + ", " + this.k1 + ", " + this.k2 + ')';
        }
    }

    @NotNull
    private final String variableName;

    @NotNull
    private final List<Constraint> constraints;

    public StripesVariable(@NotNull final String variableName) {
        this.variableName = variableName;
        this.constraints = new LinkedList<>();
    }

    public StripesVariable(@NotNull final String variableName, final List<Constraint> constraints) {
        this.variableName = variableName;
        this.constraints = new LinkedList<>(constraints);
    }

    public StripesVariable add(
        @NotNull final StripesVariable y,
        @Nullable final StripesVariable z,
        final int k1,
        final int k2
    ) {
        this.constraints.add(
                new Constraint(y.variableName, (z == null) ? null : z.variableName, k1, k2)
            );
        return this;
    }

    public StripesVariable remove(@NotNull final StripesVariable x) {
        this.constraints.removeIf(
                constraint ->
                    constraint.getY().equals(x.variableName) ||
                    ((constraint.getZ() != null) && constraint.getZ().equals(x.variableName))
            );

        return this;
    }

    public StripesVariable clearAndAdd(
        @NotNull final StripesVariable y,
        @Nullable final StripesVariable z,
        final int k1,
        final int k2
    ) {
        this.clear();
        this.add(y, z, k1, k2);

        return this;
    }

    public StripesVariable clear() {
        this.constraints.clear();
        return this;
    }

    @Override
    public StripesVariable copy() {
        return new StripesVariable(this.variableName, this.constraints);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final StripesVariable that = (StripesVariable) o;

        if (!this.variableName.equals(that.variableName)) {
            return false;
        }
        if (this.constraints.size() != that.constraints.size()) {
            return false;
        }
        for (final Constraint firstConstraint : this.constraints) {
            boolean found = false;
            for (final Constraint secondConstraint : that.constraints) {
                if (firstConstraint.equals(secondConstraint)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = this.variableName.hashCode();
        result = 31 * result + this.constraints.hashCode();
        return result;
    }

    @Override
    public String toString() {
        if (this == TOP) {
            return "⊤";
        }
        if (this == BOTTOM) {
            return "⊥";
        }

        return this.variableName + " → {" + this.constraints + '}';
    }
}
