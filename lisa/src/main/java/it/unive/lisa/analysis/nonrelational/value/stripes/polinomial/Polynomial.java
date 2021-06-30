package it.unive.lisa.analysis.nonrelational.value.stripes.polinomial;

import java.util.Arrays;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class that represents a polynomial. A polynomial holds a sequence of monomials that differs in
 * the variable, i.e., it is not possible that a polynomial holds two monomials with the same
 * variable.<br> This class is immutable, i.e., every operation between two polynomials creates a
 * new polynomial that holds the result.<br> This class holds a fixed number of monomials. When the
 * result of an operation exceeds this number, {@link Polynomial#INVALID} is returned. This
 * polynomial is the ONLY one whose {@link Polynomial#isValid()} returns {@code false}.
 *
 * @author Alessio De Biasi
 * @author Jonathan Gobbo
 * @version 1.3 2021-06-29
 * @since 1.5 2021-04-17
 */
public final class Polynomial {

    /**
     * Invalid polynomial. A polynomial is invalid when it is requested to hold more monomials than
     * the maximum allowed.
     */
    @NotNull
    public static final Polynomial INVALID = new Polynomial();

    /**
     * Sequence of monomials.
     */
    @NotNull
    private final Monomial[] monomials;

    /**
     * Value of the monomial with degree 0.
     */
    private final int constantCoefficient;

    /**
     * Maximum number of monomials.
     */
    private final int monomialsCount;

    /**
     * Flag that states if this polynomial is constant or not.
     */
    private final boolean isConstantPolynomial;

    /**
     * Creates the invalid polynomial.
     */
    private Polynomial() {
        // Call the other constructor
        this(new Monomial[1], 0, 0, false);
        // Initialize the monomials
        this.monomials[0] = new Monomial();
    }

    /**
     * Creates a new polynomial with the given monomial.
     *
     * @param monomials Array of monomials. This array is cloned, so, modify the specify
     *         array after constructing this object does not affect the saved array.
     * @param constantCoefficient Value of the constant monomial.
     * @param monomialsCount Maximum allowed monomials, constant monomial excluded.
     * @param isConstantPolynomial {@code true} if this polynomial represents a constant
     *         value, false otherwise.
     */
    Polynomial(
        @NotNull final Monomial[] monomials,
        final int constantCoefficient,
        final int monomialsCount,
        final boolean isConstantPolynomial
    ) {
        // Initialize the polynomial
        this.monomials = monomials.clone();
        this.constantCoefficient = constantCoefficient;
        this.monomialsCount = monomialsCount;
        this.isConstantPolynomial = isConstantPolynomial;
    }

    /**
     * Returns {@code false} if this polynomial is the invalid polynomial {@link
     * Polynomial#INVALID}, {@code true} otherwise.
     *
     * @return {@code false} if this polynomial is the invalid polynomial, {@code true} otherwise.
     */
    public boolean isValid() {
        // Check if this polynomial is invalid
        return this != Polynomial.INVALID;
    }

    /**
     * Returns the number of non-null monomials in this polynomial, constant monomial (i.e.,
     * monomial of degree 0) excluded.
     *
     * @return The number of non-null monomials in this polynomial.
     * @throws IllegalStateException If this method is called on the invalid polynomial.
     */
    public int getSize() {
        // Check if this polynomial is invalid
        if (this.isValid()) {
            // Find the first non-null monomial
            int i = this.monomialsCount - 1;
            boolean continueSearch = true;
            while ((i >= 0) && continueSearch) {
                if (this.monomials[i].isNull()) {
                    i--;
                } else {
                    continueSearch = false;
                }
            }
            // Return the index of the first non-null monomial
            return i + 1;
        }
        throw new IllegalStateException("Cannot invoke getSize() on the invalid polynomial");
    }

    /**
     * Returns {@code true} if this polynomial is constant, {@code false} otherwise. The invalid
     * polynomial is considered non-constant.
     *
     * @return {@code true} if this polynomial is constant, {@code false} otherwise.
     */
    public boolean isConstantPolynomial() {
        return this.isConstantPolynomial && this.isValid();
    }

    /**
     * Returns the coefficient of the constant monomial (i.e., the monomial with degree 0).
     *
     * @return The coefficient of the constant monomial.
     * @throws IllegalStateException If this method is called on the invalid polynomial.
     */
    public int getConstantCoefficient() {
        if (this.isValid()) {
            return this.constantCoefficient;
        }
        throw new IllegalStateException(
            "Cannot extract the constant coefficient on the invalid polynomial"
        );
    }

    /**
     * Returns the monomial at the specified index. Note that there is no implicit order in the
     * monomials.
     *
     * @param index The index of the requested monomial.
     * @return The requested monomial.
     * @throws IllegalArgumentException If {@code index < 0}, if {@code index >= monomialsCount}, or
     *                                  if the monomial at the specified is a null monomial.
     */
    @NotNull
    public Monomial getMonomial(final int index) {
        // Check if the polynomial is valid
        if (this.isValid()) {
            // Check if the index is valid
            if ((index >= 0) && (index < this.monomialsCount)) {
                // Extract the monomial
                final Monomial requestedMonomial = this.monomials[index];
                // Return only non-null monomials
                if (!requestedMonomial.isNull()) {
                    return requestedMonomial;
                }
                throw new IllegalArgumentException("Monomial at index " + index + " is null");
            }
            throw new IllegalArgumentException(
                "Invalid index " +
                index +
                ": it must be greater than or equal to 0 and must " +
                "be less than than monomialsCount"
            );
        }
        throw new IllegalStateException("Cannot extract monomials from the invalid polynomial");
    }

    /**
     * Utility method that checks if this polynomial is divisible by the specified constant. A
     * polynomial is considered divisible by an integer constant if and only if all the monomials
     * coefficients (excluding the constant monomial) are multiples of the constant.<br> If the
     * constant is 0, then the polynomial is not divisible.
     *
     * @param constant The constant to check.
     * @return {@code true} if this polynomial is divisible by the specified constant, false
     *         otherwise.
     */
    private boolean isDivisible(final int constant) {
        boolean isDivisible = constant != 0;
        int i = 0;
        // Check that all monomials are multiples of the specified constant
        while ((i < this.monomialsCount) && isDivisible) {
            isDivisible = ((this.monomials[i].getCoefficient() % constant) == 0);
            i++;
        }
        return isDivisible;
    }

    /**
     * Computes the result of the division of this polynomial by the given constant. The division is
     * possible if and only if the specified constant is not 0 and all the monomials coefficients
     * (excluding the constant monomial) are multiples of the constant.<br> The invalid polynomial
     * is not divisible.
     *
     * @param constant The constant to divide this polynomial by.
     * @return The result of the division, or {@link Polynomial#INVALID} if the division is not
     *         possible.
     */
    @NotNull
    public Polynomial divide(final int constant) {
        // Check if the division can be performed
        if (this.isValid() && this.isDivisible(constant)) {
            // Creates the new monomials
            final Monomial[] newMonomials = IntStream
                .range(0, this.monomialsCount)
                .mapToObj(i -> this.monomials[i].divide(constant))
                .toArray(Monomial[]::new);

            // Compute the new coefficient
            final int newCoefficient = this.constantCoefficient / constant;
            return new Polynomial(
                newMonomials,
                newCoefficient,
                this.monomialsCount,
                this.isConstantPolynomial
            );
        }
        // The division is not possible
        return Polynomial.INVALID;
    }

    /**
     * Computes the result of this polynomial modulo the specified constant. This operation can be
     * performed only if all the monomials coefficients (excluding the constant monomials) are
     * multiples of the specified constant.<br> This operation, applied to the invalid polynomial,
     * gives the invalid polynomial as result.<br>Note that whatever monomial modulo a constant, if
     * its coefficient is a multiple of such constant, gives as result 0.
     *
     * @param constant The constant to use for the modulo operation.
     * @return The result of the modulo operation, or {@link Polynomial#INVALID} if the operation
     *         cannot be performed.
     */
    @NotNull
    public Polynomial modulo(final int constant) {
        // Check if the operation can be performed
        if (this.isValid() && this.isDivisible(constant)) {
            // Clear all the monomials
            final Monomial[] newMonomials = new Monomial[this.monomialsCount];
            Arrays.fill(newMonomials, new Monomial());

            // Apply the modulo operation only to the constant coefficient
            return new Polynomial(newMonomials, this.constantCoefficient % constant, 0, true);
        }
        return Polynomial.INVALID;
    }

    /**
     * Multiplies this polynomial by the specified constant.<br> Multiply the invalid polynomial
     * gives as result the invalid polynomial.
     *
     * @param constant The constant this polynomial will be multiplied by.
     * @return The result of the multiplication.
     */
    @NotNull
    public Polynomial multiply(final int constant) {
        // CHeck if the polynomial is valid
        if (this.isValid()) {
            // Create the monomials container
            final Monomial[] newMonomials;
            final boolean isResultConstant = (this.isConstantPolynomial) || (constant == 0);
            // Check whether the result polynomial will be constant
            if (isResultConstant) {
                newMonomials = new Monomial[this.monomialsCount];
                // Clear all the monomials
                Arrays.fill(newMonomials, new Monomial());
            } else {
                // Multiply all the monomials
                newMonomials =
                    IntStream
                        .range(0, this.monomialsCount)
                        .mapToObj(i -> this.monomials[i].multiply(constant))
                        .toArray(Monomial[]::new);
            }

            // Compute the new coefficient
            final int newCoefficient = this.constantCoefficient * constant;

            // Create the result
            return new Polynomial(
                newMonomials,
                newCoefficient,
                this.monomialsCount,
                isResultConstant
            );
        }
        return Polynomial.INVALID;
    }

    /**
     * Computes the negation of this polynomial. Apply this operation to the invalid polynomial has
     * no effect.
     *
     * @return The negation of this polynomial.
     */
    @NotNull
    public Polynomial negate() {
        // Check if the polynomial is valid
        if (this.isValid()) {
            // Negate the monomials
            final Monomial[] newMonomials = IntStream
                .range(0, this.monomialsCount)
                .mapToObj(i -> this.monomials[i].negate())
                .toArray(Monomial[]::new);
            // Negate the constant
            final int resultConstant = -this.constantCoefficient;

            // Create te result
            return new Polynomial(
                newMonomials,
                resultConstant,
                this.monomialsCount,
                this.isConstantPolynomial
            );
        }
        return Polynomial.INVALID;
    }

    /**
     * Computes the sum of this polynomial and the specified one. If at least one of the two
     * polynomials is invalid or if the result needs more than {@code monomialsCount} monomials,
     * then the result is the invalid polynomial.
     *
     * @param otherPolynomial The polynomial to sum to this one.
     * @return The result of the sum of this polynomial and the specified one.
     */
    @SuppressWarnings("FeatureEnvy")
    @NotNull
    public Polynomial sum(@NotNull final Polynomial otherPolynomial) {
        // Check if both polynomials are valid
        if (!this.isValid() || !otherPolynomial.isValid()) {
            return Polynomial.INVALID;
        }
        // Create the result container
        final Monomial[] newMonomials = new Monomial[this.monomialsCount];
        Arrays.fill(newMonomials, new Monomial());
        // Clone the monomials to add, so to delete them when added
        @Nullable
        final Monomial[] monomialsToSum = otherPolynomial.monomials.clone();
        // Try to sum all the monomials
        int nextMonomialIndex = 0;
        for (int i = 0; i < this.monomialsCount; i++) {
            // Find a monomial with the same variable or a null monomial
            boolean toSum = true;
            int j = 0;
            while ((j < otherPolynomial.monomialsCount) && toSum) {
                // Extract the monomial to add
                final Monomial monomialToAdd = monomialsToSum[j];
                // Check if it has already been added
                if (monomialToAdd != null) {
                    // Try to sum the monomials
                    final Monomial result = this.monomials[i].trySum(monomialToAdd);
                    // Check if the sum has been performed
                    if (result != null) {
                        // Add the monomial is it is not null
                        if (!result.isNull()) {
                            newMonomials[nextMonomialIndex] = result;
                            nextMonomialIndex++;
                        }
                        // Delete the added monomial
                        //noinspection AssignmentToNull
                        monomialsToSum[j] = null;
                        toSum = false;
                    }
                }
                j++;
            }
            // Check if the monomial has been correctly summed
            if (toSum) {
                return Polynomial.INVALID;
            }
        }
        // Compute the new constant coefficient
        final int newConstant = this.constantCoefficient + otherPolynomial.constantCoefficient;

        // Return the result
        return new Polynomial(
            newMonomials,
            newConstant,
            this.monomialsCount,
            newMonomials[0].isNull()
        );
    }

    /**
     * Computes the subtraction of this polynomial and the specified one. If at least one of the two
     * polynomials is invalid or if the result needs more than {@code monomialsCount} monomials,
     * then the result is the invalid polynomial.
     *
     * @param otherPolynomial The polynomial to subtract to this one.
     * @return The result of the subtraction.
     */
    @NotNull
    public Polynomial subtract(@NotNull final Polynomial otherPolynomial) {
        // Compute -((-this) + otherPolynomial)
        return this.negate().sum(otherPolynomial).negate();
    }

    /**
     * Returns a string representation of this polynomial.
     *
     * @return A string representation of this polynomial.
     */
    @SuppressWarnings("FeatureEnvy")
    @NotNull
    @Override
    public String toString() {
        // Manage the invalid polynomial
        if (!this.isValid()) {
            return " Polynomial [INVALID]";
        }
        // Print the size and monomialsCount
        final StringBuilder builder = new StringBuilder("Polynomial [")
            .append(this.getSize())
            .append('/')
            .append(this.monomialsCount)
            .append(']')
            .append(" { ");

        // Print the monomials
        for (int i = 0; i < this.getSize(); i++) {
            // Extract the monomial
            final Monomial monomial = this.monomials[i];
            // Skip null monomials

            // Add the sign
            if ((monomial.getCoefficient() >= 0) && (i > 0)) {
                builder.append(" + ");
            }
            if (monomial.getCoefficient() < 0) {
                builder.append('-');
                if (i > 0) {
                    builder.append(' ');
                }
            }
            // Append the monomial
            builder
                .append(Math.abs(monomial.getCoefficient()))
                .append(" · ")
                .append(monomial.getVariable())
                .append(' ');
        }
        // Append the constant
        if (this.getSize() == 0) {
            builder.append(' ');
        }
        if (this.constantCoefficient < 0) {
            builder.append("- ");
        } else {
            builder.append("+ ");
        }
        builder.append(Math.abs(this.constantCoefficient));
        builder.append(" }");
        // Check if the polynomial is constant
        if (this.isConstantPolynomial) {
            builder.append(" : const");
        }

        // Return the representation
        return builder.toString();
    }
}
