/*
 * 接口定义
 */
package org.evosuite.template.engine;

/**
 * Interface for template engines that render test code from templates.
 */
public interface TemplateEngine {

    /**
     * Render a template string with the given context.
     *
     * @param template the template string to render
     * @param context  the variables accessible in the template
     * @return the rendered output string
     * @throws TemplateRenderException if rendering fails
     */
    String render(String template, TemplateContext context) throws TemplateRenderException;
}