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
public class CfgNode<T> {

    private static final int TRUE_INDEX = 0;

    private static final int FALSE_INDEX = 1;

    @Nullable
    private final String identifier;

    @NotNull
    private final T data;

    @Nullable
    private final CfgNode<T>[] nexts;

    boolean visited;

    public CfgNode(@Nullable final String identifier, @NotNull final T data) {
        this.identifier = identifier;
        this.data = data;
        this.nexts = new CfgNode[] { null, null };
        this.visited = false;
    }

    public @Nullable String getIdentifier() {
        return this.identifier;
    }

    public @NotNull T getData() {
        return this.data;
    }

    public boolean isVisited() {
        return this.visited;
    }

    public void setVisited(final boolean visited) {
        this.visited = visited;
    }

    @Nullable
    public CfgNode<T> getTrue() {
        return this.nexts[CfgNode.TRUE_INDEX];
    }

    public void setTrue(@NotNull final CfgNode<T> trueNode) {
        if (this.nexts[CfgNode.TRUE_INDEX] != null) {
            throw new InvalidCfgException("True branch is already set");
        }
        this.nexts[CfgNode.TRUE_INDEX] = trueNode;
    }

    @Nullable
    public CfgNode<T> getFalse() {
        return this.nexts[CfgNode.FALSE_INDEX];
    }

    public void setFalse(@NotNull final CfgNode<T> falseNode) {
        if (this.nexts[CfgNode.FALSE_INDEX] != null) {
            throw new InvalidCfgException("False branch is already set");
        }
        this.nexts[CfgNode.FALSE_INDEX] = falseNode;
    }

    public boolean isEqualInSizeTo(final CfgNode<T> otherNode) {
        if (this.nexts[CfgNode.TRUE_INDEX] == null) {
            return otherNode.nexts[CfgNode.TRUE_INDEX] == null;
        }
        if (this.nexts[CfgNode.FALSE_INDEX] == null) {
            return otherNode.nexts[CfgNode.FALSE_INDEX] == null;
        }
        return (
            (otherNode.nexts[CfgNode.TRUE_INDEX] != null) &&
            (otherNode.nexts[CfgNode.FALSE_INDEX] != null)
        );
    }
}
