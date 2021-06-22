package it.unive.lisa.test.stripes.cfg.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-20
 * @since version date
 */
@FunctionalInterface
public interface CfgNodeEqualityComputer<T> {
    boolean areEqual(
            @Nullable String firstIdentifier,
            @Nullable String secondIdentifier,
            @NotNull T firstData,
            @NotNull T secondData
    );
}

