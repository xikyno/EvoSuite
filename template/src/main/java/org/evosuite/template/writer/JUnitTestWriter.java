package org.evosuite.template.writer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.evosuite.template.analyzer.ClassAnalysisResult;
import org.evosuite.template.analyzer.ParameterInfo;
import org.evosuite.template.analyzer.MethodInfo;
import org.evosuite.template.config.MockConfig;
import org.evosuite.template.config.StubConfig;
import org.evosuite.template.config.TestCaseConfig;
import org.evosuite.template.config.TestDataConfig;
import org.evosuite.template.config.TestMethodConfig;
import org.evosuite.template.engine.BuiltinFunctions;
import org.evosuite.template.engine.SimpleTemplateEngine;
import org.evosuite.template.engine.TemplateContext;
import org.evosuite.template.engine.TemplateEngine;
import org.evosuite.template.engine.TemplateRenderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitTestWriter {

    private static final Logger logger = LoggerFactory.getLogger(JUnitTestWriter.class);
    private static final String DEFAULT_TEMPLATE = "default-junit4.ftl";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TemplateEngine templateEngine;
    private final CodeFormatter formatter;

    public JUnitTestWriter() {
        this.templateEngine = new SimpleTemplateEngine();
        this.formatter = new CodeFormatter();
    }

    public String generateTestClass(ClassAnalysisResult classInfo, TestDataConfig config)
            throws TemplateRenderException {
        TemplateContext context = buildContext(classInfo, config);
        String raw = ((SimpleTemplateEngine) templateEngine).renderResource(DEFAULT_TEMPLATE, context);
        return formatter.cleanup(raw);
    }

    public String generateTestClass(ClassAnalysisResult classInfo, TestDataConfig config,
                                    String customTemplate) throws TemplateRenderException {
        TemplateContext context = buildContext(classInfo, config);
        return templateEngine.render(customTemplate, context);
    }

    public File writeToFile(String sourceCode, TestDataConfig config) throws IOException {
        String testClassName = getTestClassName(config);
        String outputDir = resolveJavaOutputDir(config);
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File outputFile = new File(dir, testClassName + ".java");
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            writer.write(sourceCode);
        }
        logger.info("Test class written to: {}", outputFile.getAbsolutePath());
        return outputFile;
    }

    public File writeConfigResource(TestDataConfig config) throws IOException {
        String testClassName = getTestClassName(config);
        String outputDir = resolveResourceOutputDir(config);
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File outputFile = new File(dir, testClassName + ".json");
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            writer.write(OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(sanitizeConfigForRuntime(config)));
        }
        logger.info("Runtime config written to: {}", outputFile.getAbsolutePath());
        return outputFile;
    }

    private String getTestClassName(TestDataConfig config) {
        String className = config.getTargetClass();
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        String prefix = config.getTestClassPrefix() != null ? config.getTestClassPrefix() : "";
        String suffix = config.getTestClassSuffix() != null ? config.getTestClassSuffix() : "Test";
        return prefix.isEmpty() ? simpleName + suffix : prefix + simpleName + suffix;
    }

    private TemplateContext buildContext(ClassAnalysisResult classInfo, TestDataConfig config) {
        TemplateContext ctx = new TemplateContext();
        ctx.put("classInfo", classInfo);
        ctx.put("className", classInfo.getClassName());
        ctx.put("simpleName", classInfo.getSimpleName());
        ctx.put("packageName", classInfo.getPackageName());

        ctx.put("config", config);
        ctx.put("testPackage", config.getTestPackage() != null ? config.getTestPackage() : classInfo.getPackageName());

        String testFramework = resolveTestFramework(classInfo, config);
        boolean useSpringBoot = "SPRING_BOOT".equals(testFramework);
        boolean useMockito = !useSpringBoot;
        ctx.put("useSpringBoot", useSpringBoot);
        ctx.put("useMockito", useMockito);
        ctx.put("mockAnnotation", useSpringBoot ? "MockBean" : "Mock");
        ctx.put("targetClassName", classInfo.getClassName());
        ctx.put("targetSimpleName", classInfo.getSimpleName());
        ctx.put("targetVariable", "target");
        ctx.put("testClassName", getTestClassName(config));

        boolean hasBaseTestClass = config.getBaseTestClass() != null && !config.getBaseTestClass().trim().isEmpty();
        ctx.put("hasBaseTestClass", hasBaseTestClass);
        ctx.put("baseTestSimpleName", hasBaseTestClass ? BuiltinFunctions.simpleName(config.getBaseTestClass()) : "");
        ctx.put("needsMockitoLifecycle", !hasBaseTestClass && useMockito);
        ctx.put("generationTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        List<Map<String, Object>> globalMocks = processGlobalMocks(config);
        ctx.put("globalMocks", globalMocks);

        Set<String> allImports = new HashSet<>(classInfo.getImports());
        allImports.add("org.junit.jupiter.api.Assertions");
        allImports.add("org.junit.jupiter.api.Test");
        if (useSpringBoot) {
            allImports.add("org.springframework.beans.factory.annotation.Autowired");
            allImports.add("org.springframework.boot.test.context.SpringBootTest");
        } else {
            allImports.add("org.mockito.InjectMocks");
        }
        if (hasBaseTestClass) {
            allImports.add(config.getBaseTestClass());
        } else if (useMockito) {
            allImports.add("org.mockito.MockitoAnnotations");
            allImports.add("org.junit.jupiter.api.AfterEach");
            allImports.add("org.junit.jupiter.api.BeforeEach");
        }

        boolean hasMocks = !globalMocks.isEmpty() || config.getTestCases().stream().anyMatch(tc -> tc.getMocks() != null && !tc.getMocks().isEmpty());
        if (hasMocks) {
            allImports.add("org.mockito.Mockito");
            allImports.add(useSpringBoot ? "org.springframework.boot.test.mock.mockito.MockBean" : "org.mockito.Mock");
        }
        allImports.add("java.util.List");
        allImports.add("java.util.Map");
        allImports.add("java.util.Collections");
        allImports.add("com.fasterxml.jackson.core.type.TypeReference");
        allImports.add("com.fasterxml.jackson.databind.ObjectMapper");
        List<String> imports = new ArrayList<>(allImports);
        Collections.sort(imports);
        ctx.put("imports", imports);

        List<Map<String, Object>> processedCases = new ArrayList<>();
        for (TestCaseConfig testCase : config.getTestCases()) {
            processedCases.add(processMethodGroup(testCase, classInfo, globalMocks));
        }
        ctx.put("testCases", processedCases);
        ctx.put("methods", classInfo.getMethods());
        ctx.put("constructors", classInfo.getConstructors());
        ctx.put("runtimeConfigPath", BuiltinFunctions.toJavaLiteral(resolveConfigResourcePath(config)));
        return ctx;
    }

    private String resolveTestFramework(ClassAnalysisResult classInfo, TestDataConfig config) {
        String configured = config.getTestFramework() != null ? config.getTestFramework().trim().toUpperCase() : "AUTO";
        if ("SPRINGBOOT".equals(configured) || "SPRING_BOOT".equals(configured)) {
            return "SPRING_BOOT";
        }
        if ("MOCKITO".equals(configured)) {
            return "MOCKITO";
        }
        return shouldUseSpringBoot(classInfo) ? "SPRING_BOOT" : "MOCKITO";
    }

    private boolean shouldUseSpringBoot(ClassAnalysisResult classInfo) {
        for (String annotation : classInfo.getAnnotationNames()) {
            if (isSpringStereotype(annotation)) {
                return true;
            }
        }
        for (String annotation : classInfo.getFieldAnnotationNames()) {
            if (annotation.startsWith("org.springframework.")) {
                return true;
            }
        }
        for (String fieldType : classInfo.getFieldTypes()) {
            if (fieldType.startsWith("org.springframework.")) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpringStereotype(String annotation) {
        return "org.springframework.stereotype.Service".equals(annotation)
                || "org.springframework.stereotype.Component".equals(annotation)
                || "org.springframework.stereotype.Repository".equals(annotation)
                || "org.springframework.stereotype.Controller".equals(annotation)
                || "org.springframework.web.bind.annotation.RestController".equals(annotation)
                || "org.springframework.context.annotation.Configuration".equals(annotation);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> processGlobalMocks(TestDataConfig config) {
        List<Map<String, Object>> processed = new ArrayList<>();
        Object mocks = config.getGlobals() != null ? config.getGlobals().get("mocks") : null;
        if (!(mocks instanceof List)) {
            return processed;
        }
        for (Object item : (List<Object>) mocks) {
            if (item instanceof Map) {
                Map<String, Object> raw = (Map<String, Object>) item;
                MockConfig mock = new MockConfig();
                mock.setMockClass(String.valueOf(raw.get("mockClass")));
                mock.setMockVariable(String.valueOf(raw.get("mockVariable")));
                processed.add(processMock(mock));
            }
        }
        return processed;
    }

    private Map<String, Object> processMethodGroup(TestCaseConfig testCase, ClassAnalysisResult classInfo,
                                                   List<Map<String, Object>> globalMocks) {
        Map<String, Object> group = new HashMap<>();
        String methodName = testCase.getMethodName();
        String testMethodName = "test" + capitalize(methodName);
        group.put("testName", testMethodName);
        group.put("methodName", methodName);

        MethodInfo methodInfo = findMethodInfo(methodName, classInfo);
        group.put("isStaticMethod", methodInfo != null && methodInfo.isStatic());
        group.put("hasReturnValue", methodInfo != null && !methodInfo.isVoid());
        group.put("returnType", methodInfo != null ? methodInfo.getSimpleReturnTypeName() : "Object");
        group.put("constructorSetupCode", buildConstructorSetupCode(classInfo, methodInfo));
        group.put("parameterLoadCode", buildParameterLoadCode(methodInfo));
        group.put("exceptionInvocationCode", buildInvocationCode(classInfo, methodInfo, true));
        group.put("normalInvocationCode", buildInvocationCode(classInfo, methodInfo, false));

        List<Map<String, Object>> methodParameters = new ArrayList<>();
        if (methodInfo != null) {
            int index = 0;
            for (ParameterInfo parameter : methodInfo.getParameters()) {
                Map<String, Object> parameterMap = new HashMap<>();
                parameterMap.put("name", parameter.getName());
                parameterMap.put("typeName", parameter.getTypeName());
                parameterMap.put("simpleTypeName", parameter.getSimpleTypeName());
                parameterMap.put("index", index++);
                methodParameters.add(parameterMap);
            }
        }
        group.put("methodParameters", methodParameters);

        MethodInfo constructorInfo = findConstructorInfo(classInfo);
        List<Map<String, Object>> constructorParameters = new ArrayList<>();
        if (constructorInfo != null) {
            int index = 0;
            for (ParameterInfo parameter : constructorInfo.getParameters()) {
                Map<String, Object> parameterMap = new HashMap<>();
                parameterMap.put("name", parameter.getName());
                parameterMap.put("typeName", parameter.getTypeName());
                parameterMap.put("simpleTypeName", parameter.getSimpleTypeName());
                parameterMap.put("index", index++);
                constructorParameters.add(parameterMap);
            }
        }
        group.put("constructorParameters", constructorParameters);
        group.put("hasConstructor", constructorInfo != null
                && !classInfo.isAbstract()
                && !classInfo.isInterface()
                && methodInfo != null
                && !methodInfo.isStatic());

        List<Map<String, Object>> processedMocks = new ArrayList<>();
        processedMocks.addAll(globalMocks);
        if (testCase.getMocks() != null) {
            for (MockConfig mock : testCase.getMocks()) {
                mergeOrAddMock(processedMocks, processMock(mock));
            }
        }
        group.put("mocks", processedMocks);
        group.put("hasMocks", !processedMocks.isEmpty());
        group.put("variantsJsonKey", methodName);
        return group;
    }

    private Map<String, Object> processTestCase(TestCaseConfig testCase, ClassAnalysisResult classInfo,
                                                List<Map<String, Object>> globalMocks, String prefix) {
        Map<String, Object> tc = new HashMap<>();
        String testName = testCase.getTestName();
        if (prefix != null && !prefix.isEmpty()) {
            testName = testName == null || testName.isEmpty() ? prefix : testName;
        }
        tc.put("testName", testName);
        tc.put("description", testCase.getDescription() != null ? testCase.getDescription() : "Test: " + testName);
        tc.put("expectedException", testCase.getExpectedException());
        tc.put("hasExpectedException", testCase.getExpectedException() != null && !testCase.getExpectedException().isEmpty());

        List<Map<String, Object>> processedMocks = new ArrayList<>();
        processedMocks.addAll(globalMocks);
        if (testCase.getMocks() != null) {
            for (MockConfig mock : testCase.getMocks()) {
                mergeOrAddMock(processedMocks, processMock(mock));
            }
        }
        tc.put("mocks", processedMocks);
        tc.put("hasMocks", !processedMocks.isEmpty());

        List<Map<String, Object>> processedCalls = new ArrayList<>();
        for (TestMethodConfig call : testCase.getMethodCalls()) {
            processedCalls.add(processMethodCall(call, classInfo));
        }
        tc.put("methodCalls", processedCalls);
        tc.put("setupCalls", processedCalls.isEmpty() ? Collections.emptyList()
                : processedCalls.subList(0, Math.max(0, processedCalls.size() - 1)));
        tc.put("exceptionCall", processedCalls.isEmpty() ? "" : processedCalls.get(processedCalls.size() - 1).get("callExpression"));
        tc.put("assertions", processAssertions(testCase.getAssertions()));
        return tc;
    }

    private void mergeOrAddMock(List<Map<String, Object>> processedMocks, Map<String, Object> newMock) {
        Object newVariable = newMock.get("mockVariable");
        for (Map<String, Object> existing : processedMocks) {
            if (newVariable != null && newVariable.equals(existing.get("mockVariable"))) {
                existing.put("stubs", newMock.get("stubs"));
                existing.put("hasStubs", newMock.get("hasStubs"));
                return;
            }
        }
        processedMocks.add(newMock);
    }

    private Map<String, Object> processMethodCall(TestMethodConfig call, ClassAnalysisResult classInfo) {
        Map<String, Object> mc = new HashMap<>();
        mc.put("methodName", call.getMethodName());
        mc.put("targetVariable", call.getTargetVariable());
        mc.put("resultVariable", call.getResultVariable());
        mc.put("constructorFor", call.getConstructorFor());

        MethodInfo methodInfo = call.isConstructorCall() ? findConstructorInfo(classInfo) : findMethodInfo(call.getMethodName(), classInfo);
        mc.put("returnType", methodInfo != null ? methodInfo.getSimpleReturnTypeName() : "Object");

        List<Map<String, Object>> processedParams = new ArrayList<>();
        StringBuilder paramLiterals = new StringBuilder();
        List<Object> paramValues = call.getParameters();
        for (int i = 0; i < paramValues.size(); i++) {
            if (i > 0) {
                paramLiterals.append(", ");
            }
            String literal = BuiltinFunctions.toJavaLiteral(paramValues.get(i));
            paramLiterals.append(literal);

            Map<String, Object> param = new HashMap<>();
            String paramType = "Object";
            if (methodInfo != null && i < methodInfo.getParameters().size()) {
                paramType = methodInfo.getParameters().get(i).getSimpleTypeName();
            }
            param.put("paramLiteral", literal);
            param.put("paramType", paramType);
            param.put("paramIndex", i);
            processedParams.add(param);
        }
        mc.put("parameters", processedParams);
        mc.put("paramLiterals", paramLiterals.toString());
        mc.put("callExpression", buildCallExpression(call, methodInfo, paramLiterals.toString()));
        return mc;
    }

    private String buildCallExpression(TestMethodConfig call, MethodInfo methodInfo, String paramLiterals) {
        if (call.isConstructorCall()) {
            String var = call.getResultVariable() != null ? call.getResultVariable() : "target";
            return call.getConstructorFor() + " " + var + " = new " + BuiltinFunctions.simpleName(call.getConstructorFor())
                    + "(" + paramLiterals + ")";
        }
        String receiver = call.getTargetVariable();
        if ((receiver == null || receiver.isEmpty()) && methodInfo != null && !methodInfo.isStatic()) {
            receiver = "target";
        }
        String invocation;
        if (receiver != null && !receiver.isEmpty()) {
            invocation = receiver + "." + call.getMethodName() + "(" + paramLiterals + ")";
        } else if (methodInfo != null && methodInfo.isStatic()) {
            invocation = BuiltinFunctions.simpleName(methodInfo.getDeclaringClass()) + "."
                    + call.getMethodName() + "(" + paramLiterals + ")";
        } else {
            invocation = call.getMethodName() + "(" + paramLiterals + ")";
        }
        if (call.getResultVariable() != null && !call.getResultVariable().isEmpty()) {
            String returnType = methodInfo != null ? methodInfo.getSimpleReturnTypeName() : "Object";
            return returnType + " " + call.getResultVariable() + " = " + invocation;
        }
        return invocation;
    }

    private MethodInfo findMethodInfo(String methodName, ClassAnalysisResult classInfo) {
        for (MethodInfo m : classInfo.getMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        for (MethodInfo c : classInfo.getConstructors()) {
            if (c.getName().equals(methodName)) {
                return c;
            }
        }
        return null;
    }

    private MethodInfo findConstructorInfo(ClassAnalysisResult classInfo) {
        return classInfo.getConstructors().isEmpty() ? null : classInfo.getConstructors().get(0);
    }

    private Map<String, Object> processMock(MockConfig mock) {
        Map<String, Object> m = new HashMap<>();
        m.put("mockClass", mock.getMockClass());
        m.put("mockVariable", mock.getMockVariable());
        String simpleName = BuiltinFunctions.simpleName(mock.getMockClass());
        m.put("mockSimpleName", simpleName);
        m.put("injectMode", mock.getInjectMode() != null ? mock.getInjectMode() : "constructor");
        m.put("setterMethod", mock.getSetterMethod());
        m.put("fieldName", mock.getFieldName());

        List<Map<String, Object>> processedStubs = new ArrayList<>();
        if (mock.getStubs() != null) {
            for (StubConfig stub : mock.getStubs()) {
                processedStubs.add(processStub(stub));
            }
        }
        m.put("stubs", processedStubs);
        m.put("hasStubs", !processedStubs.isEmpty());
        m.put("mockCreation", simpleName + " " + mock.getMockVariable() + " = mock(" + simpleName + ".class)");
        return m;
    }

    private Map<String, Object> processStub(StubConfig stub) {
        Map<String, Object> s = new HashMap<>();
        s.put("method", stub.getMethod());
        StringBuilder matchers = new StringBuilder();
        if (stub.getMatchers() != null) {
            for (int i = 0; i < stub.getMatchers().size(); i++) {
                if (i > 0) {
                    matchers.append(", ");
                }
                matchers.append(stub.getMatchers().get(i));
            }
        }
        s.put("matchers", matchers.toString());

        if (stub.getReturns() != null && !stub.getReturns().isEmpty()) {
            StringBuilder returns = new StringBuilder();
            for (int i = 0; i < stub.getReturns().size(); i++) {
                if (i > 0) {
                    returns.append(", ");
                }
                returns.append(BuiltinFunctions.toJavaLiteral(stub.getReturns().get(i)));
            }
            s.put("returns", returns.toString());
            s.put("hasReturns", true);
            s.put("throwsException", false);
        } else if (stub.getThrowException() != null) {
            s.put("throwException", stub.getThrowException());
            s.put("hasReturns", false);
            s.put("throwsException", true);
        } else {
            s.put("hasReturns", false);
            s.put("throwsException", false);
        }
        return s;
    }

    private List<String> processAssertions(Map<String, Object> assertions) {
        List<String> lines = new ArrayList<>();
        if (assertions == null) {
            return lines;
        }
        for (Map.Entry<String, Object> entry : assertions.entrySet()) {
            String assertionType = entry.getKey();
            Object value = entry.getValue();
            if (assertionType == null || assertionType.startsWith("_")) {
                continue;
            }
            switch (assertionType) {
                case "assertEquals":
                    if (value instanceof Map) {
                        Map<String, Object> eqMap = (Map<String, Object>) value;
                        for (Map.Entry<String, Object> eqEntry : eqMap.entrySet()) {
                            lines.add("Assertions.assertEquals(" + BuiltinFunctions.toJavaLiteral(eqEntry.getKey())
                                    + ", " + resolveRef(String.valueOf(eqEntry.getValue())) + ");");
                        }
                    }
                    break;
                default:
                    if (value instanceof String) {
                        String custom = resolveRef((String) value);
                        lines.add(custom.endsWith(";") ? custom : custom + ";");
                    }
                    break;
            }
        }
        return lines;
    }

    private String resolveRef(String value) {
        if (value == null) {
            return "null";
        }
        if (value.startsWith("$ref:")) {
            return value.substring(5);
        }
        return value;
    }

    private String decapitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() > 1 && Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(value.charAt(1))) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String buildConstructorSetupCode(ClassAnalysisResult classInfo, MethodInfo methodInfo) {
        MethodInfo constructorInfo = findConstructorInfo(classInfo);
        if (constructorInfo == null || classInfo.isAbstract() || classInfo.isInterface()
                || methodInfo == null || methodInfo.isStatic()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("            if (calls.isEmpty()) {\n");
        sb.append("                continue;\n");
        sb.append("            }\n");
        sb.append("            Map<String, Object> constructorCall = calls.get(0);\n");
        sb.append("            List<Object> constructorParams = parametersOf(constructorCall);\n");
        sb.append("            final ").append(classInfo.getSimpleName()).append(" target = new ")
                .append(classInfo.getSimpleName()).append("(");
        List<String> args = new ArrayList<>();
        for (int i = 0; i < constructorInfo.getParameters().size(); i++) {
            args.add(conversionExpression(constructorInfo.getParameters().get(i), "constructorParams.get(" + i + ")"));
        }
        sb.append(String.join(", ", args)).append(");\n");
        sb.append("            runtime.put(\"target\", target);\n");
        return sb.toString();
    }

    private String buildParameterLoadCode(MethodInfo methodInfo) {
        if (methodInfo == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("            Map<String, Object> methodCall = calls.get(calls.size() - 1);\n");
        sb.append("            List<Object> params = parametersOf(methodCall);\n");
        for (int i = 0; i < methodInfo.getParameters().size(); i++) {
            ParameterInfo parameter = methodInfo.getParameters().get(i);
            sb.append("            ").append(parameter.getSimpleTypeName()).append(" ").append(parameter.getName())
                    .append(" = ").append(conversionExpression(parameter, "params.get(" + i + ")")).append(";\n");
        }
        return sb.toString();
    }

    private String buildInvocationCode(ClassAnalysisResult classInfo, MethodInfo methodInfo, boolean forException) {
        if (methodInfo == null) {
            return "";
        }
        String targetExpr = methodInfo.isStatic()
                ? classInfo.getSimpleName() + "." + methodInfo.getName()
                : "target." + methodInfo.getName();
        List<String> args = new ArrayList<>();
        for (ParameterInfo parameter : methodInfo.getParameters()) {
            args.add(parameter.getName());
        }
        String invocation = targetExpr + "(" + String.join(", ", args) + ")";
        if (forException || methodInfo.isVoid()) {
            return invocation + ";";
        }
        return methodInfo.getSimpleReturnTypeName() + " result = " + invocation + ";\n"
                + "            runtime.put(\"result\", result);";
    }

    private String conversionExpression(ParameterInfo parameter, String sourceExpression) {
        String simpleType = parameter.getSimpleTypeName();
        if ("int".equals(simpleType)) {
            return "asInt(" + sourceExpression + ", runtime)";
        }
        if ("long".equals(simpleType)) {
            return "asLong(" + sourceExpression + ", runtime)";
        }
        if ("double".equals(simpleType)) {
            return "asDouble(" + sourceExpression + ", runtime)";
        }
        if ("float".equals(simpleType)) {
            return "asFloat(" + sourceExpression + ", runtime)";
        }
        if ("short".equals(simpleType)) {
            return "asShort(" + sourceExpression + ", runtime)";
        }
        if ("byte".equals(simpleType)) {
            return "asByte(" + sourceExpression + ", runtime)";
        }
        if ("boolean".equals(simpleType)) {
            return "asBoolean(" + sourceExpression + ", runtime)";
        }
        if ("String".equals(simpleType)) {
            return "asString(" + sourceExpression + ", runtime)";
        }
        return "(" + parameter.getTypeName() + ") " + sourceExpression;
    }

    private String resolveJavaOutputDir(TestDataConfig config) {
        String outputDir = config.getOutputDirectory();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "./src/test/java";
        }
        if (config.getTestPackage() != null && !config.getTestPackage().isEmpty()) {
            outputDir += File.separator + config.getTestPackage().replace('.', File.separatorChar);
        }
        return outputDir;
    }

    private String resolveResourceOutputDir(TestDataConfig config) {
        String javaOutputDir = resolveJavaOutputDir(config);
        String normalized = javaOutputDir.replace('\\', '/');
        String marker = "src/test/java";
        int markerIndex = normalized.indexOf(marker);
        if (markerIndex >= 0) {
            String replaced = normalized.substring(0, markerIndex)
                    + "src/test/resources"
                    + normalized.substring(markerIndex + marker.length());
            return replaced.replace('/', File.separatorChar);
        }
        return javaOutputDir;
    }

    private String resolveConfigResourcePath(TestDataConfig config) {
        String packagePath = config.getTestPackage() == null || config.getTestPackage().isEmpty()
                ? ""
                : "/" + config.getTestPackage().replace('.', '/');
        return packagePath + "/" + getTestClassName(config) + ".json";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> sanitizeConfigForRuntime(TestDataConfig config) {
        Map<String, Object> runtimeMap = OBJECT_MAPPER.convertValue(config, Map.class);
        Object globals = runtimeMap.get("globals");
        if (globals instanceof Map) {
            ((Map<String, Object>) globals).remove("_runtimeConfigPath");
        }
        return runtimeMap;
    }
}
