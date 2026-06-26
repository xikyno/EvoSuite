/*
 * 零依赖模板引擎，支持 ${var}、${foreach}、${if}、${end}
 */
package org.evosuite.template.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A simple, zero-dependency template engine for generating JUnit test code.
 *
 * <p>Supported syntax:
 * <ul>
 *   <li>{@code ${variable}} — variable substitution</li>
 *   <li>{@code ${obj.property}} — dotted property access</li>
 *   <li>{@code ${foreach item in list}}...{@code ${end}} — iteration</li>
 *   <li>{@code ${if condition}}...{@code ${else}}...{@code ${end}} — conditional</li>
 *   <li>{@code ${! include resourceName}} — include another template</li>
 *   <li>{@code ${# comment}} — comment (not rendered)</li>
 * </ul>
 *
 * <p>This engine is deliberately simple — no expression language, no method calls,
 * just basic property access and iteration. It is designed for offline use with
 * zero external dependencies.
 */
public class SimpleTemplateEngine implements TemplateEngine {

    private final Map<String, String> templateCache = new HashMap<>();

    // Special keywords
    private static final String FOREACH = "foreach";
    private static final String END = "end";
    private static final String IF = "if";
    private static final String ELSE = "else";
    private static final String INCLUDE = "! include";
    private static final String COMMENT = "#";

    @Override
    public String render(String template, TemplateContext context) throws TemplateRenderException {
        if (template == null) {
            return "";
        }
        return renderTemplate(template, context, 0);
    }

    /**
     * Load a template from the classpath resources and render it.
     */
    public String renderResource(String resourcePath, TemplateContext context)
            throws TemplateRenderException {
        String template = templateCache.get(resourcePath);
        if (template == null) {
            template = loadResource(resourcePath);
            templateCache.put(resourcePath, template);
        }
        return render(template, context);
    }

    /**
     * Load a template file from the classpath.
     */
    private String loadResource(String resourcePath) throws TemplateRenderException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            // Try with "templates/" prefix
            is = getClass().getClassLoader().getResourceAsStream("templates/" + resourcePath);
        }
        if (is == null) {
            throw new TemplateRenderException("Template resource not found: " + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new TemplateRenderException("Failed to load template: " + resourcePath, e);
        }
    }

    /**
     * Recursive template rendering with depth tracking to prevent infinite recursion.
     */
    private String renderTemplate(String template, TemplateContext context, int depth)
            throws TemplateRenderException {
        if (depth > 50) {
            throw new TemplateRenderException("Maximum template nesting depth exceeded");
        }

        StringBuilder result = new StringBuilder();
        int pos = 0;

        while (pos < template.length()) {
            int dollarIdx = template.indexOf("${", pos);

            if (dollarIdx < 0) {
                // No more expressions — append remaining text
                result.append(template.substring(pos));
                break;
            }

            // Append text before the expression
            result.append(template, pos, dollarIdx);

            // Find the closing brace
            int closeIdx = findMatchingClose(template, dollarIdx + 2);
            if (closeIdx < 0) {
                // Unclosed expression — treat as literal text
                result.append("${");
                pos = dollarIdx + 2;
                continue;
            }

            String expr = template.substring(dollarIdx + 2, closeIdx).trim();

            // Check if this is a block expression (foreach/if) that spans to ${end}
            if (expr.startsWith(FOREACH) || expr.startsWith(IF)) {
                // For block expressions, pass the entire template and let the handler
                // consume the full block (up to ${end})
                int[] newPos = new int[1];
                newPos[0] = closeIdx + 1;
                String rendered = evaluateBlockExpression(expr, context, template, depth, dollarIdx, newPos);
                result.append(rendered);
                pos = newPos[0];
            } else {
                pos = closeIdx + 1;
                // Parse and evaluate the expression
                String rendered = evaluateExpression(expr, context, template, depth, dollarIdx);
                result.append(rendered);
            }
        }

        return result.toString();
    }

    /**
     * Find the matching closing brace for an expression starting at openIdx.
     * Handles nested braces inside the expression.
     */
    private int findMatchingClose(String template, int openIdx) {
        int depth = 1;
        int pos = openIdx;
        while (pos < template.length() && depth > 0) {
            char c = template.charAt(pos);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return pos;
                }
            }
            pos++;
        }
        return -1; // No matching close found
    }

    /**
     * Evaluate a block expression (foreach/if) that spans from ${...} to ${end}.
     * The newPos array is updated to point past the ${end} tag.
     */
    private String evaluateBlockExpression(String expr, TemplateContext context,
                                           String template, int depth, int dollarIdx, int[] newPos)
            throws TemplateRenderException {
        if (expr.startsWith(FOREACH)) {
            return evaluateForeach(expr, context, template, depth, dollarIdx, newPos);
        } else if (expr.startsWith(IF)) {
            return evaluateIf(expr, context, template, depth, dollarIdx, newPos);
        }
        return "";
    }

    /**
     * Evaluate a single expression inside ${...}.
     */
    private String evaluateExpression(String expr, TemplateContext context,
                                      String template, int depth, int dollarIdx)
            throws TemplateRenderException {
        if (expr.isEmpty()) {
            return "";
        }

        // Comment: ${# ...}
        if (expr.startsWith(COMMENT)) {
            return "";
        }

        // Include: ${! include resourceName}
        if (expr.startsWith(INCLUDE)) {
            String resourceName = expr.substring(INCLUDE.length()).trim();
            String included = loadResource(resourceName);
            return renderTemplate(included, context, depth + 1);
        }

        // Foreach/If are handled by evaluateBlockExpression, not here
        // End/Else are handled by their parent blocks
        if (expr.equals(END) || expr.startsWith(ELSE)) {
            return "";
        }

        // Variable substitution: ${variable} or ${obj.property.sub}
        if (expr.contains(".")) {
            Object value = context.resolveProperty(expr);
            return value != null ? value.toString() : "";
        } else {
            Object value = context.get(expr);
            return value != null ? value.toString() : "";
        }
    }

    /**
     * Evaluate a foreach loop: ${foreach item in list}...${end}
     */
    private String evaluateForeach(String expr, TemplateContext context,
                                   String template, int depth, int dollarIdx, int[] newPos)
            throws TemplateRenderException {
        // Parse: "foreach item in list"
        String foreachExpr = expr.substring(FOREACH.length()).trim();
        String[] parts = foreachExpr.split("\\s+in\\s+", 2);
        if (parts.length != 2) {
            throw new TemplateRenderException(
                    "Invalid foreach syntax: '${" + expr + "}'. Expected: ${foreach itemVar in listVar}");
        }
        String itemVar = parts[0].trim();
        String listVar = parts[1].trim();

        // Find the body between ${foreach...} and ${end}
        EndBlockResult blockResult = findEndBlock(template, dollarIdx, expr);
        String body = blockResult.body;
        newPos[0] = blockResult.endPos;

        // Get the list from context
        Object listObj = context.resolveProperty(listVar);
        if (listObj == null) {
            listObj = context.get(listVar);
        }
        if (listObj == null) {
            return "";
        }

        Collection<?> items;
        if (listObj instanceof Collection) {
            items = (Collection<?>) listObj;
        } else if (listObj.getClass().isArray()) {
            List<Object> list = new ArrayList<>();
            for (Object item : (Object[]) listObj) {
                list.add(item);
            }
            items = list;
        } else {
            // Single item
            items = new ArrayList<>();
            ((List<Object>) items).add(listObj);
        }

        // Render body for each item
        StringBuilder result = new StringBuilder();
        int index = 0;
        for (Object item : items) {
            TemplateContext childCtx = context.createChild();
            childCtx.put(itemVar, item);
            childCtx.put(itemVar + "_index", index);
            childCtx.put(itemVar + "_isFirst", index == 0);
            childCtx.put(itemVar + "_isLast", index == items.size() - 1);
            result.append(renderTemplate(body, childCtx, depth + 1));
            index++;
        }

        return result.toString();
    }

    /**
     * Evaluate an if/else block: ${if condition}...${else}...${end}
     */
    private String evaluateIf(String expr, TemplateContext context,
                              String template, int depth, int dollarIdx, int[] newPos)
            throws TemplateRenderException {
        // Parse: "if condition"
        String ifExpr = expr.substring(IF.length()).trim();
        boolean condition = evaluateCondition(ifExpr, context);

        // Find the end of this if block
        EndBlockResult blockResult = findEndBlock(template, dollarIdx, expr);
        String body = blockResult.body;
        newPos[0] = blockResult.endPos;

        // Split body into if-branch and else-branch
        String ifBody;
        String elseBody = "";
        int elseIdx = findElseInBody(body);
        if (elseIdx >= 0) {
            ifBody = body.substring(0, elseIdx);
            elseBody = body.substring(elseIdx);
        } else {
            ifBody = body;
        }

        // Render the appropriate branch
        if (condition) {
            return renderTemplate(ifBody, context, depth + 1);
        } else {
            // Remove ${else} tag from else body
            int elseTagEnd = elseBody.indexOf("}");
            if (elseTagEnd >= 0 && elseBody.startsWith("${")) {
                elseBody = elseBody.substring(elseTagEnd + 1);
            }
            return renderTemplate(elseBody, context, depth + 1);
        }
    }

    /**
     * Evaluate a simple boolean condition.
     * Supports: "variable", "!variable" (not), "variable != null", "variable == value"
     */
    private boolean evaluateCondition(String condition, TemplateContext context) {
        if (condition == null || condition.isEmpty()) {
            return false;
        }

        condition = condition.trim();

        // Handle "!variable" (not)
        if (condition.startsWith("!")) {
            return !evaluateCondition(condition.substring(1), context);
        }

        // Handle "variable != null"
        if (condition.contains("!=")) {
            String[] parts = condition.split("!=", 2);
            Object left = resolveConditionValue(parts[0].trim(), context);
            String right = parts[1].trim();
            if ("null".equals(right)) {
                return left != null;
            }
            return left != null && !left.toString().equals(right);
        }

        // Handle "variable == value"
        if (condition.contains("==")) {
            String[] parts = condition.split("==", 2);
            Object left = resolveConditionValue(parts[0].trim(), context);
            String right = parts[1].trim();
            if ("null".equals(right)) {
                return left == null;
            }
            return left != null && left.toString().equals(right);
        }

        // Simple truthiness check
        Object value = resolveConditionValue(condition, context);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        return !value.toString().isEmpty();
    }

    /**
     * Resolve a value in a condition expression.
     */
    private Object resolveConditionValue(String expr, TemplateContext context) {
        if ("null".equals(expr) || "true".equals(expr) || "false".equals(expr)) {
            return null;
        }
        if (expr.contains(".")) {
            return context.resolveProperty(expr);
        }
        return context.get(expr);
    }

    /**
     * Find the ${end} that matches a foreach/if block.
     * Returns the body (between the opening tag and ${end}) and the position after ${end}.
     */
    private EndBlockResult findEndBlock(String template, int startDollarIdx, String expr)
            throws TemplateRenderException {
        int searchStart = startDollarIdx + expr.length() + 3; // +3 for ${...}
        int depth = 1;
        int pos = searchStart;

        while (pos < template.length() && depth > 0) {
            int nextDollar = template.indexOf("${", pos);
            if (nextDollar < 0) {
                throw new TemplateRenderException(
                        "Unclosed block starting at position " + startDollarIdx);
            }
            int closeIdx = findMatchingClose(template, nextDollar + 2);
            if (closeIdx < 0) {
                throw new TemplateRenderException("Unclosed expression at position " + nextDollar);
            }

            String innerExpr = template.substring(nextDollar + 2, closeIdx).trim();
            if (innerExpr.startsWith(FOREACH) || innerExpr.startsWith(IF)) {
                depth++;
            } else if (innerExpr.equals(END)) {
                depth--;
                if (depth == 0) {
                    String body = template.substring(searchStart, nextDollar);
                    return new EndBlockResult(body, closeIdx + 1);
                }
            }
            pos = closeIdx + 1;
        }

        throw new TemplateRenderException(
                "Unclosed block starting at position " + startDollarIdx);
    }

    /**
     * Find the position of ${else} in a body string.
     * Only matches else at the top level (depth 0).
     */
    private int findElseInBody(String body) {
        int depth = 0;
        int pos = 0;
        while (pos < body.length()) {
            int nextDollar = body.indexOf("${", pos);
            if (nextDollar < 0) {
                return -1;
            }
            int closeIdx = findMatchingClose(body, nextDollar + 2);
            if (closeIdx < 0) {
                return -1;
            }
            String innerExpr = body.substring(nextDollar + 2, closeIdx).trim();
            if (innerExpr.startsWith(FOREACH) || innerExpr.startsWith(IF)) {
                depth++;
            } else if (innerExpr.equals(END)) {
                depth--;
            } else if (innerExpr.equals(ELSE) && depth == 0) {
                return nextDollar;
            }
            pos = closeIdx + 1;
        }
        return -1;
    }

    /**
     * Result of finding an end block.
     */
    private static class EndBlockResult {
        final String body;
        final int endPos;

        EndBlockResult(String body, int endPos) {
            this.body = body;
            this.endPos = endPos;
        }
    }
}