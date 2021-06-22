package it.unive.lisa.test.stripes.cfg.program;

import it.unive.lisa.test.stripes.cfg.graph.CfgNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
class IfNode<T> implements Node<T> {

    @NotNull
    private final CfgNode<T> conditionNode;

    @Nullable
    private final Node<T> lastThenNode;

    @Nullable
    private final Node<T> lastElseNode;

    public IfNode(
        @NotNull final CfgNode<T> conditionNode,
        @Nullable final Node<T> lastThenNode,
        @Nullable final Node<T> lastElseNode
    ) {
        this.conditionNode = conditionNode;
        this.lastThenNode = lastThenNode;
        this.lastElseNode = lastElseNode;
    }

    @Override
    public void linkTo(final Node<T> otherNode) {
        if (this.lastThenNode == null) {
            this.conditionNode.setTrue(otherNode.getCfgNode());
        } else {
            this.lastThenNode.linkTo(otherNode);
        }
        if (this.lastElseNode == null) {
            this.conditionNode.setFalse(otherNode.getCfgNode());
        } else {
            this.lastElseNode.linkTo(otherNode);
        }
    }

    @SuppressWarnings("SuspiciousGetterSetter")
    @Override
    public CfgNode<T> getCfgNode() {
        return this.conditionNode;
    }
}
