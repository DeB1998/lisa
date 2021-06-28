package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.symbolic.value.Variable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

    static @Unmodifiable @NotNull Map<@NotNull Variable, @Unmodifiable @NotNull Set<@NotNull Constraint>> mergeConstraints(
        @Unmodifiable final @NotNull Map<@NotNull Variable, @Unmodifiable @NotNull Set<@NotNull Constraint>> oldConstraints,
        final @NotNull List<@NotNull FullConstraint> newConstraints
    ) {
        @NotNull
        final Map<@NotNull Variable, @Unmodifiable @NotNull Set<@NotNull Constraint>> newConstraintsMap = new HashMap<>(
            oldConstraints
        );

        for (final FullConstraint newConstraint : newConstraints) {
            final Set<@NotNull Constraint> oldConstraintsSet = newConstraintsMap.get(
                newConstraint.getX()
            );
            final Set<@NotNull Constraint> newConstraintsSet;
            if (oldConstraintsSet == null) {
                newConstraintsSet = Collections.singleton(newConstraint.getConstraint());
            } else {
                newConstraintsSet =
                    Utils.addConstraint(oldConstraintsSet, newConstraint.getConstraint());
            }
            newConstraintsMap.put(newConstraint.getX(), newConstraintsSet);
        }

        return Collections.unmodifiableMap(newConstraintsMap);
    }

    @Unmodifiable
    private static Set<Constraint> addConstraint(
        @NotNull final Set<Constraint> oldConstraintSet,
        final Constraint constraintToAdd
    ) {
        final Set<Constraint> newConstraints = new HashSet<>(oldConstraintSet);
        final Iterator<Constraint> iterator = newConstraints.iterator();
        boolean toAdd = true;
        boolean stop = false;
        while (iterator.hasNext() && !stop) {
            final Constraint constraint = iterator.next();
            if (constraint.differsOnlyOnK2(constraintToAdd)) {
                if (constraintToAdd.getK2() >= constraint.getK2()) {
                    iterator.remove();
                } else {
                    toAdd = false;
                }
                stop = true;
            }
        }
        if (toAdd) {
            newConstraints.add(constraintToAdd);
        }
        return Collections.unmodifiableSet(newConstraints);
    }
    /*
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
    }*/
}
