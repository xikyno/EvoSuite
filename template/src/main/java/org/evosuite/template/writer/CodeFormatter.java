/*
 * 后处理：去掉多余空行
 */
package org.evosuite.template.writer;

/**
 * Basic code formatter for generated JUnit test code.
 * Handles indentation and basic cleanup.
 */
public class CodeFormatter {

    private static final String INDENT = "    "; // 4 spaces

    /**
     * Format Java source code with basic indentation.
     */
    public String format(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }

        StringBuilder result = new StringBuilder();
        int indentLevel = 0;
        boolean newLine = true;

        for (String line : source.split("\n")) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                result.append("\n");
                newLine = true;
                continue;
            }

            // Decrease indent before writing closing braces
            if (trimmed.startsWith("}") || trimmed.startsWith(")")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }

            // Write indentation
            if (newLine) {
                for (int i = 0; i < indentLevel; i++) {
                    result.append(INDENT);
                }
            }

            result.append(trimmed).append("\n");

            // Increase indent after opening braces
            if (trimmed.endsWith("{") || trimmed.endsWith("(")) {
                indentLevel++;
            }
            // Handle lines that are just a closing brace
            if (trimmed.equals("}") && indentLevel == 0) {
                // Already at root level
            }

            newLine = true;
        }

        return result.toString();
    }

    /**
     * Remove excessive blank lines (more than 2 consecutive).
     */
    public String removeExcessiveBlankLines(String source) {
        if (source == null) {
            return null;
        }
        return source.replaceAll("\n{3,}", "\n\n");
    }

    /**
     * Cleanup generated source without changing user-visible test logic.
     */
    public String cleanup(String source) {
        if (source == null) {
            return null;
        }

        String normalized = source.replace("\r\n", "\n")
                .replaceAll(";{2,}", ";")
                .replaceAll("[ \t]+\n", "\n");

        normalized = compactImports(normalized);
        normalized = compactClassBody(normalized);
        normalized = normalized.replaceAll("\n{3,}", "\n\n");
        return normalized.trim() + "\n";
    }

    private String compactImports(String source) {
        String[] lines = source.split("\n", -1);
        StringBuilder result = new StringBuilder();
        boolean inImportBlock = false;
        boolean previousImport = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("import ")) {
                inImportBlock = true;
                previousImport = true;
                result.append(trimmed).append("\n");
                continue;
            }

            if (inImportBlock && trimmed.isEmpty()) {
                continue;
            }

            if (inImportBlock && !trimmed.startsWith("import ")) {
                if (previousImport) {
                    result.append("\n");
                }
                inImportBlock = false;
                previousImport = false;
            }

            result.append(line).append("\n");
        }

        return result.toString();
    }

    private String compactClassBody(String source) {
        String[] lines = source.split("\n", -1);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            String previous = previousNonEmpty(lines, i);
            String next = nextNonEmpty(lines, i);

            if (trimmed.isEmpty()) {
                if (previous.startsWith("@") && !next.startsWith("@")) {
                    continue;
                }
                if (previous.endsWith("{") && !next.startsWith("@")) {
                    continue;
                }
                if (next.equals("}")) {
                    continue;
                }
            }

            result.append(line).append("\n");
        }

        return result.toString();
    }

    private String previousNonEmpty(String[] lines, int index) {
        for (int i = index - 1; i >= 0; i--) {
            String trimmed = lines[i].trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return "";
    }

    private String nextNonEmpty(String[] lines, int index) {
        for (int i = index + 1; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return "";
    }
}
