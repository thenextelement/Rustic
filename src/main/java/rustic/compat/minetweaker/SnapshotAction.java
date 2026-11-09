package rustic.compat.minetweaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minetweaker.IUndoableAction;

/**
 * An undoable action that snapshots the recipe collection it changes, so {@code /minetweaker reload} can roll it back.
 * MineTweaker undoes actions in reverse order, so restoring snapshots is exact.
 */
public abstract class SnapshotAction implements IUndoableAction {

    private final String description;

    protected SnapshotAction(String description) {
        this.description = description;
    }

    protected abstract void doApply();

    @Override
    public boolean canUndo() {
        return true;
    }

    @Override
    public String describe() {
        return description;
    }

    @Override
    public String describeUndo() {
        return "Undoing: " + description;
    }

    @Override
    public Object getOverrideKey() {
        return null;
    }

    public static <T> SnapshotAction onList(final List<T> list, String description, final Runnable change) {
        return new SnapshotAction(description) {

            private List<T> snapshot;

            @Override
            public void apply() {
                snapshot = new ArrayList<>(list);
                doApply();
            }

            @Override
            protected void doApply() {
                change.run();
            }

            @Override
            public void undo() {
                if (snapshot == null) return;
                list.clear();
                list.addAll(snapshot);
            }
        };
    }

    public static <K, V> SnapshotAction onMap(final Map<K, V> map, String description, final Runnable change) {
        return new SnapshotAction(description) {

            private Map<K, V> snapshot;

            @Override
            public void apply() {
                snapshot = new HashMap<>(map);
                doApply();
            }

            @Override
            protected void doApply() {
                change.run();
            }

            @Override
            public void undo() {
                if (snapshot == null) return;
                map.clear();
                map.putAll(snapshot);
            }
        };
    }

}
