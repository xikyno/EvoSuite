/*
 * Mock 配置（mockClass、stubs、injectMode）
 */
package org.evosuite.template.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a single mock object in a test case.
 * Supports Mockito-based mocking with constructor/setter/field injection.
 */
public class MockConfig {

    /** Fully qualified class name to mock */
    private String mockClass;

    /** Variable name for the mock instance */
    private String mockVariable;

    /** Injection method: "constructor", "setter", "field", "none" */
    private String injectMode = "constructor";

    /** For setter injection: the setter method name (e.g., "setDatabaseService") */
    private String setterMethod;

    /** For field injection: the field name to inject into */
    private String fieldName;

    /** Method stubs for this mock */
    private List<StubConfig> stubs = new ArrayList<>();

    public MockConfig() {
    }

    public String getMockClass() {
        return mockClass;
    }

    public void setMockClass(String mockClass) {
        this.mockClass = mockClass;
    }

    public String getMockVariable() {
        return mockVariable;
    }

    public void setMockVariable(String mockVariable) {
        this.mockVariable = mockVariable;
    }

    /**
     * How to inject the mock into the SUT:
     * "constructor" - pass as constructor parameter
     * "setter" - call setter method
     * "field" - set via reflection
     * "none" - no injection (mock is used standalone)
     */
    public String getInjectMode() {
        return injectMode;
    }

    public void setInjectMode(String injectMode) {
        this.injectMode = injectMode;
    }

    public String getSetterMethod() {
        return setterMethod;
    }

    public void setSetterMethod(String setterMethod) {
        this.setterMethod = setterMethod;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public List<StubConfig> getStubs() {
        return stubs;
    }

    public void setStubs(List<StubConfig> stubs) {
        this.stubs = stubs;
    }
}