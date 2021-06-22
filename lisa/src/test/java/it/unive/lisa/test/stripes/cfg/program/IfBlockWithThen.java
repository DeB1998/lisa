package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public interface IfBlockWithThen<V extends Variable<V>> {
    
    IfBlockWithThen<V> next(V... variables);
    IfBlockWithThen<V> next(String identifier, V... variables);
    IfBlockWithThen<V> next(CodeBlock<V> codeBlock);
    IfBlockWithElse<V> elseBlock();
}
