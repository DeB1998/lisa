package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
abstract class BaseCodeBlock<R, V extends Variable<V>> extends CodeBlock<V> {

    public R next(final V... variables) {
        return this.next(new SequenceCodeBlock<>(variables));
    }

    public R next(final String identifier, final V... variables) {
        return this.next(new SequenceCodeBlock<>(identifier, variables));
    }

    public abstract R next(CodeBlock<V> codeBlock);
}
