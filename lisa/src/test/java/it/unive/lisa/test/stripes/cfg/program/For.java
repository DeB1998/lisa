package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public class For<V extends Variable<V>> implements ForBlock<V> {
    
    private ForCodeBlock<V> codeBlock;
    
    public For() {
        this.codeBlock = new ForCodeBlock<>();
    }
    
    @Override
    public ForWithInitializationBlock<V> initialization(V... variables) {
        
        return codeBlock.initialization(variables);
    }
    
    @Override
    public ForWithInitializationBlock<V> initialization(String identifier, V... variables) {
    
        return codeBlock.initialization(variables);
    }
}
