package it.unive.lisa.test.stripes.cfg.program;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public interface VariableDataExtractor<V extends Variable<V>, T> {

    T extractData(@Nullable String identifier, @NotNull List<V> variables);
}
