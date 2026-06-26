/*
 *  CLI 选项定义
 */
package org.evosuite.template.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * CLI option definitions for template-based test generation.
 */
public final class TemplateOptions {

    public static final String HELP = "help";
    public static final String TARGET = "target";
    public static final String CONFIG = "config";
    public static final String CLASSPATH = "classpath";
    public static final String OUTPUT = "output";
    public static final String TEMPLATE = "template";
    public static final String VALIDATE_ONLY = "validate-only";
    public static final String SKELETON = "skeleton";

    private TemplateOptions() {
        // Utility class
    }

    public static Options buildOptions() {
        Options options = new Options();

        options.addOption(Option.builder("h")
                .longOpt(HELP)
                .desc("Print this help message")
                .build());

        options.addOption(Option.builder("t")
                .longOpt(TARGET)
                .hasArg()
                .argName("class")
                .desc("Fully qualified target class name (e.g., com.example.UserService)")
                .build());

        options.addOption(Option.builder("c")
                .longOpt(CONFIG)
                .hasArg()
                .argName("file")
                .desc("Path to JSON or XML test data configuration file")
                .build());

        options.addOption(Option.builder("cp")
                .longOpt(CLASSPATH)
                .hasArg()
                .argName("path")
                .desc("Classpath of the target project (colon/semicolon separated)")
                .build());

        options.addOption(Option.builder("o")
                .longOpt(OUTPUT)
                .hasArg()
                .argName("dir")
                .desc("Output directory for generated tests (default: ./generated-tests)")
                .build());

        options.addOption(Option.builder("tmpl")
                .longOpt(TEMPLATE)
                .hasArg()
                .argName("file")
                .desc("Custom template file to use for code generation")
                .build());

        options.addOption(Option.builder("v")
                .longOpt(VALIDATE_ONLY)
                .desc("Only validate the config file against the class, do not generate tests")
                .build());

        options.addOption(Option.builder("s")
                .longOpt(SKELETON)
                .desc("Generate a JSON config skeleton from the target class (--config not required)")
                .build());

        return options;
    }
}