package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public class If<V extends Variable<V>> extends CodeBlock<V> implements IfBlock<V> {

    private IfCodeBlock<V> codeBlock;

    public If(final V... conditionVariables) {
        this(null, conditionVariables);
    }

    public If(final String conditionIdentifier, final V... conditionVariables) {
        this.codeBlock = new IfCodeBlock<>(conditionIdentifier, conditionVariables);
    }

    @Override
    <T> Node<T> convert(VariableDataExtractor<V, T> extractor) {
        return this.codeBlock.convert(extractor);
    }

    @Override
    public IfWithThen<V> thenBlock() {
        this.codeBlock.thenBlock();
        return new IfWithThen<>(this.codeBlock);
    }

    @Override
    public IfWithElse<V> elseBlock() {
        this.codeBlock.elseBlock();
        return new IfWithElse<>(this.codeBlock);
    }
}
