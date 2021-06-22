package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.symbolic.value.Variable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-17
 * @since version date
 */
final class Utils {

    private Utils() {}

    // TODO: Modify this!
    static void mergeConstraints(
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> oldConstraints,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newConstraints
    ) {
        for (final Entry<@NotNull Variable, @NotNull Set<@NotNull Constraint>> entry : newConstraints.entrySet()) {
            final Set<@NotNull Constraint> oldConstraintsSet = oldConstraints.get(entry.getKey());
            if (oldConstraintsSet == null) {
                oldConstraints.put(entry.getKey(), entry.getValue());
            } else {
                final Set<@NotNull Constraint> newConstraintsSet = entry.getValue();
                Utils.removeIfPresent(oldConstraintsSet, newConstraintsSet);
                Utils.removeIfPresent(newConstraintsSet, oldConstraintsSet);
                oldConstraintsSet.addAll(entry.getValue());
            }
        }
    }

    private static void removeIfPresent(
        final Set<Constraint> sourceConstraints,
        final Set<Constraint> destinationConstraints
    ) {
        sourceConstraints.removeIf(
            sourceConstraint -> {
                for (final Constraint destinationConstraint : destinationConstraints) {
                    if (
                        sourceConstraint.isSameConstraint(destinationConstraint) &&
                        (sourceConstraint.getK2() < destinationConstraint.getK2())
                    ) {
                        return true;
                    }
                }
                return false;
            }
        );
    }

    static void addConstraint(
        final @NotNull Map<? super @NotNull Variable, @NotNull Set<@NotNull Constraint>> elements,
        final Variable key,
        final Constraint constraintToAdd
    ) {
        @Nullable
        final Set<@NotNull Constraint> constraints = elements.get(key);
        if (constraints == null) {
            Set<Constraint> newSet = new HashSet<>();
            newSet.add(constraintToAdd);
            elements.put(key, newSet);
        } else {
            constraints.add(constraintToAdd);
        }
    }
}
