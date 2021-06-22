package it.unive.lisa.test.stripes.cfg.program;

import it.unive.lisa.test.stripes.cfg.graph.CfgNode;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
class SequenceNode<T> implements Node<T> {

    private CfgNode<T> cfgNode;

    public SequenceNode(final CfgNode<T> cfgNode) {
        this.cfgNode = cfgNode;
    }

    @Override
    public void linkTo(Node<T> otherNode) {
        this.cfgNode.setTrue(otherNode.getCfgNode());
    }

    @Override
    public CfgNode<T> getCfgNode() {
        return this.cfgNode;
    }
}
