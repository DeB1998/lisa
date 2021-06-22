package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public interface IfBlockWithElse<V extends Variable<V>> {
    
    IfBlockWithElse<V> next(V... variables);
    IfBlockWithElse<V> next(String identifier, V... variables);
    IfBlockWithElse<V> next(CodeBlock<V> codeBlock);
}
