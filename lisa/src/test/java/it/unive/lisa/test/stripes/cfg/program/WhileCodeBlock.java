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
public class WhileCodeBlock<V extends Variable<V>>
    extends BaseCodeBlock<WhileBlock<V>, V>
    implements WhileBlock<V> {

    private final String conditionIdentifier;
    private final List<V> conditionVariables;
    private final List<CodeBlock<V>> codeBlocks;

    public WhileCodeBlock(final String conditionIdentifier, final V[] conditionVariables) {
        this.conditionIdentifier = conditionIdentifier;
        this.conditionVariables = new LinkedList<>();
        for (V variable : conditionVariables) {
            this.conditionVariables.add(variable.copy());
        }
        this.codeBlocks = new LinkedList<>();
    }

    @Override
    public WhileBlock<V> next(final CodeBlock<V> codeBlock) {
        this.codeBlocks.add(codeBlock);
        return this;
    }

    @Override
    <T> Node<T> convert(final VariableDataExtractor<V, T> extractor) {
        final T data = extractor.extractData(this.conditionIdentifier, this.conditionVariables);
        final CfgNode<T> conditionNode = new CfgNode<>(this.conditionIdentifier, data);
        final Node<T> whileNode = new WhileNode<>(conditionNode);

        if (this.codeBlocks.isEmpty()) {
            conditionNode.setTrue(conditionNode);
        } else {
            final Pair<Node<T>, Node<T>> whileNodes = CodeBlock.linkSequence(
                this.codeBlocks,
                extractor
            );
            conditionNode.setTrue(whileNodes.getFirst().getCfgNode());
            whileNodes.getSecond().linkTo(whileNode);
        }
        return whileNode;
    }
}
