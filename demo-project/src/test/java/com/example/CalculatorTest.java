package com.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class CalculatorTest {
    private static final String RUNTIME_CONFIG_PATH = "/com/example/CalculatorTest.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @InjectMocks
    private Calculator target;

    private AutoCloseable closeable;

    @BeforeEach
    public void beforeAll() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterAll() throws Exception {
        closeable.close();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadConfig() throws Exception {
        try (java.io.InputStream inputStream = CalculatorTest.class.getResourceAsStream(RUNTIME_CONFIG_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Runtime config not found: " + RUNTIME_CONFIG_PATH);
            }
            return OBJECT_MAPPER.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadVariants(String methodName) throws Exception {
        Map<String, Object> root = loadConfig();
        Object rawCases = root.get("testCases");
        if (!(rawCases instanceof List)) {
            return Collections.emptyList();
        }
        for (Object item : (List<Object>) rawCases) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map<String, Object> testCase = (Map<String, Object>) item;
            if (methodName.equals(String.valueOf(testCase.get("methodName")))) {
                Object rawVariants = testCase.get("variants");
                if (rawVariants instanceof List) {
                    return (List<Map<String, Object>>) rawVariants;
                }
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Object> parametersOf(Map<String, Object> call) {
        Object value = call.get("parameters");
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> methodCallsOf(Map<String, Object> variant) {
        Object value = variant.get("methodCalls");
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        return Collections.emptyList();
    }

    private String expectedExceptionOf(Map<String, Object> variant) {
        Object value = variant.get("expectedException");
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> assertionsOf(Map<String, Object> variant) {
        Object value = variant.get("assertions");
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> assertEqualsOf(Map<String, Object> assertions) {
        Object value = assertions.get("assertEquals");
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    private String resolveRef(Object value, Map<String, Object> runtime) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        if (text.startsWith("$ref:")) {
            Object resolved = runtime.get(text.substring(5));
            return resolved == null ? null : String.valueOf(resolved);
        }
        return text;
    }

    private int asInt(Object value, Map<String, Object> runtime) {
        return Integer.parseInt(resolveRef(value, runtime));
    }

    private long asLong(Object value, Map<String, Object> runtime) {
        return Long.parseLong(resolveRef(value, runtime));
    }

    private double asDouble(Object value, Map<String, Object> runtime) {
        return Double.parseDouble(resolveRef(value, runtime));
    }

    private float asFloat(Object value, Map<String, Object> runtime) {
        return Float.parseFloat(resolveRef(value, runtime));
    }

    private short asShort(Object value, Map<String, Object> runtime) {
        return Short.parseShort(resolveRef(value, runtime));
    }

    private byte asByte(Object value, Map<String, Object> runtime) {
        return Byte.parseByte(resolveRef(value, runtime));
    }

    private boolean asBoolean(Object value, Map<String, Object> runtime) {
        return Boolean.parseBoolean(resolveRef(value, runtime));
    }

    private String asString(Object value, Map<String, Object> runtime) {
        return resolveRef(value, runtime);
    }

    @Test
    public void testAdd() throws Exception {
        for (Map<String, Object> variant : loadVariants("add")) {
            Map<String, Object> runtime = new java.util.HashMap<>();
            List<Map<String, Object>> calls = methodCallsOf(variant);

            if (calls.isEmpty()) {
                continue;
            }
            Map<String, Object> constructorCall = calls.get(0);
            List<Object> constructorParams = parametersOf(constructorCall);
            final Calculator target = new Calculator(asString(constructorParams.get(0), runtime));
            runtime.put("target", target);

            Map<String, Object> methodCall = calls.get(calls.size() - 1);
            List<Object> params = parametersOf(methodCall);
            int arg0 = asInt(params.get(0), runtime);
            int arg1 = asInt(params.get(1), runtime);

            String expectedException = expectedExceptionOf(variant);
            if (expectedException != null && !expectedException.isEmpty()) {
                if ("java.lang.IllegalArgumentException".equals(expectedException)) {
                    Assertions.assertThrows(IllegalArgumentException.class, () -> {
                        target.add(arg0, arg1);
                    });
                    continue;
                }
                throw new IllegalArgumentException("Unsupported expectedException: " + expectedException);
            }
int result = target.add(arg0, arg1);
            runtime.put("result", result);

            for (Map.Entry<String, Object> entry : assertEqualsOf(assertionsOf(variant)).entrySet()) {
                String expected = resolveRef(entry.getKey(), runtime);
                String actual = resolveRef(entry.getValue(), runtime);
                Assertions.assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testDivide() throws Exception {
        for (Map<String, Object> variant : loadVariants("divide")) {
            Map<String, Object> runtime = new java.util.HashMap<>();
            List<Map<String, Object>> calls = methodCallsOf(variant);

            if (calls.isEmpty()) {
                continue;
            }
            Map<String, Object> constructorCall = calls.get(0);
            List<Object> constructorParams = parametersOf(constructorCall);
            final Calculator target = new Calculator(asString(constructorParams.get(0), runtime));
            runtime.put("target", target);

            Map<String, Object> methodCall = calls.get(calls.size() - 1);
            List<Object> params = parametersOf(methodCall);
            int arg0 = asInt(params.get(0), runtime);
            int arg1 = asInt(params.get(1), runtime);

            String expectedException = expectedExceptionOf(variant);
            if (expectedException != null && !expectedException.isEmpty()) {
                if ("java.lang.IllegalArgumentException".equals(expectedException)) {
                    Assertions.assertThrows(IllegalArgumentException.class, () -> {
                        target.divide(arg0, arg1);
                    });
                    continue;
                }
                throw new IllegalArgumentException("Unsupported expectedException: " + expectedException);
            }
int result = target.divide(arg0, arg1);
            runtime.put("result", result);

            for (Map.Entry<String, Object> entry : assertEqualsOf(assertionsOf(variant)).entrySet()) {
                String expected = resolveRef(entry.getKey(), runtime);
                String actual = resolveRef(entry.getValue(), runtime);
                Assertions.assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testGetName() throws Exception {
        for (Map<String, Object> variant : loadVariants("getName")) {
            Map<String, Object> runtime = new java.util.HashMap<>();
            List<Map<String, Object>> calls = methodCallsOf(variant);

            if (calls.isEmpty()) {
                continue;
            }
            Map<String, Object> constructorCall = calls.get(0);
            List<Object> constructorParams = parametersOf(constructorCall);
            final Calculator target = new Calculator(asString(constructorParams.get(0), runtime));
            runtime.put("target", target);

            Map<String, Object> methodCall = calls.get(calls.size() - 1);
            List<Object> params = parametersOf(methodCall);

            String expectedException = expectedExceptionOf(variant);
            if (expectedException != null && !expectedException.isEmpty()) {
                if ("java.lang.IllegalArgumentException".equals(expectedException)) {
                    Assertions.assertThrows(IllegalArgumentException.class, () -> {
                        target.getName();
                    });
                    continue;
                }
                throw new IllegalArgumentException("Unsupported expectedException: " + expectedException);
            }
String result = target.getName();
            runtime.put("result", result);

            for (Map.Entry<String, Object> entry : assertEqualsOf(assertionsOf(variant)).entrySet()) {
                String expected = resolveRef(entry.getKey(), runtime);
                String actual = resolveRef(entry.getValue(), runtime);
                Assertions.assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testGreet() throws Exception {
        for (Map<String, Object> variant : loadVariants("greet")) {
            Map<String, Object> runtime = new java.util.HashMap<>();
            List<Map<String, Object>> calls = methodCallsOf(variant);

            if (calls.isEmpty()) {
                continue;
            }
            Map<String, Object> constructorCall = calls.get(0);
            List<Object> constructorParams = parametersOf(constructorCall);
            final Calculator target = new Calculator(asString(constructorParams.get(0), runtime));
            runtime.put("target", target);

            Map<String, Object> methodCall = calls.get(calls.size() - 1);
            List<Object> params = parametersOf(methodCall);
            String arg0 = asString(params.get(0), runtime);

            String expectedException = expectedExceptionOf(variant);
            if (expectedException != null && !expectedException.isEmpty()) {
                if ("java.lang.IllegalArgumentException".equals(expectedException)) {
                    Assertions.assertThrows(IllegalArgumentException.class, () -> {
                        target.greet(arg0);
                    });
                    continue;
                }
                throw new IllegalArgumentException("Unsupported expectedException: " + expectedException);
            }
String result = target.greet(arg0);
            runtime.put("result", result);

            for (Map.Entry<String, Object> entry : assertEqualsOf(assertionsOf(variant)).entrySet()) {
                String expected = resolveRef(entry.getKey(), runtime);
                String actual = resolveRef(entry.getValue(), runtime);
                Assertions.assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testMultiply() throws Exception {
        for (Map<String, Object> variant : loadVariants("multiply")) {
            Map<String, Object> runtime = new java.util.HashMap<>();
            List<Map<String, Object>> calls = methodCallsOf(variant);

            if (calls.isEmpty()) {
                continue;
            }

            Map<String, Object> methodCall = calls.get(calls.size() - 1);
            List<Object> params = parametersOf(methodCall);
            int arg0 = asInt(params.get(0), runtime);
            int arg1 = asInt(params.get(1), runtime);

            String expectedException = expectedExceptionOf(variant);
            if (expectedException != null && !expectedException.isEmpty()) {
                if ("java.lang.IllegalArgumentException".equals(expectedException)) {
                    Assertions.assertThrows(IllegalArgumentException.class, () -> {
                        Calculator.multiply(arg0, arg1);
                    });
                    continue;
                }
                throw new IllegalArgumentException("Unsupported expectedException: " + expectedException);
            }
int result = Calculator.multiply(arg0, arg1);
            runtime.put("result", result);

            for (Map.Entry<String, Object> entry : assertEqualsOf(assertionsOf(variant)).entrySet()) {
                String expected = resolveRef(entry.getKey(), runtime);
                String actual = resolveRef(entry.getValue(), runtime);
                Assertions.assertEquals(expected, actual);
            }
        }
    }
}
