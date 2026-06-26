/*
 * 一次方法调用（methodName、parameters、targetVariable）
 */
package org.evosuite.template.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a single method invocation within a test case.
 */
public class TestMethodConfig {

    private String methodName;
    private String targetVariable;
    private String resultVariable;
    private List<Object> parameters = new ArrayList<>();
    private String constructorFor;

    public TestMethodConfig() {
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * The variable name to call the method on.
     * null for static methods or when the method is called on 'this' (for constructors).
     */
    public String getTargetVariable() {
        return targetVariable;
    }

    public void setTargetVariable(String targetVariable) {
        this.targetVariable = targetVariable;
    }

    /**
     * The variable name to store the result in.
     * null for void methods or when the result is not needed.
     */
    public String getResultVariable() {
        return resultVariable;
    }

    public void setResultVariable(String resultVariable) {
        this.resultVariable = resultVariable;
    }

    /**
     * Parameter values. Can be primitives, strings, numbers, or special references
     * like "$ref:variableName".
     */
    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * If this is a constructor call, the fully qualified class name.
     * null for regular method calls.
     */
    public String getConstructorFor() {
        return constructorFor;
    }

    public void setConstructorFor(String constructorFor) {
        this.constructorFor = constructorFor;
    }

    /**
     * Returns true if this is a constructor call.
     */
    public boolean isConstructorCall() {
        return constructorFor != null && !constructorFor.isEmpty();
    }
}