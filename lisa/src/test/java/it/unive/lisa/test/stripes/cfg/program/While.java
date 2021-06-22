package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public class While<V extends Variable<V>> extends CodeBlock<V> implements WhileBlock<V> {

    private WhileCodeBlock<V> codeBlock;

    public While(final V... conditionVariables) {
        this(null, conditionVariables);
    }

    public While(final String conditionIdentifier, final V... conditionVariables) {
        this.codeBlock = new WhileCodeBlock<>(conditionIdentifier, conditionVariables);
    }

    @Override
    <T> Node<T> convert(VariableDataExtractor<V, T> extractor) {
        return this.codeBlock.convert(extractor);
    }

    @Override
    public While<V> next(V... variables) {
        this.codeBlock.next(variables);
        return this;
    }

    @Override
    public While<V> next(String identifier, V... variables) {
        this.codeBlock.next(identifier, variables);
        return this;
    }

    @Override
    public While<V> next(CodeBlock<V> codeBlock) {
        this.codeBlock.next(codeBlock);
        return this;
    }
}
