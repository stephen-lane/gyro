package beam.lang;

import com.google.common.base.CaseFormat;
import com.google.common.base.Throwables;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContainerNode extends Node {

    transient Map<String, ValueNode> keyValues = new HashMap<>();

    /**
     * Returns a map of key/value pairs for this block.
     *
     * This map is the internal state with the properties (from subclasses)
     * overlaid.
     *
     * The values are after resolution.
     */
    public Map<String, Object> resolvedKeyValues() {
        Map<String, Object> values = new HashMap<>();

        for (String key : keys()) {
            values.put(key, get(key).getValue());
        }

        try {
            for (PropertyDescriptor p : Introspector.getBeanInfo(getClass()).getPropertyDescriptors()) {
                Method reader = p.getReadMethod();

                try {
                    Field propertyField = getClass().getDeclaredField(p.getName());
                    if (Modifier.isTransient(propertyField.getModifiers())) {
                        continue;
                    }
                } catch (NoSuchFieldException ex) {
                    continue;
                }

                if (reader != null) {
                    String key = keyFromFieldName(p.getDisplayName());
                    Object value = reader.invoke(this);

                    values.put(key, value);
                }
            }
        } catch (IllegalAccessException | IntrospectionException error) {
            throw new IllegalStateException(error);
        } catch (InvocationTargetException error) {
            throw Throwables.propagate(error);
        }

        return values;
    }

    public Set<String> keys() {
        return keyValues.keySet();
    }

    public ValueNode get(String key) {
        return keyValues.get(key);
    }

    public void put(String key, ValueNode valueNode) {
        valueNode.setParentNode(this);

        keyValues.put(key, valueNode);
    }

    @Override
    public boolean resolve() {
        for (ValueNode value : keyValues.values()) {
            boolean resolved = value.resolve();
            if (!resolved) {
                throw new BeamLanguageException("Unable to resolve configuration.", value);
            }
        }

        return true;
    }

    protected String valueToString(Object value) {
        StringBuilder sb = new StringBuilder();

        if (value instanceof String) {
            sb.append("'" + value + "'");
        } else if (value instanceof Map) {
            sb.append(mapToString((Map) value));
        } else if (value instanceof List) {
            sb.append(listToString((List) value));
        }

        return sb.toString();
    }

    protected String mapToString(Map map) {
        StringBuilder sb = new StringBuilder();

        sb.append("{").append("\n");

        for (Object key : map.keySet()) {
            Object value = map.get(key);

            sb.append("        ");
            sb.append(key).append(": ");
            sb.append(valueToString(value));
            sb.append(",\n");
        }
        sb.setLength(sb.length() - 2);

        sb.append("\n    }");

        return sb.toString();
    }

    protected String listToString(List list) {
        StringBuilder sb = new StringBuilder();

        sb.append("[").append("\n");

        for (Object value : list) {
            sb.append("        ");
            sb.append(valueToString(value));
            sb.append(",\n");
        }
        sb.setLength(sb.length() - 2);

        sb.append("\n    ]");

        return sb.toString();
    }

    protected String fieldNameFromKey(String key) {
        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, key);
    }

    protected String keyFromFieldName(String field) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, field).replaceFirst("get-", "");
    }

}