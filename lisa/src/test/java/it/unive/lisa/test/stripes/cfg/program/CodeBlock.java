package it.unive.lisa.test.stripes.cfg.program;

import it.unive.lisa.test.stripes.cfg.graph.InvalidCfgException;
import it.unive.lisa.test.stripes.cfg.util.Pair;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public abstract class CodeBlock<V extends Variable<V>> {

    abstract <T> Node<T> convert(VariableDataExtractor<V, T> extractor);

    static <T, V extends Variable<V>> Pair<Node<T>, Node<T>> linkSequence(
        @NotNull final List<CodeBlock<V>> codeBlocks,
        @NotNull final VariableDataExtractor<V, T> extractor
    ) {
        Node<T> first = null;
        Node<T> last = null;
        for (final CodeBlock<V> codeBlock : codeBlocks) {
            final Node<T> converted = codeBlock.convert(extractor);
            if (last != null) {
                last.linkTo(converted);
            } else {
                first = converted;
            }
            last = converted;
        }
        if ((first == null) || (last == null)) {
            throw new InvalidCfgException("Invalid code block");
        }
        return new Pair<>(first, last);
    }
}
