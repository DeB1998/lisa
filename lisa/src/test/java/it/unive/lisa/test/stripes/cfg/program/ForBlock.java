package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public interface ForBlock<V extends Variable<V>> extends ForWithInitializationBlock<V> {
    ForWithInitializationBlock<V> initialization(V... variables);
    ForWithInitializationBlock<V> initialization(String identifier, V... variables);
}
