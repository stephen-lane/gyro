package beam.core.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import beam.core.BeamUI;
import beam.lang.ast.scope.DiffableScope;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

public abstract class Change {

    private final List<Diff> diffs = new ArrayList<>();
    private boolean executable;
    private final AtomicBoolean changed = new AtomicBoolean();

    public List<Diff> getDiffs() {
        return diffs;
    }

    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public boolean isChanged() {
        return changed.get();
    }

    public abstract Diffable getDiffable();

    public abstract void writeTo(BeamUI ui);

    protected abstract void doExecute();

    public final void execute() throws Exception {
        if (!isExecutable()) {
            throw new IllegalStateException("Can't change yet!");
        }

        if (!changed.compareAndSet(false, true)) {
            return;
        }

        resolve(getDiffable());
        doExecute();
    }

    private void resolve(Object object) throws Exception {
        if (object instanceof Diffable) {
            Diffable diffable = (Diffable) object;
            DiffableScope scope = diffable.scope();

            if (scope != null) {
                diffable.initialize(scope.resolve());
            }

            for (DiffableField field : DiffableType.getInstance(diffable.getClass()).getFields()) {
                if (Diffable.class.isAssignableFrom(field.getItemClass())) {
                    resolve(field.getValue(diffable));
                }
            }

        } else if (object instanceof List) {
            for (Object item : (List<?>) object) {
                resolve(item);
            }
        }
    }

    public static String processAsScalarValue(String key, Object currentValue, Object pendingValue) {
        StringBuilder sb = new StringBuilder();

        sb.append(key);
        sb.append(": ");
        if (ObjectUtils.isBlank(currentValue)) {
            sb.append(pendingValue);
        } else {
            sb.append(currentValue);
            sb.append(" -> ");
            sb.append(pendingValue);
        }

        return sb.toString();
    }

    public static String processAsMapValue(String key, Map currentValue, Map pendingValue) {
        StringBuilder sb = new StringBuilder();

        String diff = mapSummaryDiff(currentValue, pendingValue);
        if (!ObjectUtils.isBlank(diff)) {
            sb.append(key);
            sb.append(": ");
            sb.append(diff);
        }

        return sb.toString();
    }

    public static String processAsListValue(String key, List currentValue, List pendingValue) {
        StringBuilder sb = new StringBuilder();

        List<String> additions = new ArrayList<>();
        if (pendingValue != null) {
            additions.addAll(pendingValue);

            if (currentValue != null) {
                additions.removeAll(currentValue);
            }
        }

        List<String> subtractions = new ArrayList<>();
        if (currentValue != null) {
            subtractions.addAll(currentValue);

            if (pendingValue != null) {
                subtractions.removeAll(pendingValue);
            }
        }

        if (!additions.isEmpty()) {
            sb.append(key);
            sb.append(": ");
            sb.append("+[");
            sb.append(StringUtils.join(additions, ", "));
            sb.append("]");
        } else if (!subtractions.isEmpty()) {
            sb.append(key);
            sb.append(": ");
            sb.append("-[");
            sb.append(StringUtils.join(subtractions, ", "));
            sb.append("]");
        }

        return sb.toString();
    }

    public static String mapSummaryDiff(Map current, Map pending) {
        if (current == null) {
            current = new HashMap();
        }

        final MapDifference diff = Maps.difference(current, pending);

        List<String> diffs = new ArrayList<>();

        for (Object key : diff.entriesOnlyOnRight().keySet()) {
            diffs.add(String.format("+[%s => %s]", key, pending.get(key)));
        }

        for (Object key : diff.entriesOnlyOnLeft().keySet()) {
            diffs.add(String.format("-[%s => %s]", key, current.get(key)));
        }

        for (Object key : diff.entriesDiffering().keySet()) {
            StringBuilder diffResult = new StringBuilder();
            diffResult.append(String.format("*[%s: ", key));
            MapDifference.ValueDifference value = (MapDifference.ValueDifference)diff.entriesDiffering().get(key);
            Object currentValue = value.leftValue();
            Object pendingValue = value.rightValue();

            if (currentValue instanceof Map && pendingValue instanceof Map) {
                diffResult.append(mapSummaryDiff((Map) currentValue, (Map) pendingValue));
            } else {
                diffResult.append(String.format("%s => %s", currentValue, pendingValue));
            }

            diffResult.append("]");

            diffs.add(diffResult.toString());
        }

        return StringUtils.join(diffs, ", ");
    }

}