package it.unive.lisa.test.stripes.cfg.program;

import it.unive.lisa.test.stripes.cfg.graph.CfgNode;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
class ForNode<T> implements Node<T> {

    @Nullable
    private Node<T> initialization;

    private Node<T> whileNode;

    public ForNode(@Nullable final Node<T> initialization, final Node<T> whileNode) {
        this.initialization = initialization;
        this.whileNode = whileNode;
    }

    @Override
    public void linkTo(Node<T> otherNode) {
        this.whileNode.linkTo(otherNode);
    }

    @Override
    public CfgNode<T> getCfgNode() {
        if (this.initialization != null) {
            return initialization.getCfgNode();
        }

        return whileNode.getCfgNode();
    }
}
