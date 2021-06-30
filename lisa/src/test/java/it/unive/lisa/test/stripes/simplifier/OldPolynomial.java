package it.unive.lisa.test.stripes.simplifier;

import it.unive.lisa.symbolic.value.Variable;
import org.jetbrains.annotations.NotNull;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-05-16
 * @since version date
 */
public class OldPolynomial {

    public static final OldPolynomial INVALID = new OldPolynomial(false);

    @NotNull
    private OldMonomial[] monomials;

    private int constant;

    private final int monomialCount;

    private boolean isConstant;

    private boolean valid;

    private int size;

    public OldPolynomial(final int monomialCount, final int constant) {
        this.monomials = new OldMonomial[monomialCount];
        for (int i = 0; i < monomialCount; i++) {
            this.monomials[i] = new OldMonomial();
        }
        this.constant = constant;
        this.monomialCount = monomialCount;
        this.isConstant = true;
        this.valid = true;
        this.size = 0;
    }
    
    public OldPolynomial(final OldPolynomial polynomial) {
    
        this.monomials = new OldMonomial[polynomial.monomialCount];
        for (int i = 0; i < polynomial.monomialCount; i++) {
            this.monomials[i] = new OldMonomial(polynomial.monomials[i]);
        }
        this.constant = polynomial.constant;
        this.monomialCount = polynomial.monomialCount;
        this.isConstant = polynomial.isConstant;
        this.valid = polynomial.valid;
        this.size = polynomial.size;
    }

    public OldPolynomial(final int monomialCount, final Variable variable, final int coefficient) {
        this(monomialCount, 0);
        final OldMonomial firstMonomial = new OldMonomial(coefficient, variable);
        this.size = (firstMonomial.isNull()) ? 0 : 1;
        this.monomials[0] = firstMonomial;
        this.isConstant = false;
    }

    private OldPolynomial(final boolean isValid) {
        this(0, 0);
        this.valid = isValid;
    }

    public boolean isValid() {
        return this.valid;
    }

    public int getSize() {
        if (this.valid) {
            return this.size;
        }
        throw new IllegalStateException("Null polynomial!");
    }

    public boolean isConstant() {
        return this.isConstant && this.valid;
    }

    public int getConstant() {
        if (this.valid) {
            return this.constant;
        }
        throw new IllegalStateException("Null polynomial!");
    }

    public OldMonomial getMonomial(final int index) {
        if (this.valid && (index >= 0) && (index < this.size)) {
            return this.monomials[index];
        }
        throw new IllegalArgumentException("Invalid monomial index " + index);
    }

    private boolean isDivisible(final int constant) {
        boolean isDivisible = constant != 0;
        int i = 0;
        while ((i < this.size) && isDivisible) {
            final OldMonomial monomial = this.monomials[i];
            isDivisible = ((monomial.getCoefficient() % constant) == 0);
            i++;
        }
        return true;
    }

    public void divide(final int constant) {
        if (this.valid && this.isDivisible(constant)) {
            for (final OldMonomial monomial : this.monomials) {
                monomial.divide(constant);
            }
            this.constant /= constant;
        } else {
            this.valid = false;
        }
    }

    public void modulo(final int constant) {
        if (this.valid && this.isDivisible(constant)) {
            for (final OldMonomial monomial : this.monomials) {
                monomial.clear();
            }
            this.isConstant = true;
            this.size = 0;
            this.constant %= constant;
        } else {
            this.valid = false;
        }
    }

    public void multiply(final int constant) {
        if (this.valid) {
            for (final OldMonomial monomial : this.monomials) {
                monomial.multiply(constant);
            }
            this.constant *= constant;
            // (3x + 3y + 7z + 7) * d = c
            // 3 * d = c
            this.isConstant = this.isConstant || (constant == 0);
            this.size = (constant == 0) ? 0 : this.size;
        }
    }

    public void invert() {
        // - (polyno)
        if (this.valid) {
            for (final OldMonomial monomial : this.monomials) {
                monomial.invert();
            }
            this.constant = -this.constant;
        }
    }

    public void add(final OldPolynomial otherPolynomial) {
        if (this.valid && otherPolynomial.valid) {
            final boolean[] monomialAdded = new boolean[otherPolynomial.monomialCount];
            int newSize = 0;
            for (final OldMonomial monomial : this.monomials) {
                boolean added = false;
                int i = 0;
                while (i < otherPolynomial.monomialCount && !added) {
                    if (!monomialAdded[i]) {
                        added = monomial.tryAdd(otherPolynomial.monomials[i]);
                        if (added) {
                            monomialAdded[i] = true;
                        }
                    }
                    i++;
                }
                if (!added) {
                    this.valid = false;
                } else {
                    newSize += (monomial.isNull()) ? 0 : 1;
                }
            }
            this.constant += otherPolynomial.constant;
            this.isConstant = newSize == 0;

            final OldMonomial[] newMonomials = new OldMonomial[this.monomialCount];
            int j = 0;
            for (int i = 0; i < newSize; i++) {
                if (!this.monomials[i].isNull()) {
                    newMonomials[j] = this.monomials[i];
                    j++;
                }
            }
            for (; j < this.monomialCount; j++) {
                newMonomials[j] = new OldMonomial();
            }
            this.size = newSize;
            this.monomials = newMonomials;
        } else {
            this.valid = false;
        }
    }

    public void subtract(final OldPolynomial otherPolynomial) {
        this.invert();
        this.add(otherPolynomial);
        this.invert();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("Polynomial[");
        builder.append(this.size).append("/").append(this.monomialCount).append("]").append(" { ");
        if (this.valid) {
            boolean first = true;
            for (final OldMonomial monomial : this.monomials) {
                if (!monomial.isNull()) {
                    if (monomial.getCoefficient() >= 0 && !first) {
                        builder.append(" + ");
                    } else {
                        if (monomial.getCoefficient() < 0) {
                            builder.append(" -");
                        }
                        if (!first) {
                            builder.append(" ");
                        }
                    }
                    builder.append(Math.abs(monomial.getCoefficient())).append(monomial.getVariable());
                    first = false;
                }
            }
            
            if ((this.constant >= 0) && (this.size >= 0) && !first) {
                builder.append(" + ");
            } else if (this.constant < 0) {
                builder.append(" - ");
            }
            
            builder.append(Math.abs(this.constant));
            builder.append(" }");
            if (this.isConstant) {
                builder.append(" : constant");
            }
        } else {
            builder.append("INVALID}");
        }
        
        return builder.toString();
    }
}
