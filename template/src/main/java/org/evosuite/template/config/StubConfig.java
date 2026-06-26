/*
 * Stub 配置（when().thenReturn()）
 */
package org.evosuite.template.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a single method stub on a mock object.
 */
public class StubConfig {

    /** Method name to stub */
    private String method;

    /** Mockito matchers (e.g., "anyLong()", "anyString()", "any(User.class)") */
    private List<String> matchers = new ArrayList<>();

    /** Return values for consecutive calls (supports $ref:variableName) */
    private List<Object> returns = new ArrayList<>();

    /** Whether to throw an exception instead of returning */
    private String throwException;

    public StubConfig() {
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Mockito argument matchers.
     * Examples: "anyLong()", "anyString()", "any(User.class)", "eq(42)"
     * Empty list means the method takes no parameters.
     */
    public List<String> getMatchers() {
        return matchers;
    }

    public void setMatchers(List<String> matchers) {
        this.matchers = matchers;
    }

    /**
     * Return values for consecutive calls.
     * Supports $ref:variableName to reference other objects.
     */
    public List<Object> getReturns() {
        return returns;
    }

    public void setReturns(List<Object> returns) {
        this.returns = returns;
    }

    /**
     * Exception class to throw instead of returning a value.
     * Only one of "returns" or "throwException" should be set.
     */
    public String getThrowException() {
        return throwException;
    }

    public void setThrowException(String throwException) {
        this.throwException = throwException;
    }
}