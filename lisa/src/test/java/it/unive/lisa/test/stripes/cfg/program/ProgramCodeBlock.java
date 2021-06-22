package it.unive.lisa.test.stripes.cfg.program;

import it.unive.lisa.test.stripes.cfg.graph.Cfg;
import it.unive.lisa.test.stripes.cfg.util.Pair;
import java.util.LinkedList;
import java.util.List;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
class ProgramCodeBlock<V extends Variable<V>> implements EndedProgram<V> {

    private List<CodeBlock<V>> codeBlocks;

    public ProgramCodeBlock() {
        this.codeBlocks = new LinkedList<>();
    }

    public ProgramCodeBlock<V> next(final V... variables) {
        return this.next(new SequenceCodeBlock<>(variables));
    }

    public ProgramCodeBlock<V> next(final String identifier, final V... variables) {
        return this.next(new SequenceCodeBlock<>(identifier, variables));
    }

    public ProgramCodeBlock<V> next(final CodeBlock<V> codeBlock) {
        this.codeBlocks.add(codeBlock);

        return this;
    }

    @Override
    public <T> Cfg<T> getCfg(final VariableDataExtractor<V, T> extractor) {
        final Pair<Node<T>, Node<T>> nodes = CodeBlock.linkSequence(this.codeBlocks, extractor);

        return new Cfg<>(nodes.getFirst().getCfgNode());
    }
}
