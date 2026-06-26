/*
 * 模板变量容器，支持 obj.property 点号路径反射访问
 */
package org.evosuite.template.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Context object passed to templates during rendering.
 * Holds all variables accessible via ${variable} syntax.
 */
public class TemplateContext {

    private final Map<String, Object> variables;

    public TemplateContext() {
        this.variables = new HashMap<>();
    }

    public TemplateContext(Map<String, Object> variables) {
        this.variables = new HashMap<>(variables);
    }

    /**
     * Put a variable into the context.
     */
    public void put(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * Get a variable from the context, or null if not found.
     */
    public Object get(String key) {
        return variables.get(key);
    }

    /**
     * Check if a variable exists in the context.
     */
    public boolean has(String key) {
        return variables.containsKey(key);
    }

    /**
     * Get a variable as a String, or null if not found.
     */
    public String getString(String key) {
        Object value = variables.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Get all variables.
     */
    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
    }

    /**
     * Create a child context that inherits all variables from this context.
     */
    public TemplateContext createChild() {
        return new TemplateContext(this.variables);
    }

    /**
     * Resolve a dotted property path like "user.name" or "items[0].name".
     */
    public Object resolveProperty(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        String[] parts = path.split("\\.");
        Object current = variables;

        for (String part : parts) {
            if (current == null) {
                return null;
            }

            // Handle array/list access: "list[0]"
            int bracketIdx = part.indexOf('[');
            if (bracketIdx > 0) {
                String propName = part.substring(0, bracketIdx);
                String indexStr = part.substring(bracketIdx + 1, part.indexOf(']'));
                current = getProperty(current, propName);
                if (current instanceof java.util.List) {
                    int index = Integer.parseInt(indexStr);
                    java.util.List<?> list = (java.util.List<?>) current;
                    current = (index < list.size()) ? list.get(index) : null;
                } else if (current.getClass().isArray()) {
                    int index = Integer.parseInt(indexStr);
                    Object[] arr = (Object[]) current;
                    current = (index < arr.length) ? arr[index] : null;
                } else {
                    return null;
                }
            } else {
                current = getProperty(current, part);
            }
        }
        return current;
    }

    /**
     * Get a property from an object by name, trying getter methods and field access.
     */
    private Object getProperty(Object obj, String propertyName) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(propertyName);
        }

        // Try "get" + capitalized property name
        String getterName = "get" + Character.toUpperCase(propertyName.charAt(0))
                + propertyName.substring(1);
        try {
            java.lang.reflect.Method getter = obj.getClass().getMethod(getterName);
            return getter.invoke(obj);
        } catch (Exception e) {
            // ignore
        }

        // Try "is" + capitalized property name (for boolean)
        String isGetterName = "is" + Character.toUpperCase(propertyName.charAt(0))
                + propertyName.substring(1);
        try {
            java.lang.reflect.Method isGetter = obj.getClass().getMethod(isGetterName);
            return isGetter.invoke(obj);
        } catch (Exception e) {
            // ignore
        }

        // Try direct field access
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            // ignore
        }

        return null;
    }
}