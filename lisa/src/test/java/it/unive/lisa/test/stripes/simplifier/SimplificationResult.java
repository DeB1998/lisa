package it.unive.lisa.test.stripes.simplifier;

import it.unive.lisa.symbolic.value.Variable;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-05-02
 * @since version date
 */
public class SimplificationResult {

    @Nullable
    private Variable firstIdentifier;

    private int firstIdentifierCount;

    @Nullable
    private Variable secondIdentifier;

    private int secondIdentifierCount;
    private int constant;

    public SimplificationResult(
        @Nullable final Variable firstIdentifier,
        final int firstIdentifierCount,
        @Nullable final Variable secondIdentifier,
        final int secondIdentifierCount,
        final int constant
    ) {
        this.firstIdentifier = firstIdentifier;
        this.firstIdentifierCount = firstIdentifierCount;
        this.secondIdentifier = secondIdentifier;
        this.secondIdentifierCount = secondIdentifierCount;
        this.constant = constant;
    }

    public SimplificationResult(final int constant) {
        this(null, 0, null, 0, constant);
    }

    public @Nullable Variable getFirstIdentifier() {
        return this.firstIdentifier;
    }

    public void setFirstIdentifier(final @Nullable Variable firstIdentifier) {
        this.firstIdentifier = firstIdentifier;
    }

    public int getFirstIdentifierCount() {
        return this.firstIdentifierCount;
    }

    public void setFirstIdentifierCount(final int firstIdentifierCount) {
        this.firstIdentifierCount = firstIdentifierCount;
    }

    public @Nullable Variable getSecondIdentifier() {
        return this.secondIdentifier;
    }

    public void setSecondIdentifier(final @Nullable Variable secondIdentifier) {
        this.secondIdentifier = secondIdentifier;
    }

    public int getSecondIdentifierCount() {
        return this.secondIdentifierCount;
    }

    public void setSecondIdentifierCount(final int secondIdentifierCount) {
        this.secondIdentifierCount = secondIdentifierCount;
    }

    public int getConstant() {
        return this.constant;
    }

    public void setConstant(final int constant) {
        this.constant = constant;
    }
}
