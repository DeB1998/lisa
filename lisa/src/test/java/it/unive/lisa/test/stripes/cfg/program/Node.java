package it.unive.lisa.test.stripes.cfg.program;

import it.unive.lisa.test.stripes.cfg.graph.CfgNode;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
interface Node<T> {
    void linkTo(Node<T> otherNode);
    CfgNode<T> getCfgNode();
}
