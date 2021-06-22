package it.unive.lisa.test.stripes.cfg.program;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public class ForCodeBlock<V extends Variable<V>>
    extends BaseCodeBlock<ForCodeBlock<V>, V>
    implements ForBlock<V>, ForWithConditionBlock<V>, ForWithInitializationBlock<V> {

    @NotNull
    private WhileCodeBlock<V> whileBlock;

    @NotNull
    private SequenceCodeBlock<V> initializationBlock;

    @SuppressWarnings({"SuspiciousArrayCast", "ZeroLengthArrayAllocation"})
    public ForCodeBlock() {
        this.whileBlock = new WhileCodeBlock<>("", (V[]) new Variable[0]);
        this.initializationBlock = new SequenceCodeBlock<>("", (V[]) new Variable[0]);
    }

    @Override
    public ForWithInitializationBlock<V> initialization(V... variables) {
        this.initializationBlock = new SequenceCodeBlock<>(variables);

        return this;
    }

    @Override
    public ForWithInitializationBlock<V> initialization(String identifier, V... variables) {
        this.initializationBlock = new SequenceCodeBlock<>(identifier, variables);

        return this;
    }

    @Override
    public ForWithConditionBlock<V> condition(V... variables) {
        this.whileBlock = new WhileCodeBlock<>(null, variables);

        return this;
    }

    @Override
    public ForWithConditionBlock<V> condition(String identifier, V... variables) {
        this.whileBlock = new WhileCodeBlock<>(identifier, variables);

        return this;
    }

    @Override
    public ForCodeBlock<V> next(CodeBlock<V> codeBlock) {
        this.whileBlock.next(codeBlock);
        return this;
    }

    @Override
    public CodeBlock<V> increment(V... variables) {
        this.whileBlock.next(variables);
        return this;
    }

    @Override
    public CodeBlock<V> increment(String identifier, V... variables) {
        this.whileBlock.next(identifier, variables);
        return this;
    }

    @Override
    <T> Node<T> convert(VariableDataExtractor<V, T> extractor) {
        Node<T> whileNode = whileBlock.convert(extractor);
        Node<T> initializationNode = this.initializationBlock.convert(extractor);
        initializationNode.linkTo(whileNode);
        return new ForNode<>(initializationNode, whileNode);
    }
}
