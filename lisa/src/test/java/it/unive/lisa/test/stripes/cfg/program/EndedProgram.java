package it.unive.lisa.test.stripes.cfg.program;

import it.unive.lisa.test.stripes.cfg.graph.Cfg;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public interface EndedProgram<V extends Variable<V>> {
    
    <T> Cfg<T> getCfg(VariableDataExtractor<V, T> extractor);
}
