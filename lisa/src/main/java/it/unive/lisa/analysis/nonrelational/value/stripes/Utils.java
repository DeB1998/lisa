package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.symbolic.value.Variable;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-17
 * @since version date
 */
final class Utils {

    private Utils() {}

    static void mergeConstraints(
        final @NotNull Map<? super @NotNull Variable, @NotNull Set<@NotNull Constraint>> oldConstraints,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newConstraints
    ) {
        for (final Entry<@NotNull Variable, @NotNull Set<@NotNull Constraint>> entry : newConstraints.entrySet()) {
            final Set<@NotNull Constraint> constraints = oldConstraints.get(entry.getKey());
            if (constraints == null) {
                oldConstraints.put(entry.getKey(), entry.getValue());
            } else {
                constraints.addAll(entry.getValue());
            }
        }
    }

    static void addConstraint(
        final @NotNull Map<? super @NotNull Variable, @NotNull Set<@NotNull Constraint>> elements,
        final Variable key,
        final Constraint constraintToAdd
    ) {
        @Nullable
        final Set<@NotNull Constraint> constraints = elements.get(key);
        if (constraints == null) {
            elements.put(key, Collections.singleton(constraintToAdd));
        } else {
            constraints.add(constraintToAdd);
        }
    }
}
