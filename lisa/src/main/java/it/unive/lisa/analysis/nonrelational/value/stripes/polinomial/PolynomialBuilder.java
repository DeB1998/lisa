package it.unive.lisa.analysis.nonrelational.value.stripes.polinomial;

import it.unive.lisa.symbolic.value.Variable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-26
 * @since version date
 */
public final class PolynomialBuilder {

    private final List<Monomial> monomials;
    private final int monomialsCount;
    private int constantCoefficient;

    public PolynomialBuilder(final int monomialsCount) {
        this.monomialsCount = monomialsCount;
        this.monomials = new LinkedList<>();
        this.constantCoefficient = 0;
    }

    public PolynomialBuilder addMonomial(final int coefficient, @Nullable final Variable variable) {
        if ((variable != null) && (coefficient != 0)) {
            this.monomials.add(new Monomial(coefficient, variable));
        }
        return this;
    }

    public PolynomialBuilder setConstantCoefficient(final int constantCoefficient) {
        this.constantCoefficient = constantCoefficient;

        return this;
    }

    public Polynomial build() {
        final Monomial[] newMonomials = new Monomial[this.monomialsCount];
        final int monomialsSize = this.monomials.size();
        int nextMonomialIndex = 0;
        for (int i = 0; i < monomialsSize; i++) {
            if (nextMonomialIndex == this.monomialsCount) {
                throw new IllegalStateException("More monomials that monomialsCount");
            }
            final Monomial monomial = this.monomials.get(i);
            int monomialWithSameVariable = -1;
            for (int j = i; j < monomialsSize; j++) {
                final Monomial otherMonomial = this.monomials.get(j);
                if (!otherMonomial.isNull() && otherMonomial.getVariable().equals(monomial)) {
                    monomialWithSameVariable = j;
                }
            }
            if (monomialWithSameVariable == -1) {
                newMonomials[i] = new Monomial(monomial.getCoefficient(), monomial.getVariable());
                nextMonomialIndex++;
            } else {
                newMonomials[monomialWithSameVariable] =
                    new Monomial(
                        monomial.getCoefficient() + newMonomials[monomialWithSameVariable].getCoefficient(),
                            newMonomials[monomialWithSameVariable].getVariable()
                    );
            }
        }
    
        Arrays.fill(newMonomials,nextMonomialIndex,this.monomialsCount,new Monomial());

        return new Polynomial(
            newMonomials,
            this.constantCoefficient,
            this.monomialsCount,
            this.monomials.isEmpty()
        );
    }
}
