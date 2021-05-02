package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.symbolic.value.Identifier;
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
    private Identifier firstIdentifier;

    private int firstIdentifierCount;

    @Nullable
    private Identifier secondIdentifier;

    private int secondIdentifierCount;
    private int constant;

    public SimplificationResult(
        @Nullable final Identifier firstIdentifier,
        final int firstIdentifierCount,
        @Nullable final Identifier secondIdentifier,
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

    public @Nullable Identifier getFirstIdentifier() {
        return this.firstIdentifier;
    }

    public void setFirstIdentifier(final @Nullable Identifier firstIdentifier) {
        this.firstIdentifier = firstIdentifier;
    }

    public int getFirstIdentifierCount() {
        return this.firstIdentifierCount;
    }

    public void setFirstIdentifierCount(final int firstIdentifierCount) {
        this.firstIdentifierCount = firstIdentifierCount;
    }

    public @Nullable Identifier getSecondIdentifier() {
        return this.secondIdentifier;
    }

    public void setSecondIdentifier(final @Nullable Identifier secondIdentifier) {
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
