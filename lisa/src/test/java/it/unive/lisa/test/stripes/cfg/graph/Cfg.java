package it.unive.lisa.test.stripes.cfg.graph;

import it.unive.lisa.test.stripes.cfg.util.Pair;
import java.util.LinkedList;
import java.util.Queue;
import org.jetbrains.annotations.NotNull;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-20
 * @since version date
 */
public class Cfg<T> {

    @NotNull
    private final CfgNode<T> root;

    public Cfg(@NotNull final CfgNode<T> root) {
        this.root = root;
    }

    public boolean isEqualTo(
        final Cfg<T> other,
        final CfgNodeEqualityComputer<T> equalityComputer,
        final CfgErrorHandler<T> onSizeDifference,
        final CfgErrorHandler<T> onDataDifference
    ) {
        final Queue<Pair<CfgNode<T>, CfgNode<T>>> nodesToCheck = new LinkedList<>();

        nodesToCheck.add(new Pair<>(this.root, other.root));
        while (!nodesToCheck.isEmpty()) {
            final Pair<CfgNode<T>, CfgNode<T>> nextNode = nodesToCheck.remove();
            final CfgNode<T> firstNode = nextNode.getFirst();
            final CfgNode<T> secondNode = nextNode.getSecond();
            // Check nodes size
            if (!firstNode.isEqualInSizeTo(secondNode)) {
                onSizeDifference.handleError(
                    firstNode.getIdentifier(),
                    secondNode.getIdentifier(),
                    firstNode.getData(),
                    secondNode.getData()
                );
                return false;
            }
            // Check nodes equality
            if (
                !equalityComputer.areEqual(
                    firstNode.getIdentifier(),
                    secondNode.getIdentifier(),
                    firstNode.getData(),
                    secondNode.getData()
                )
            ) {
                onDataDifference.handleError(
                    firstNode.getIdentifier(),
                    secondNode.getIdentifier(),
                    firstNode.getData(),
                    secondNode.getData()
                );
            }
            // Add next nodes
            if ((firstNode.getTrue() != null) && (secondNode.getTrue() != null)) {
                if ((!firstNode.isVisited()) && (!secondNode.isVisited())) {
                    nodesToCheck.add(new Pair<>(firstNode.getTrue(), secondNode.getTrue()));
                }
            } else if (!((firstNode.getTrue() == null) && (secondNode.getTrue() == null))) {
                onSizeDifference.handleError(
                        firstNode.getIdentifier(),
                        secondNode.getIdentifier(),
                        firstNode.getData(),
                        secondNode.getData()
                );
            }

            if ((firstNode.getFalse() != null) && (secondNode.getFalse() != null)) {
                if ((!firstNode.isVisited()) && (!secondNode.isVisited())) {
                    nodesToCheck.add(new Pair<>(firstNode.getFalse(), secondNode.getFalse()));
                }
            } else if (!((firstNode.getFalse() == null) && (secondNode.getFalse() == null))) {
                onSizeDifference.handleError(
                        firstNode.getIdentifier(),
                        secondNode.getIdentifier(),
                        firstNode.getData(),
                        secondNode.getData()
                );
            }
            firstNode.setVisited(true);
            secondNode.setVisited(true);
        }
        return true;
    }
}
