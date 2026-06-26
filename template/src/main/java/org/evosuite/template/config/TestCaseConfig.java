/*
 * 单个测试用例（testName、methodCalls、assertions、mocks）
 */
package org.evosuite.template.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for a single test case.
 */
public class TestCaseConfig {

    private String methodName;
    private String testName;
    private String description;
    private String expectedException;
    private List<MockConfig> mocks = new ArrayList<>();
    private List<TestMethodConfig> methodCalls = new ArrayList<>();
    private List<TestCaseConfig> variants = new ArrayList<>();
    private Map<String, Object> assertions = new HashMap<>();

    public TestCaseConfig() {
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpectedException() {
        return expectedException;
    }

    public void setExpectedException(String expectedException) {
        this.expectedException = expectedException;
    }

    /**
     * Mock object definitions for this test case.
     * Each mock will be created with Mockito and injected into the SUT.
     */
    public List<MockConfig> getMocks() {
        return mocks;
    }

    public void setMocks(List<MockConfig> mocks) {
        this.mocks = mocks;
    }

    public List<TestMethodConfig> getMethodCalls() {
        return methodCalls;
    }

    public void setMethodCalls(List<TestMethodConfig> methodCalls) {
        this.methodCalls = methodCalls;
    }

    public List<TestCaseConfig> getVariants() {
        return variants;
    }

    public void setVariants(List<TestCaseConfig> variants) {
        this.variants = variants;
    }

    public Map<String, Object> getAssertions() {
        return assertions;
    }

    public void setAssertions(Map<String, Object> assertions) {
        this.assertions = assertions;
    }

    public boolean hasVariants() {
        return variants != null && !variants.isEmpty();
    }
}
