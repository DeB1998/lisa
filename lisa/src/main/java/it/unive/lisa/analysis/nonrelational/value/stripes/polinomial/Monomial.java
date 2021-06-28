package it.unive.lisa.analysis.nonrelational.value.stripes.polinomial;

import it.unive.lisa.symbolic.value.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-05-16
 * @since version date
 */
public class Monomial {

    private final int coefficient;

    @Nullable
    private final Variable variable;

    Monomial(final int coefficient, @NotNull final Variable variable) {
        this(variable, coefficient);
    }

    private Monomial(@Nullable final Variable variable, final int coefficient) {
        this.coefficient = coefficient;
        this.variable = (coefficient == 0) ? null : variable;
    }

    Monomial() {
        this.coefficient = 0;
        this.variable = null;
    }

    public int getCoefficient() {
        return this.coefficient;
    }

    @NotNull
    public Variable getVariable() {
        if (this.variable == null) {
            throw new IllegalStateException("Null monomial!");
        }

        return this.variable;
    }

    @Nullable
    Monomial tryAdd(@NotNull final Monomial otherMonomial) {
        final Monomial result = new Monomial();

        if (
            (this.variable == null) ||
            (otherMonomial.variable == null) ||
            this.variable.equals(otherMonomial.variable)
        ) {
            final int resultCoefficient = this.coefficient + otherMonomial.coefficient;
            Variable resultVariable = this.variable;
            if ((this.variable == null)) {
                resultVariable = otherMonomial.variable;
            }

            return new Monomial(resultVariable, resultCoefficient);
        }
        return null;
    }

    @NotNull
    Monomial invert() {
        return new Monomial(this.variable, -this.coefficient);
    }

    @NotNull
    Monomial multiply(final int constant) {
        final int resultCoefficient = this.coefficient * constant;
        if (resultCoefficient == 0) {
            return new Monomial();
        }
        return new Monomial(this.variable, resultCoefficient);
    }

    @NotNull
    Monomial divide(final int constant) {
        if (constant == 0) {
            throw new ArithmeticException("Monomial division by 0");
        }
        return new Monomial(this.variable, this.coefficient / constant);
    }

    public boolean isNull() {
        return (this.coefficient == 0) && (this.variable == null);
    }

    @NotNull
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("Monomial { ");

        if (this.variable == null) {
            builder.append("NULL ");
        } else {
            builder.append(this.coefficient).append(this.variable).append(" ");
        }

        return builder.append('}').toString();
    }
}
