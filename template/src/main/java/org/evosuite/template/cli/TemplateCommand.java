/*
 * main() 入口，解析 --target、--config、--skeleton
 */
package org.evosuite.template.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.evosuite.template.TemplateGenerationResult;
import org.evosuite.template.TemplateTestGenerator;
import org.evosuite.template.analyzer.ClassAnalysisResult;
import org.evosuite.template.analyzer.MethodAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standalone CLI entry point for template-based test generation.
 * Can be used independently or integrated into EvoSuite's main CLI.
 */
public class TemplateCommand {

    private static final Logger logger = LoggerFactory.getLogger(TemplateCommand.class);

    public static void main(String[] args) {
        int exitCode = execute(args);
        System.exit(exitCode);
    }

    /**
     * Execute template test generation from command line arguments.
     *
     * @param args command line arguments
     * @return exit code (0 = success, 1 = error)
     */
    public static int execute(String[] args) {
        Options options = TemplateOptions.buildOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption(TemplateOptions.HELP)) {
                printHelp(options);
                return 0;
            }

            // --- Skeleton mode: generate JSON config skeleton from class ---
            if (line.hasOption(TemplateOptions.SKELETON)) {
                return executeSkeleton(line, options);
            }

            // --- Normal mode: generate tests from config ---
            String targetClass = line.getOptionValue(TemplateOptions.TARGET);
            String configPath = line.getOptionValue(TemplateOptions.CONFIG);
            String classpath = line.getOptionValue(TemplateOptions.CLASSPATH);

            if (targetClass == null || configPath == null) {
                System.err.println("Error: --target and --config are required options.");
                printHelp(options);
                return 1;
            }

            File configFile = new File(configPath);
            if (!configFile.exists()) {
                System.err.println("Error: Config file not found: " + configPath);
                return 1;
            }

            // Build classpath list
            List<String> classPathEntries = new ArrayList<>();
            if (classpath != null && !classpath.isEmpty()) {
                String separator = System.getProperty("path.separator", ";");
                classPathEntries.addAll(Arrays.asList(classpath.split(separator)));
            }

            // Run template generation
            TemplateTestGenerator generator = new TemplateTestGenerator();
            TemplateGenerationResult result = generator.generate(configFile, classPathEntries);

            // Print result
            System.out.println(result.toString());

            return result.isSuccess() ? 0 : 1;

        } catch (ParseException e) {
            System.err.println("Error parsing command line: " + e.getMessage());
            printHelp(options);
            return 1;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            logger.error("Template generation failed", e);
            return 1;
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("template-test-gen",
                "\nEvoSuite Template Test Generator\n"
                + "Generates JUnit tests from JSON/XML configuration files.\n\n",
                options,
                "\nExamples:\n"
                + "  # Step 1: Generate a JSON config skeleton from a class\n"
                + "  template-test-gen --skeleton --target com.example.UserService --classpath target/classes -o skeleton.json\n"
                + "\n"
                + "  # Step 2: Edit skeleton.json to fill in TODO values\n"
                + "\n"
                + "  # Step 3: Generate tests from the filled config\n"
                + "  template-test-gen --target com.example.UserService --config skeleton.json --classpath target/classes\n",
                true);
    }

    /**
     * Execute skeleton mode: analyze the target class and output a JSON config skeleton.
     */
    private static int executeSkeleton(CommandLine line, Options options) {
        String targetClass = line.getOptionValue(TemplateOptions.TARGET);
        String classpath = line.getOptionValue(TemplateOptions.CLASSPATH);
        String outputPath = line.getOptionValue(TemplateOptions.OUTPUT);

        if (targetClass == null) {
            System.err.println("Error: --target is required in skeleton mode.");
            printHelp(options);
            return 1;
        }

        try {
            // Build classpath list
            List<String> classPathEntries = new ArrayList<>();
            if (classpath != null && !classpath.isEmpty()) {
                String separator = System.getProperty("path.separator", ";");
                classPathEntries.addAll(Arrays.asList(classpath.split(separator)));
            }

            // Analyze the class
            System.out.println("Analyzing class: " + targetClass + " ...");
            MethodAnalyzer analyzer = new MethodAnalyzer();
            ClassAnalysisResult classInfo = analyzer.analyze(targetClass, classPathEntries);

            System.out.println("Found " + classInfo.getConstructors().size() + " constructors, "
                    + classInfo.getMethods().size() + " methods.");

            // Generate skeleton JSON
            ConfigSkeletonGenerator skeletonGen = new ConfigSkeletonGenerator();
            String skeletonJson = skeletonGen.generate(classInfo);

            // Write to file or stdout
            if (outputPath != null && !outputPath.isEmpty()) {
                File outputFile = new File(outputPath);
                try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
                    writer.print(skeletonJson);
                }
                System.out.println("Config skeleton written to: " + outputFile.getAbsolutePath());
            } else {
                System.out.println(skeletonJson);
            }

            return 0;

        } catch (Exception e) {
            System.err.println("Error generating skeleton: " + e.getMessage());
            logger.error("Skeleton generation failed", e);
            return 1;
        }
    }
}
