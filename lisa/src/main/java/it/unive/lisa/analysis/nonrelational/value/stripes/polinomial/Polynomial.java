package it.unive.lisa.analysis.nonrelational.value.stripes.polinomial;

import it.unive.lisa.symbolic.value.Variable;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-05-16
 * @since version date
 */
public class Polynomial {

    public static final Polynomial INVALID = new Polynomial(false);

    @NotNull
    private final Monomial[] monomials;

    private final int constantCoefficient;

    private final int monomialCount;

    private final boolean isConstantPolynomial;

    private final boolean valid;

    public Polynomial(final int monomialCount, final int constantCoefficient) {
        this(new Monomial[monomialCount], constantCoefficient, monomialCount, true);
        Arrays.fill(this.monomials, new Monomial());
    }

    public Polynomial(final int monomialCount, final Variable variable, final int coefficient) {
        this(new Monomial[monomialCount], 0, monomialCount, coefficient == 0);
        Arrays.fill(this.monomials, new Monomial());
        this.monomials[0] = new Monomial(coefficient, variable);
    }

    private Polynomial(final boolean isValid) {
        this.monomials = new Monomial[1];
        //noinspection AssignmentToNull
        this.monomials[0] = new Monomial();
        this.constantCoefficient = 0;
        this.monomialCount = 0;
        this.isConstantPolynomial = false;
        this.valid = isValid;
    }

    Polynomial(
        @NotNull final Monomial[] monomials,
        final int constantCoefficient,
        final int monomialCount,
        final boolean isConstantPolynomial
    ) {
        this.monomials = monomials;
        this.constantCoefficient = constantCoefficient;
        this.monomialCount = monomialCount;
        this.isConstantPolynomial = isConstantPolynomial;
        this.valid = true;
    }

    public boolean isValid() {
        return this.valid;
    }

    public int getSize() {
        if (this.valid) {
            int i = this.monomialCount - 1;
            boolean found = false;
            while ((i >= 0) && !found) {
                if (!this.monomials[i].isNull()) {
                    found = true;
                } else {
                    i--;
                }
            }
            return i + 1;
        }
        throw new IllegalStateException("Null polynomial!");
    }

    public boolean isConstantPolynomial() {
        return this.isConstantPolynomial && this.valid;
    }

    public int getConstantCoefficient() {
        if (this.valid) {
            return this.constantCoefficient;
        }
        throw new IllegalStateException("Null polynomial!");
    }

    @NotNull
    public Monomial getMonomial(final int index) {
        if (this.valid && (index >= 0)) {
            final Monomial requestedMonomial = this.monomials[index];
            if (!requestedMonomial.isNull()) {
                return requestedMonomial;
            }
        }
        throw new IllegalArgumentException("Invalid index " + index);
    }

    private boolean isDivisible(final int constant) {
        boolean isDivisible = constant != 0;
        int i = 0;
        while ((i < this.monomialCount) && isDivisible) {
            isDivisible = ((this.monomials[i].getCoefficient() % constant) == 0);
            i++;
        }
        return true;
    }

    @NotNull
    public Polynomial divide(final int constant) {
        if (this.valid && this.isDivisible(constant)) {
            final Monomial[] newMonomials = new Monomial[this.monomialCount];
            Arrays.fill(newMonomials, new Monomial());

            for (int i = 0; i < this.monomialCount; i++) {
                newMonomials[i] = this.monomials[i].divide(constant);
            }
            final int newCoefficient = this.constantCoefficient / constant;
            return new Polynomial(
                newMonomials,
                newCoefficient,
                this.monomialCount,
                this.isConstantPolynomial
            );
        }

        return Polynomial.INVALID;
    }

    @NotNull
    public Polynomial modulo(final int constant) {
        if (this.valid && this.isDivisible(constant)) {
            final Monomial[] newMonomials = new Monomial[this.monomialCount];
            Arrays.fill(newMonomials, new Monomial());

            return new Polynomial(newMonomials, this.constantCoefficient % constant, 0, true);
        }
        return Polynomial.INVALID;
    }

    @NotNull
    public Polynomial multiply(final int constant) {
        if (this.valid) {
            final Monomial[] newMonomials = new Monomial[this.monomialCount];
            Arrays.fill(newMonomials, new Monomial());

            final boolean isNewPolynomialConstant = (this.isConstantPolynomial) || (constant == 0);
            for (int i = 0; i < this.monomialCount; i++) {
                if (!isNewPolynomialConstant) {
                    newMonomials[i] = this.monomials[i].multiply(constant);
                }
            }
            final int newCoefficient = this.constantCoefficient * constant;

            return new Polynomial(
                newMonomials,
                newCoefficient,
                this.monomialCount,
                isNewPolynomialConstant
            );
        }
        return Polynomial.INVALID;
    }

    @NotNull
    public Polynomial invert() {
        // - (polyno)
        if (this.valid) {
            final Monomial[] newMonomials = new Monomial[this.monomialCount];
            Arrays.fill(newMonomials, new Monomial());
            for (int i = 0; i < this.monomialCount; i++) {
                newMonomials[i] = this.monomials[i].invert();
            }
            final int newConstantCoefficient = -this.constantCoefficient;
            return new Polynomial(
                newMonomials,
                newConstantCoefficient,
                this.monomialCount,
                this.isConstantPolynomial
            );
        }
        return Polynomial.INVALID;
    }

    @NotNull
    public Polynomial add(final Polynomial otherPolynomial) {
        if (this.valid && otherPolynomial.valid) {
            final Monomial[] newMonomials = new Monomial[this.monomialCount];
            Arrays.fill(newMonomials, new Monomial());

            final boolean[] monomialAdded = new boolean[otherPolynomial.monomialCount];
            int nextMonomialIndex = 0;
            for (int i = 0; i < this.monomialCount; i++) {
                //final Monomial monomial : this.monomials
                boolean added = false;
                int j = 0;
                while ((j < otherPolynomial.monomialCount) && !added) {
                    if (!monomialAdded[j]) {
                        final Monomial result =
                            this.monomials[i].tryAdd(otherPolynomial.monomials[j]);
                        if ((result != null)) {
                            if (!result.isNull()) {
                                newMonomials[nextMonomialIndex] = result;
                                nextMonomialIndex++;
                            }
                            monomialAdded[j] = true;
                            added = true;
                        }
                    }
                    j++;
                }
                if (!added) {
                    return Polynomial.INVALID;
                }
            }
            final int newCoefficient =
                this.constantCoefficient + otherPolynomial.constantCoefficient;

            final boolean isNewPolynomialConstant = newMonomials[0].isNull();

            return new Polynomial(
                newMonomials,
                newCoefficient,
                this.monomialCount,
                isNewPolynomialConstant
            );
        }
        return Polynomial.INVALID;
    }

    @NotNull
    public Polynomial subtract(final Polynomial otherPolynomial) {
        return this.invert().add(otherPolynomial).invert();
    }

    @NotNull
    @Override
    public String toString() {
        int size = 0;
        boolean found = false;
        while ((size < this.monomialCount) && !found) {
            if (this.monomials[size].isNull()) {
                found = true;
            } else {
                size++;
            }
        }

        final StringBuilder builder = new StringBuilder("Polynomial[");
        builder.append(size).append("/").append(this.monomialCount).append("]").append(" { ");
        if (this.valid) {
            boolean first = true;
            for (final Monomial monomial : this.monomials) {
                if (!monomial.isNull()) {
                    if ((monomial.getCoefficient() >= 0) && !first) {
                        builder.append(" + ");
                    } else {
                        if (monomial.getCoefficient() < 0) {
                            builder.append(" -");
                        }
                        if (!first) {
                            builder.append(" ");
                        }
                    }
                    builder
                        .append(Math.abs(monomial.getCoefficient()))
                        .append(monomial.getVariable());
                    first = false;
                }
            }

            if ((this.constantCoefficient >= 0) && !first) {
                builder.append(" + ");
            } else if (this.constantCoefficient < 0) {
                builder.append(" - ");
            }

            builder.append(Math.abs(this.constantCoefficient));
            builder.append(" }");
            if (this.isConstantPolynomial) {
                builder.append(" : const");
            }
        } else {
            builder.append("INVALID}");
        }

        return builder.toString();
    }
}
