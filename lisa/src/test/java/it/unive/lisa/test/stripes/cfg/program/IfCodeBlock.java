package it.unive.lisa.test.stripes.cfg.program;

import it.unive.lisa.test.stripes.cfg.graph.CfgNode;
import it.unive.lisa.test.stripes.cfg.util.Pair;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
class IfCodeBlock<V extends Variable<V>>
    extends BaseCodeBlock<IfCodeBlock<V>, V>
    implements IfBlock<V>, IfBlockWithThen<V>, IfBlockWithElse<V> {

    private final String conditionIdentifier;
    private final List<V> conditionVariables;
    private final List<CodeBlock<V>> thenBranch;
    private final List<CodeBlock<V>> elseBranch;

    private boolean addToElse;

    public IfCodeBlock(final String conditionIdentifier, final V[] conditionVariables) {
        this.conditionIdentifier = conditionIdentifier;
        this.conditionVariables = new LinkedList<>();
        for (V variable : conditionVariables) {
            this.conditionVariables.add(variable.copy());
        }
        this.thenBranch = new LinkedList<>();
        this.elseBranch = new LinkedList<>();
        this.addToElse = false;
    }

    @Override
    public IfBlockWithThen<V> thenBlock() {
        return this;
    }

    @Override
    public IfBlockWithElse<V> elseBlock() {
        this.addToElse = true;
        return this;
    }

    @Override
    public IfCodeBlock<V> next(final CodeBlock<V> codeBlock) {
        if (this.addToElse) {
            this.elseBranch.add(codeBlock);
        } else {
            this.thenBranch.add(codeBlock);
        }

        return this;
    }

    @Override
    <T> Node<T> convert(final VariableDataExtractor<V, T> extractor) {
    
        final T data = extractor.extractData(this.conditionIdentifier, this.conditionVariables);
        final CfgNode<T> conditionNode = new CfgNode<>(this.conditionIdentifier, data);
        
        Node<T> lastThenNode = null;
        if (!this.thenBranch.isEmpty()) {
            final Pair<Node<T>, Node<T>> thenNodes = CodeBlock.linkSequence(this.thenBranch, extractor);
            conditionNode.setTrue(thenNodes.getFirst().getCfgNode());
            lastThenNode = thenNodes.getSecond();
        }
        Node<T> lastElseNode = null;
        if (!this.elseBranch.isEmpty()) {
            final Pair<Node<T>, Node<T>> elseNodes = CodeBlock.linkSequence(this.elseBranch, extractor);
            conditionNode.setFalse(elseNodes.getFirst().getCfgNode());
            lastElseNode = elseNodes.getSecond();
        }
        return new IfNode<>(conditionNode, lastThenNode, lastElseNode);
    }
}
