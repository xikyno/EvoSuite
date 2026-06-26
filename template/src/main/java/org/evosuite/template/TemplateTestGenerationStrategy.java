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

import org.evosuite.Properties;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter that bridges the template-based test generator with EvoSuite's
 * TestGenerationStrategy interface. This allows the template generator to be
 * used as a strategy within EvoSuite's existing pipeline.
 *
 * <p>This strategy deliberately bypasses the genetic algorithm entirely.
 * The generateTests() method performs class analysis, loads configuration,
 * generates test code via templates, and writes directly to disk — then
 * returns an empty TestSuiteChromosome to satisfy the interface contract.
 */
public class TemplateTestGenerationStrategy extends TestGenerationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(TemplateTestGenerationStrategy.class);

    private final String targetClassName;
    private final File configFile;

    /**
     * Create a template-based test generation strategy.
     *
     * @param targetClassName fully qualified target class name
     * @param configFile      JSON or XML configuration file
     */
    public TemplateTestGenerationStrategy(String targetClassName, File configFile) {
        this.targetClassName = targetClassName;
        this.configFile = configFile;
    }

    @Override
    public TestSuiteChromosome generateTests() {
        logger.info("TemplateTestGenerationStrategy: generating tests for {}", targetClassName);

        // Disable GA-related features that are not needed for template generation
        Properties.P_FUNCTIONAL_MOCKING = 0;
        Properties.INSTRUMENTATION_SKIP_DEBUG = true;

        // Use the template generator to produce tests
        TemplateTestGenerator generator = new TemplateTestGenerator();
        TemplateGenerationResult result = generator.generate(configFile, null);

        if (result.isSuccess()) {
            logger.info("Template test generation succeeded: {}",
                    result.getOutputFile() != null
                            ? result.getOutputFile().getAbsolutePath() : "no output file");
        } else {
            logger.error("Template test generation failed: {}", result.getErrors());
        }

        // Return empty TestSuiteChromosome — the actual test code has already been
        // written to disk by the TemplateTestGenerator
        return new TestSuiteChromosome();
    }
}