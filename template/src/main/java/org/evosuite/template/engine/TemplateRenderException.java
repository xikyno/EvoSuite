/*
 * 渲染异常
 */
package org.evosuite.template.engine;

/**
 * Exception thrown when template rendering fails.
 */
public class TemplateRenderException extends Exception {

    private static final long serialVersionUID = 1L;

    public TemplateRenderException(String message) {
        super(message);
    }

    public TemplateRenderException(String message, Throwable cause) {
        super(message, cause);
    }
}