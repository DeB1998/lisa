package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public interface ForWithConditionBlock<V extends Variable<V>> {
    CodeBlock<V> increment(V... variables);
    CodeBlock<V> increment(String identifier, V... variables);

    ForWithConditionBlock<V> next(V... variables);
    ForWithConditionBlock<V> next(String identifier, V... variables);
    ForWithConditionBlock<V> next(CodeBlock<V> codeBlock);
}
