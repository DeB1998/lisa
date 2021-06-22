package it.unive.lisa.test.stripes.cfg.program;

import it.unive.lisa.test.stripes.cfg.graph.CfgNode;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
class WhileNode<T> implements Node<T> {
    
    private CfgNode<T> whileNode;
    
    public WhileNode(final CfgNode<T> whileNode) {
        
        this.whileNode = whileNode;
    }
    
    @Override
    public void linkTo(Node<T> otherNode) {
        
        this.whileNode.setFalse(otherNode.getCfgNode());
    }
    
    @SuppressWarnings("SuspiciousGetterSetter")
    @Override
    public CfgNode<T> getCfgNode() {
        
        return this.whileNode;
    }
}
