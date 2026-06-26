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
package org.evosuite.executionmode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.template.TemplateGenerationResult;
import org.evosuite.template.TemplateTestGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execution mode for template-based test generation.
 * This is a separate execution mode that bypasses the GA pipeline entirely
 * and runs in-process (no RMI, no client process spawning).
 */
public class TemplateTestGeneration {

    private static final Logger logger = LoggerFactory.getLogger(TemplateTestGeneration.class);

    private TemplateTestGeneration() {
        // Utility class
    }

    /**
     * Execute template-based test generation.
     *
     * @param options  the CLI options
     * @param javaOpts Java options
     * @param line     the parsed command line
     * @return null (result is printed to stdout and written to disk)
     */
    public static Object execute(Options options, List<String> javaOpts, CommandLine line) {
        try {
            String configPath = line.getOptionValue("generateTemplate");
            if (configPath == null || configPath.isEmpty()) {
                logger.error("No config file specified for -generateTemplate");
                System.err.println("Error: -generateTemplate requires a JSON/XML config file path");
                return null;
            }

            File configFile = new File(configPath);
            if (!configFile.exists()) {
                logger.error("Config file not found: {}", configPath);
                System.err.println("Error: Config file not found: " + configPath);
                return null;
            }

            // Set target class from command line if provided
            if (line.hasOption("class")) {
                Properties.TARGET_CLASS = line.getOptionValue("class");
            }

            // Build classpath from projectCP and target options
            List<String> classPathEntries = new ArrayList<>();
            if (line.hasOption("projectCP")) {
                String separator = System.getProperty("path.separator", ";");
                classPathEntries.addAll(
                        Arrays.asList(line.getOptionValue("projectCP").split(separator)));
            }
            if (line.hasOption("target")) {
                classPathEntries.add(line.getOptionValue("target"));
            }

            // Add system classpath as fallback
            if (classPathEntries.isEmpty()) {
                String sysClasspath = System.getProperty("java.class.path");
                if (sysClasspath != null) {
                    classPathEntries.addAll(
                            Arrays.asList(sysClasspath.split(System.getProperty("path.separator", ";"))));
                }
            }

            // Run template generation
            TemplateTestGenerator generator = new TemplateTestGenerator();
            TemplateGenerationResult result = generator.generate(configFile, classPathEntries);

            // Print result
            System.out.println(result.toString());

            if (result.isSuccess()) {
                logger.info("Template test generation completed successfully");
                // Write source code to disk (already done by generator, but print path)
                if (result.getOutputFile() != null) {
                    System.out.println("Test file written to: "
                            + result.getOutputFile().getAbsolutePath());
                }
            } else {
                logger.error("Template test generation failed");
                System.err.println("Generation failed: " + result.getErrors());
            }

            return result;

        } catch (Exception e) {
            logger.error("Template test generation failed with exception", e);
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }
}