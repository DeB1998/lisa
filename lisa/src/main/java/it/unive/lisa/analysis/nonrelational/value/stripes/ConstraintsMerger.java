package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.symbolic.value.Variable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * Utility class for merging constraints.
 *
 * @author Alessio De Biasi
 * @author Jonathan Gobbo
 * @version 1.2 2021-06-27
 * @since 1.5 2021-04-17
 */
final class ConstraintsMerger {

    /**
     * Private constructor that disallow the creation of this class that exposes only static
     * methods.
     */
    private ConstraintsMerger() {}

    /**
     * Merge the constraints specified in the map with the constraints specified in list.
     *
     * @param oldConstraints The constraint the new one are going to be merged to.
     * @param newConstraints The new constraints to merge with the old ones.
     * @return The map containing the merge of {@code oldConstraints} and {@code newConstraints}.
     */
    @SuppressWarnings("FeatureEnvy")
    @Unmodifiable
    @NotNull
    static Map<@NotNull Variable, @Unmodifiable @NotNull Set<@NotNull Constraint>> mergeConstraints(
        final @UnmodifiableView @NotNull Map<? extends @NotNull Variable,
                ? extends @Unmodifiable @NotNull Set<@NotNull Constraint>> oldConstraints,
        final @UnmodifiableView @NotNull Iterable<@NotNull FullConstraint> newConstraints
    ) {
        // Create the final map
        @NotNull
        final Map<@NotNull Variable,
                @Unmodifiable @NotNull Set<@NotNull Constraint>> newConstraintsMap = new HashMap<>(
            oldConstraints
        );

        // Loop over the new constraints
        for (final FullConstraint newConstraint : newConstraints) {
            // Extract the old constraints associated to the variable
            final Set<@NotNull Constraint> oldConstraintsSet = newConstraintsMap.get(
                newConstraint.getX()
            );
            final Set<@NotNull Constraint> newConstraintsSet;
            // Check if they have been found
            if (oldConstraintsSet == null) {
                // No constraints are associated to the variable
                newConstraintsSet = Collections.singleton(newConstraint.getConstraint());
            } else {
                // Merge the old constraints with the new one
                newConstraintsSet =
                    ConstraintsMerger.mergeConstraint(
                        oldConstraintsSet,
                        newConstraint.getConstraint()
                    );
            }
            // Insert the new constraints
            newConstraintsMap.put(newConstraint.getX(), newConstraintsSet);
        }
        // Make the map unmodifiable
        return Collections.unmodifiableMap(newConstraintsMap);
    }

    /**
     * Merges the new constraint to the set of constraints.
     *
     * @param oldConstraintSet The set of old constraints.
     * @param constraintToAdd The new constraint to merge.
     * @return The new set of constraints.
     */
    @SuppressWarnings("FeatureEnvy")
    @Unmodifiable
    @NotNull
    private static Set<Constraint> mergeConstraint(
        @NotNull final Set<Constraint> oldConstraintSet,
        @NotNull final Constraint constraintToAdd
    ) {
        // Clear the result
        final Set<Constraint> newConstraints = new HashSet<>(oldConstraintSet);
        final Iterator<Constraint> iterator = newConstraints.iterator();
        boolean toAdd = true;
        boolean continueSearch = true;
        // loop over the old constraints
        while (iterator.hasNext() && continueSearch) {
            final Constraint constraint = iterator.next();
            // Check if the constraints are similar
            if (constraint.differsOnlyOnK2(constraintToAdd)) {
                // Delete the wider constraint
                if (constraintToAdd.getK2() >= constraint.getK2()) {
                    iterator.remove();
                } else {
                    toAdd = false;
                }
                continueSearch = false;
            }
        }
        if (toAdd) {
            newConstraints.add(constraintToAdd);
        }
        // Create the new constraints set
        return Collections.unmodifiableSet(newConstraints);
    }
}
