package it.unive.lisa.test.stripes.simplifier;

import it.unive.lisa.symbolic.value.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-17
 * @since version date
 */
final class OldUtils {

    private OldUtils() {}

    // TODO: Modify this!
    static void mergeConstraints(
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> oldConstraints,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> newConstraints
    ) {
        for (final Entry<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> entry : newConstraints.entrySet()) {
            final Set<@NotNull OldConstraint> oldConstraintsSet = oldConstraints.get(entry.getKey());
            if (oldConstraintsSet == null) {
                oldConstraints.put(entry.getKey(), entry.getValue());
            } else {
                final Set<@NotNull OldConstraint> newConstraintsSet = entry.getValue();
                OldUtils.removeIfPresent(oldConstraintsSet, newConstraintsSet);
                OldUtils.removeIfPresent(newConstraintsSet, oldConstraintsSet);
                oldConstraintsSet.addAll(entry.getValue());
            }
        }
    }

    private static void removeIfPresent(
        final Set<OldConstraint> sourceConstraints,
        final Set<OldConstraint> destinationConstraints
    ) {
        sourceConstraints.removeIf(
            sourceConstraint -> {
                for (final OldConstraint destinationConstraint : destinationConstraints) {
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
        final @NotNull Map<? super @NotNull Variable, @NotNull Set<@NotNull OldConstraint>> elements,
        final Variable key,
        final OldConstraint constraintToAdd
    ) {
        @Nullable
        final Set<@NotNull OldConstraint> constraints = elements.get(key);
        if (constraints == null) {
            Set<OldConstraint> newSet = new HashSet<>();
            newSet.add(constraintToAdd);
            elements.put(key, newSet);
        } else {
            constraints.add(constraintToAdd);
        }
    }
}
