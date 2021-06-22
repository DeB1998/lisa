package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public interface ForWithInitializationBlock<V extends Variable<V>> {
    
    ForWithConditionBlock<V> condition(V... variables);
    ForWithConditionBlock<V> condition(String identifier, V... variables);
}
