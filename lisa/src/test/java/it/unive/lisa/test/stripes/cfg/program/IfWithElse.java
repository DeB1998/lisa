package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public class IfWithElse<V extends Variable<V>> extends CodeBlock<V> implements IfBlockWithElse<V> {
    
    private IfCodeBlock<V> codeBlock;
    
    IfWithElse(final IfCodeBlock<V> codeBlock) {
        
        this.codeBlock = codeBlock;
    }
    
    @Override
    public IfWithElse<V> next(V... variables) {
        
        this.codeBlock.next(variables);
        return this;
    }
    
    @Override
    public IfWithElse<V> next(String identifier, V... variables) {
    
        this.codeBlock.next(identifier,variables);
        return this;
    }
    
    @Override
    public IfWithElse<V> next(CodeBlock<V> codeBlock) {
    
        this.codeBlock.next(codeBlock);
        return this;
    }
    @Override
    <T> Node<T> convert(VariableDataExtractor<V, T> extractor) {
        
        return this.codeBlock.convert(extractor);
    }
    
}
