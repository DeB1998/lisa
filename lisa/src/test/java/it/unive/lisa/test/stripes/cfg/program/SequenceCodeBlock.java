package it.unive.lisa.test.stripes.cfg.program;

import it.unive.lisa.test.stripes.cfg.graph.CfgNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
class SequenceCodeBlock<V extends Variable<V>> extends CodeBlock<V> {

    @Nullable
    private final String identifier;

    @NotNull
    private final List<V> variables;
    
    public SequenceCodeBlock(final V[] variables) {
        
        this(null, variables);
    }
    
    public SequenceCodeBlock(final @Nullable String identifier, final V[] variables) {
        this.identifier = identifier;
        this.variables = new LinkedList<>();
        for (V variable : variables) {
            this.variables.add(variable.copy());
        }
    }

    @Override
    <T> Node<T> convert(final VariableDataExtractor<V, T> extractor) {
        final T data = extractor.extractData(this.identifier, this.variables);
        return new SequenceNode<>(new CfgNode<>(this.identifier, data));
    }
}
