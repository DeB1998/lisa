package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.symbolic.value.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-26
 * @since version date
 */
class FullConstraint {

    @NotNull
    private final Variable x;

    @NotNull
    private final Constraint constraint;

    public FullConstraint(@NotNull final Variable x, @NotNull final Constraint constraint) {
        this.x = x;
        this.constraint = constraint;
    }

    public FullConstraint(
        final Variable x,
        @NotNull final Variable y,
        @Nullable final Variable z,
        final int k1,
        final int k2
    ) {
        this(x, new Constraint(y, z, k1, k2));
    }

    @NotNull
    public Variable getX() {
        return this.x;
    }

    @NotNull
    public Variable getY() {
        return this.constraint.getY();
    }

    @Nullable
    public Variable getZ() {
        return this.constraint.getZ();
    }

    public int getK1() {
        return this.constraint.getK1();
    }

    public int getK2() {
        return this.constraint.getK2();
    }
    
    public @NotNull Constraint getConstraint() {
        
        return this.constraint;
    }
    
    public boolean differsOnlyOnK2(@NotNull final Constraint otherConstraint) {
        return this.constraint.differsOnlyOnK2(otherConstraint);
    }
    
    @Override
    public String toString() {
        
        return this.x + " → " + this.constraint;
    }
}
