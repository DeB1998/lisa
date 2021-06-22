package it.unive.lisa.test.stripes.cfg.util;

import org.jetbrains.annotations.NotNull;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-20
 * @since version date
 */
public class Pair<T, U> {

    @NotNull
    private T first;

    @NotNull
    private U second;

    public Pair(@NotNull final T first, @NotNull final U second) {
        this.first = first;
        this.second = second;
    }

    @NotNull
    public T getFirst() {
        return this.first;
    }

    @NotNull
    public U getSecond() {
        return this.second;
    }
}
