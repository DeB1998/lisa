package it.unive.lisa.test.stripes.cfg.dot;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-20
 * @since version date
 */
@FunctionalInterface
public interface Extractor<T> {
    
    T extract(String label, String color, int peripheries);
}
