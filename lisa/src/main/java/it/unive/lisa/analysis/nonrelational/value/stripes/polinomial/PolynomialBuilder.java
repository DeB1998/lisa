package it.unive.lisa.analysis.nonrelational.value.stripes.polinomial;

import it.unive.lisa.symbolic.value.Variable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for building a polynomial. It allows for adding monomials without creating
 * temporary polynomials.<br> Actually, this is the only way to create a polynomial.
 *
 * @author Alessio De Biasi
 * @author Jonathan Gobbo
 * @version 1.0.1 2021-06-29
 * @since 1.5 2021-04-17
 */
public final class PolynomialBuilder {

    /**
     * Sequence of monomials.
     */
    @NotNull
    private final Map<Variable, Integer> monomials;

    /**
     * Maximum number of monomials.
     */
    private final int monomialsCount;

    /**
     * Value of the constant, i.e., the monomial of degree 0.
     */
    private int constantCoefficient;

    /**
     * Creates a new polynomial builder with the specified maximum number of monomials.
     *
     * @param monomialsCount Maximum number of monomials allowed. The constant monomial
     *         (i.e., the monomial of degree 0) is excluded.
     */
    public PolynomialBuilder(final int monomialsCount) {
        this.monomialsCount = monomialsCount;
        this.monomials = new HashMap<>();
        this.constantCoefficient = 0;
    }

    /**
     * Adds the specified monomial.<br> If a monomial with the same variable has already been added,
     * the specified coefficient is added.<br> If either {@code variable} is {@code null} or {@code
     * coefficient} is {@code 0}, then no monomial is added.
     *
     * @param coefficient The coefficient of the monomial.
     * @param variable The variable fo the monomial.
     * @return This object in order to allow chaining.
     */
    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    @Contract(value = "_, _-> this", mutates = "this")
    @NotNull
    public PolynomialBuilder addMonomial(final int coefficient, @Nullable final Variable variable) {
        // CHeck the parameters
        if ((variable != null) && (coefficient != 0)) {
            // Add the monomial
            final int newCount = this.monomials.getOrDefault(variable, 0) + 1;
            this.monomials.put(variable, newCount);
        }
        // Allow chaining
        return this;
    }

    /**
     * Set the constant (i.e., the monomial of degree 0) of the polynomial.
     *
     * @param constantCoefficient The value of the constant.
     * @return This object in order to allow chaining.
     */
    @Contract(value = "_ -> this", mutates = "this")
    @NotNull
    public PolynomialBuilder setConstantCoefficient(final int constantCoefficient) {
        // Set the constant
        this.constantCoefficient = constantCoefficient;
        // Allow chaining
        return this;
    }

    /**
     * Builds the polynomial.
     *
     * @return The built polynomial or {@link Polynomial#INVALID} if more than {@code
     *         monomialsCount} monomials have been added (constant monomial excluded).
     */
    @NotNull
    public Polynomial build() {
        // Check the number of monomials
        final int monomialsSize = this.monomials.size();
        if (monomialsSize > this.monomialsCount) {
            return Polynomial.INVALID;
        }
        // Create the monomial container
        final Monomial[] newMonomials = new Monomial[this.monomialsCount];
        Arrays.fill(newMonomials, monomialsSize, this.monomialsCount, new Monomial());

        // Create the monomials
        int index = 0;
        for (final Entry<Variable, Integer> entry : this.monomials.entrySet()) {
            //noinspection ObjectAllocationInLoop
            newMonomials[index] = new Monomial(entry.getValue(), entry.getKey());
            index++;
        }

        // Create the polynomial
        return new Polynomial(
            newMonomials,
            this.constantCoefficient,
            this.monomialsCount,
            this.monomials.isEmpty()
        );
    }
}
