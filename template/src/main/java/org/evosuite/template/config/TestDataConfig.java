/*
 * JSON 根对象（targetClass、testCases 列表）
 */
package org.evosuite.template.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Root configuration object for template-based test generation.
 * Maps to the top-level JSON/XML configuration file.
 */
public class TestDataConfig {

    private String targetClass;
    private String testPackage;
    private String outputFormat = "JUNIT4";
    private String outputDirectory = "./generated-tests";
    private String testFramework = "AUTO";
    private String testClassPrefix = "";
    private String testClassSuffix = "Test";
    private String baseTestClass;
    private List<TestCaseConfig> testCases = new ArrayList<>();
    private Map<String, Object> globals = new HashMap<>();

    public TestDataConfig() {
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public String getTestPackage() {
        return testPackage;
    }

    public void setTestPackage(String testPackage) {
        this.testPackage = testPackage;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getTestFramework() {
        return testFramework;
    }

    public void setTestFramework(String testFramework) {
        this.testFramework = testFramework;
    }

    public String getTestClassPrefix() {
        return testClassPrefix;
    }

    public void setTestClassPrefix(String testClassPrefix) {
        this.testClassPrefix = testClassPrefix;
    }

    public String getTestClassSuffix() {
        return testClassSuffix;
    }

    public void setTestClassSuffix(String testClassSuffix) {
        this.testClassSuffix = testClassSuffix;
    }

    public String getBaseTestClass() {
        return baseTestClass;
    }

    public void setBaseTestClass(String baseTestClass) {
        this.baseTestClass = baseTestClass;
    }

    public List<TestCaseConfig> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCaseConfig> testCases) {
        this.testCases = testCases;
    }

    public Map<String, Object> getGlobals() {
        return globals;
    }

    public void setGlobals(Map<String, Object> globals) {
        this.globals = globals;
    }
}
