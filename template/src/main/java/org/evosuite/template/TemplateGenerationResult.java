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
import java.util.ArrayList;
import java.util.List;

/**
 * Result of a template-based test generation run.
 */
public class TemplateGenerationResult {

    private final boolean success;
    private final String targetClass;
    private final File outputFile;
    private final String sourceCode;
    private final int methodCount;
    private final int testCaseCount;
    private final List<String> warnings;
    private final List<String> errors;
    private final long durationMs;

    private TemplateGenerationResult(Builder builder) {
        this.success = builder.success;
        this.targetClass = builder.targetClass;
        this.outputFile = builder.outputFile;
        this.sourceCode = builder.sourceCode;
        this.methodCount = builder.methodCount;
        this.testCaseCount = builder.testCaseCount;
        this.warnings = builder.warnings;
        this.errors = builder.errors;
        this.durationMs = builder.durationMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public int getTestCaseCount() {
        return testCaseCount;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    public long getDurationMs() {
        return durationMs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Template Generation Result:\n");
        sb.append("  Target class: ").append(targetClass).append("\n");
        sb.append("  Success: ").append(success).append("\n");
        sb.append("  Methods analyzed: ").append(methodCount).append("\n");
        sb.append("  Test cases generated: ").append(testCaseCount).append("\n");
        sb.append("  Duration: ").append(durationMs).append("ms\n");
        if (outputFile != null) {
            sb.append("  Output: ").append(outputFile.getAbsolutePath()).append("\n");
        }
        if (!warnings.isEmpty()) {
            sb.append("  Warnings:\n");
            for (String w : warnings) {
                sb.append("    - ").append(w).append("\n");
            }
        }
        if (!errors.isEmpty()) {
            sb.append("  Errors:\n");
            for (String e : errors) {
                sb.append("    - ").append(e).append("\n");
            }
        }
        return sb.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private String targetClass;
        private File outputFile;
        private String sourceCode;
        private int methodCount;
        private int testCaseCount;
        private List<String> warnings = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
        private long durationMs;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder targetClass(String targetClass) {
            this.targetClass = targetClass;
            return this;
        }

        public Builder outputFile(File outputFile) {
            this.outputFile = outputFile;
            return this;
        }

        public Builder sourceCode(String sourceCode) {
            this.sourceCode = sourceCode;
            return this;
        }

        public Builder methodCount(int methodCount) {
            this.methodCount = methodCount;
            return this;
        }

        public Builder testCaseCount(int testCaseCount) {
            this.testCaseCount = testCaseCount;
            return this;
        }

        public Builder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = errors;
            return this;
        }

        public Builder durationMs(long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public TemplateGenerationResult build() {
            return new TemplateGenerationResult(this);
        }
    }
}