/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.template;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.template.analyzer.ClassAnalysisResult;
import org.evosuite.template.analyzer.MethodAnalyzer;
import org.evosuite.template.analyzer.MethodInfo;
import org.evosuite.template.config.ConfigLoadException;
import org.evosuite.template.config.ConfigLoader;
import org.evosuite.template.config.JsonConfigLoader;
import org.evosuite.template.config.TestCaseConfig;
import org.evosuite.template.config.TestDataConfig;
import org.evosuite.template.config.TestMethodConfig;
import org.evosuite.template.config.XmlConfigLoader;
import org.evosuite.template.engine.TemplateRenderException;
import org.evosuite.template.writer.JUnitTestWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for template-based test generation.
 * Coordinates class analysis, config loading, and test code generation.
 *
 * <p>This is a standalone generator that does NOT use EvoSuite's genetic algorithm.
 * It bypasses the GA pipeline entirely and generates deterministic, template-driven
 * JUnit tests from JSON/XML configuration files.
 */
public class TemplateTestGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TemplateTestGenerator.class);

    private final MethodAnalyzer methodAnalyzer;
    private final JUnitTestWriter testWriter;
    private final List<ConfigLoader> configLoaders;

    public TemplateTestGenerator() {
        this.methodAnalyzer = new MethodAnalyzer();
        this.testWriter = new JUnitTestWriter();
        this.configLoaders = new ArrayList<>();
        this.configLoaders.add(new JsonConfigLoader());
        this.configLoaders.add(new XmlConfigLoader());
    }

    /**
     * Generate tests from a configuration file.
     *
     * @param configFile     path to JSON or XML config file
     * @param classPathEntries additional classpath entries for the target project
     * @return generation result
     */
    public TemplateGenerationResult generate(File configFile, List<String> classPathEntries) {
        long startTime = System.currentTimeMillis();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            // 1. Load configuration
            TestDataConfig config = loadConfig(configFile);
            ensureRuntimeConfigPath(config, configFile);
            String targetClass = config.getTargetClass();
            if (targetClass == null || targetClass.isEmpty()) {
                throw new IllegalArgumentException(
                        "targetClass not specified in config file: " + configFile);
            }
            logger.info("Generating template tests for: {}", targetClass);

            // 2. Setup classpath
            setupClasspath(classPathEntries);

            // 3. Analyze the target class
            ClassAnalysisResult classInfo = methodAnalyzer.analyze(targetClass, classPathEntries);
            logger.info("Class analysis: {} methods, {} constructors",
                    classInfo.getMethods().size(), classInfo.getConstructors().size());

            // 4. Validate config against class analysis
            List<String> configWarnings = validateConfig(config, classInfo);
            warnings.addAll(configWarnings);

            // 5. Generate test code
            String sourceCode = testWriter.generateTestClass(classInfo, config);

            // 6. Write to disk
            File outputFile = testWriter.writeToFile(sourceCode, config);
            testWriter.writeConfigResource(config);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Template test generation completed in {}ms", duration);

            return TemplateGenerationResult.builder()
                    .success(true)
                    .targetClass(targetClass)
                    .outputFile(outputFile)
                    .sourceCode(sourceCode)
                    .methodCount(classInfo.getTotalMethodCount())
                    .testCaseCount(config.getTestCases().size())
                    .warnings(warnings)
                    .errors(errors)
                    .durationMs(duration)
                    .build();

        } catch (Exception e) {
            logger.error("Template test generation failed", e);
            errors.add(e.getMessage());
            long duration = System.currentTimeMillis() - startTime;

            return TemplateGenerationResult.builder()
                    .success(false)
                    .targetClass("unknown")
                    .warnings(warnings)
                    .errors(errors)
                    .durationMs(duration)
                    .build();
        }
    }

    /**
     * Generate tests with a custom template string instead of the default template.
     */
    public TemplateGenerationResult generateWithCustomTemplate(File configFile,
                                                                List<String> classPathEntries,
                                                                String customTemplate) {
        long startTime = System.currentTimeMillis();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            TestDataConfig config = loadConfig(configFile);
            ensureRuntimeConfigPath(config, configFile);
            String targetClass = config.getTargetClass();

            setupClasspath(classPathEntries);
            ClassAnalysisResult classInfo = methodAnalyzer.analyze(targetClass, classPathEntries);

            String sourceCode = testWriter.generateTestClass(classInfo, config, customTemplate);
            File outputFile = testWriter.writeToFile(sourceCode, config);
            testWriter.writeConfigResource(config);

            long duration = System.currentTimeMillis() - startTime;

            return TemplateGenerationResult.builder()
                    .success(true)
                    .targetClass(targetClass)
                    .outputFile(outputFile)
                    .sourceCode(sourceCode)
                    .methodCount(classInfo.getTotalMethodCount())
                    .testCaseCount(config.getTestCases().size())
                    .warnings(warnings)
                    .errors(errors)
                    .durationMs(duration)
                    .build();

        } catch (Exception e) {
            logger.error("Template test generation failed", e);
            errors.add(e.getMessage());
            long duration = System.currentTimeMillis() - startTime;

            return TemplateGenerationResult.builder()
                    .success(false)
                    .targetClass("unknown")
                    .warnings(warnings)
                    .errors(errors)
                    .durationMs(duration)
                    .build();
        }
    }

    /**
     * Validate that all methods referenced in the config actually exist in the class.
     */
    private List<String> validateConfig(TestDataConfig config, ClassAnalysisResult classInfo) {
        List<String> warnings = new ArrayList<>();

        for (TestCaseConfig testCase : config.getTestCases()) {
            List<TestCaseConfig> effectiveCases = testCase.hasVariants()
                    ? testCase.getVariants() : java.util.Collections.singletonList(testCase);
            for (TestCaseConfig effectiveCase : effectiveCases) {
                for (TestMethodConfig call : effectiveCase.getMethodCalls()) {
                if (call.isConstructorCall()) {
                    // Verify constructor exists
                    boolean found = false;
                    for (MethodInfo ctor : classInfo.getConstructors()) {
                        if (ctor.getParameters().size() == call.getParameters().size()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        warnings.add("No constructor with " + call.getParameters().size()
                                + " parameters found for class: " + call.getConstructorFor());
                    }
                } else {
                    // Verify method exists
                    MethodInfo method = classInfo.findMethod(call.getMethodName());
                    if (method == null) {
                        warnings.add("Method not found in class: "
                                + call.getMethodName());
                    } else if (method.getParameters().size() != call.getParameters().size()) {
                        warnings.add("Parameter count mismatch for method '"
                                + call.getMethodName() + "': expected "
                                + method.getParameters().size() + ", got "
                                + call.getParameters().size());
                    }
                }
            }
            }
        }
        return warnings;
    }

    /**
     * Load configuration from file, auto-detecting format.
     */
    private TestDataConfig loadConfig(File configFile) throws ConfigLoadException {
        for (ConfigLoader loader : configLoaders) {
            if (loader.supports(configFile)) {
                return loader.load(configFile);
            }
        }
        throw new ConfigLoadException(
                "Unsupported config file format: " + configFile.getName()
                + ". Supported formats: .json, .xml");
    }

    /**
     * Setup the classpath for class analysis.
     */
    private void setupClasspath(List<String> classPathEntries) {
        if (classPathEntries != null && !classPathEntries.isEmpty()) {
            for (String entry : classPathEntries) {
                if (entry != null && !entry.isEmpty()) {
                    ClassPathHandler.getInstance().addElementToTargetProjectClassPath(entry);
                }
            }
        }
    }

    private void ensureRuntimeConfigPath(TestDataConfig config, File configFile) {
        if (config.getGlobals() == null) {
            config.setGlobals(new java.util.HashMap<String, Object>());
        }
        if (!config.getGlobals().containsKey("_runtimeConfigPath")) {
            config.getGlobals().put("_runtimeConfigPath", configFile.getAbsolutePath());
        }
    }
}
