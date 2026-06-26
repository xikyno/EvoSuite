/*
 * 工具函数：值 → Java 字面量转换（字符串加引号，数字不加）
 */
package org.evosuite.template.engine;

/**
 * Built-in functions available in templates for common code generation tasks.
 */
public final class BuiltinFunctions {

    private BuiltinFunctions() {
        // Utility class
    }

    /**
     * Get the simple class name from a fully qualified name.
     */
    public static String simpleName(String fullName) {
        if (fullName == null) {
            return "";
        }
        int lastDot = fullName.lastIndexOf('.');
        return lastDot >= 0 ? fullName.substring(lastDot + 1) : fullName;
    }

    /**
     * Get the package name from a fully qualified class name.
     */
    public static String packageName(String fullName) {
        if (fullName == null) {
            return "";
        }
        int lastDot = fullName.lastIndexOf('.');
        return lastDot >= 0 ? fullName.substring(0, lastDot) : "";
    }

    /**
     * Convert a value to a Java literal string representation.
     */
    public static String toJavaLiteral(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            String s = (String) value;
            // Check for $ref: patterns
            if (s.startsWith("$ref:")) {
                return s.substring(5);
            }
            if (s.startsWith("$expr:")) {
                return s.substring(6);
            }
            // Try to parse as number to avoid quoting numeric literals
            try {
                if (s.contains(".")) {
                    Double.parseDouble(s);
                    return s;
                } else {
                    Long.parseLong(s);
                    return s;
                }
            } catch (NumberFormatException e) {
                // Not a number, treat as string
            }
            // Check for boolean literals
            if ("true".equals(s) || "false".equals(s)) {
                return s;
            }
            // Escape special characters and wrap in quotes
            s = s.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
            return "\"" + s + "\"";
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Number) {
            // Handle long/float suffixes
            if (value instanceof Long) {
                return value + "L";
            }
            if (value instanceof Float) {
                return value + "f";
            }
            return value.toString();
        }
        if (value instanceof Character) {
            char c = (Character) value;
            return "'" + (c == '\'' ? "\\'" : c == '\\' ? "\\\\" : String.valueOf(c)) + "'";
        }
        return value.toString();
    }

    /**
     * Convert a camelCase or PascalCase name to a human-readable label.
     */
    public static String toLabel(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append(' ');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }
}
