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
    
    private int coefficient;
    @Nullable
    private Variable variable;
    
    Monomial(final int coefficient, @NotNull final Variable variable) {
    
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
    
    boolean tryAdd(@NotNull final Monomial otherMonomial) {
        
        if ((this.variable == null) || (otherMonomial.variable == null) || this.variable.equals(otherMonomial.variable)) {
            if (this.variable == null) {
                this.variable = otherMonomial.variable;
            }
            this.coefficient += otherMonomial.coefficient;
            if (this.coefficient == 0) {
                this.clear();
            }
            return true;
        }
        return false;
    }
    
    void invert() {
        this.coefficient = -this.coefficient;
    }
    
    void multiply(int constant) {
        this.coefficient *= constant;
        if (this.coefficient == 0) {
            this.clear();
        }
    }
    
    void divide(int constant) {
        this.coefficient /= constant;
    }
    
    void clear() {
        
        this.variable = null;
        this.coefficient = 0;
    }
    
    public boolean isNull() {
        return (this.coefficient == 0) && (this.variable == null);
    }
    
    @Override
    public String toString() {
        
        StringBuilder builder = new StringBuilder("Monomial { ");
        
        if (this.variable == null) {
            builder.append("NULL ");
        } else {
            builder.append(this.coefficient).append(this.variable).append(" ");
        }
        
        return builder.append('}').toString();
    }
}
