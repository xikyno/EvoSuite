package org.evosuite.template.cli;

import java.util.List;

import org.evosuite.template.analyzer.ClassAnalysisResult;
import org.evosuite.template.analyzer.MethodInfo;
import org.evosuite.template.analyzer.ParameterInfo;

public class ConfigSkeletonGenerator {

    private static final String I2 = "  ";
    private static final String I4 = "    ";
    private static final String I6 = "      ";
    private static final String I8 = "        ";
    private static final String I10 = "          ";

    public String generate(ClassAnalysisResult classInfo) {
        StringBuilder sb = new StringBuilder();
        String pkg = classInfo.getPackageName();
        String testPkg = pkg.isEmpty() ? "com.example.test" : pkg;

        sb.append("{\n");
        sb.append(I2).append("\"_comment\": \"TODO: fill parameters, then copy variants like testAdd1/testAdd2\",\n");
        sb.append(I2).append("\"targetClass\": \"").append(escapeJson(classInfo.getClassName())).append("\",\n");
        sb.append(I2).append("\"testPackage\": \"").append(escapeJson(testPkg)).append("\",\n");
        sb.append(I2).append("\"testFramework\": \"AUTO\",\n");
        sb.append(I2).append("\"testClassPrefix\": \"\",\n");
        sb.append(I2).append("\"testClassSuffix\": \"Test\",\n");
        sb.append(I2).append("\"baseTestClass\": \"\",\n");
        sb.append(I2).append("\"outputDirectory\": \"./src/test/java\",\n");
        sb.append(I2).append("\"globals\": {\n");
        sb.append(I4).append("\"mocks\": []\n");
        sb.append(I2).append("},\n");
        sb.append(I2).append("\"testCases\": [\n");

        List<MethodInfo> methods = classInfo.getMethods();
        boolean isAbstract = classInfo.isAbstract() || classInfo.isInterface();
        int groupIndex = 0;
        for (MethodInfo method : methods) {
            if (!method.isPublic() || method.getName().equals("<clinit>") || method.getName().equals("<init>")) {
                continue;
            }
            if (groupIndex > 0) {
                sb.append(",\n");
            }
            generateMethodGroup(sb, method, isAbstract);
            groupIndex++;
        }

        sb.append("\n");
        sb.append(I2).append("]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private void generateMethodGroup(StringBuilder sb, MethodInfo method, boolean isAbstract) {
        String baseName = capitalize(method.getName());

        sb.append(I4).append("{\n");
        sb.append(I6).append("\"methodName\": \"").append(method.getName()).append("\",\n");
        sb.append(I6).append("\"testNamePrefix\": \"").append(baseName).append("\",\n");
        sb.append(I6).append("\"variants\": [\n");
        sb.append(I8).append("{\n");
        sb.append(I10).append("\"testName\": \"").append(baseName).append("0\",\n");
        sb.append(I10).append("\"description\": \"TODO: scenario for ").append(method.getName()).append("(");
        appendParamTypes(method.getParameters(), sb);
        sb.append(")\",\n");
        sb.append(I10).append("\"methodCalls\": [\n");
        sb.append(I10).append("  { ");
        sb.append("\"methodName\": \"").append(method.getName()).append("\"");
        if (!method.isStatic() && !isAbstract) {
            sb.append(", \"targetVariable\": \"target\"");
        }
        if (!method.isVoid()) {
            sb.append(", \"resultVariable\": \"result\"");
        }
        sb.append(", \"parameters\": [");
        List<ParameterInfo> params = method.getParameters();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\"TODO: ").append(params.get(i).getSimpleTypeName())
                    .append(" ").append(params.get(i).getName()).append("\"");
        }
        sb.append("] }\n");
        sb.append(I10).append("],\n");
        sb.append(I10).append("\"assertions\": {\n");
        if (method.isVoid()) {
            sb.append(I10).append("  \"_comment\": \"TODO: use verify for void method\"\n");
        } else {
            sb.append(I10).append("  \"assertEquals\": { \"TODO: expected value\": \"$ref:result\" }\n");
        }
        sb.append(I10).append("}\n");
        sb.append(I8).append("}\n");
        sb.append(I6).append("]\n");
        sb.append(I4).append("}");
    }

    private void appendParamTypes(List<ParameterInfo> params, StringBuilder sb) {
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(params.get(i).getSimpleTypeName());
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String escapeJson(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
