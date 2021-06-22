package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public interface WhileBlock<V extends Variable<V>> {
    WhileBlock<V> next(V... variables);
    WhileBlock<V> next(String identifier, V... variables);
    WhileBlock<V> next(CodeBlock<V> codeBlock);
}
