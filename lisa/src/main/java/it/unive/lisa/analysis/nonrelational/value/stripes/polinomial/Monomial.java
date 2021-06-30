package it.unive.lisa.analysis.nonrelational.value.stripes.polinomial;

import it.unive.lisa.symbolic.value.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class that represents a monomial. This class implements all the operations between two monomials
 * that results in a monomial.<br> This class is immutable, i.e., every operation between two
 * monomials creates a new resulting monomial.
 *
 * @author Alessio De Biasi
 * @author Jonathan Gobbo
 * @version 1.2 2021-06-29
 * @since 1.5 2021-04-17
 */
public final class Monomial {

    /**
     * Coefficient of the monomial.
     */
    private final int coefficient;

    /**
     * Variable of the monomial. If the monomial has coefficient 0, then this field is {@code
     * null}.
     */
    @Nullable
    private final Variable variable;

    /**
     * Creates an empty monomial, i.e., a monomial that represents the constant {@code 0}.
     */
    Monomial() {
        this.coefficient = 0;
        this.variable = null;
    }

    /**
     * Creates a new monomial with the specified coefficient and the specified variable.
     *
     * @param coefficient The coefficient of the monomial.
     * @param variable The variable of the monomial.
     */
    Monomial(final int coefficient, @NotNull final Variable variable) {
        // Call the other constructor
        this(variable, coefficient);
    }

    /**
     * Creates a new monomial. This constructor is used internally to create new instances of this
     * class in order to represent the result of the operations.<br> Since in order to save memory
     * the monomial does not hold the reference to the variable if the coefficient if 0, the other
     * constructor cannot be used to construct the monomials that represent the result of
     * operations.
     *
     * @param variable The variable of the monomial or {@code null} if the coefficient is
     *         0.
     * @param coefficient The coefficient of the monomial.
     */
    private Monomial(@Nullable final Variable variable, final int coefficient) {
        //noinspection VariableNotUsedInsideIf
        this.coefficient = (variable == null) ? 0 : coefficient;
        this.variable = (coefficient == 0) ? null : variable;
    }

    /**
     * Returns the coefficient of this monomial.
     *
     * @return The coefficient of this monomial.
     */
    public int getCoefficient() {
        return this.coefficient;
    }

    /**
     * Return the variable of this monomial.
     *
     * @return The variable of this monomial.
     * @throws IllegalStateException If this monomial represents a constant value.
     */
    @NotNull
    public Variable getVariable() {
        // Check that the monomial is not a constant
        if (this.variable == null) {
            throw new IllegalStateException("Cannot extract the variable from a constant monomial");
        }

        return this.variable;
    }

    /**
     * Tries to compute the sum between this monomial and the specified one, i.e., it computes
     * {@code this + otherMonomial}. Two monomials can be summed only if:
     * <ul>
     *     <li>At least one of them is constant;</li>
     *     <li>They are defined on the same variable, possibly with different coefficients.</li>
     * </ul>
     *
     * @param otherMonomial The other monomial to sum.
     * @return The result of the sum, or {@code null} if the monomials to sum are defined on
     *         different variables.
     */
    @Nullable
    Monomial trySum(@NotNull final Monomial otherMonomial) {
        // Check if the sum can be performed
        if (
            (this.variable == null) ||
            (otherMonomial.variable == null) ||
            this.variable.equals(otherMonomial.variable)
        ) {
            // Sum the coefficients
            final int resultCoefficient = this.coefficient + otherMonomial.coefficient;
            // Compute
            Variable resultVariable = this.variable;
            if ((this.variable == null)) {
                resultVariable = otherMonomial.variable;
            }

            return new Monomial(resultVariable, resultCoefficient);
        }
        return null;
    }

    /**
     * Computes the negation of this monomial, i.e., it computes {@code -(this)}.
     *
     * @return The negation of this monomial.
     */
    @NotNull
    Monomial negate() {
        // Negate the coefficient
        return new Monomial(this.variable, -this.coefficient);
    }

    /**
     * Multiplies this monomial by a constant value, i.e., it computes {@code constant * this}.
     *
     * @param constant The value this monomial will be multiplied by.
     * @return The result of the multiplication of this monomial by the given constant value.
     */
    @NotNull
    Monomial multiply(final int constant) {
        // Compute the new constant
        final int resultCoefficient = this.coefficient * constant;
        // If the new constant is zero, then the resulting monomial is constant
        if (resultCoefficient == 0) {
            return new Monomial();
        }
        // Return the result
        return new Monomial(this.variable, resultCoefficient);
    }

    /**
     * Divides this monomial by a constant value, i.e., it computes {@code this / constant}.
     *
     * @param constant The value this monomial will be divided by.
     * @return The result of the division of this monomial by the given constant value.
     * @throws ArithmeticException If the specified constant is {@code 0}.
     */
    @NotNull
    Monomial divide(final int constant) {
        // Check the constant
        if (constant == 0) {
            throw new ArithmeticException("Cannot divide a monomial by 0");
        }
        // Compute the result
        return new Monomial(this.variable, this.coefficient / constant);
    }

    /**
     * Checks whether this monomial is null, i.e., it checks if its coefficient is 0.
     *
     * @return {@code true} if this monomial is null, {@code false} otherwise.
     */
    public boolean isNull() {
        return (this.coefficient == 0) && (this.variable == null);
    }

    /**
     * Creates a string representation of this monomial.
     *
     * @return A string representation of this monomial.
     */
    @NotNull
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("Monomial { ");
        // Check whether the monomial is null
        if (this.variable == null) {
            builder.append("NULL ");
        } else {
            builder.append(this.coefficient).append(this.variable).append(' ');
        }
        // Return the representation
        return builder.append('}').toString();
    }
}
