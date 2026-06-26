package ${testPackage};

${foreach import in imports}
import ${import};
${end}

${if useSpringBoot}
@SpringBootTest
${end}
public class ${testClassName}${if hasBaseTestClass} extends ${baseTestSimpleName}${end} {

    private static final String RUNTIME_CONFIG_PATH = ${runtimeConfigPath};
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

${if useSpringBoot}
    @Autowired
${else}
    @InjectMocks
${end}
    private ${targetSimpleName} ${targetVariable};

${foreach mock in globalMocks}
    @${mockAnnotation}
    private ${mock.mockSimpleName} ${mock.mockVariable};

${end}
${if needsMockitoLifecycle}
    private AutoCloseable closeable;

    @BeforeEach
    public void beforeAll() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterAll() throws Exception {
        closeable.close();
    }

${end}
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadConfig() throws Exception {
        try (java.io.InputStream inputStream = ${testClassName}.class.getResourceAsStream(RUNTIME_CONFIG_PATH)) {
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

${foreach testCase in testCases}
    @Test
    public void ${testCase.testName}() throws Exception {
        for (Map<String, Object> variant : loadVariants("${testCase.methodName}")) {
            Map<String, Object> runtime = new java.util.HashMap<>();
            List<Map<String, Object>> calls = methodCallsOf(variant);
${if testCase.constructorSetupCode}
${testCase.constructorSetupCode}
${end}
${if !testCase.hasConstructor}
            if (calls.isEmpty()) {
                continue;
            }
${end}
${testCase.parameterLoadCode}
            String expectedException = expectedExceptionOf(variant);
            if (expectedException != null && !expectedException.isEmpty()) {
                if ("java.lang.IllegalArgumentException".equals(expectedException)) {
                    Assertions.assertThrows(IllegalArgumentException.class, () -> {
                        ${testCase.exceptionInvocationCode}
                    });
                    continue;
                }
                throw new IllegalArgumentException("Unsupported expectedException: " + expectedException);
            }
${testCase.normalInvocationCode}
${if testCase.hasReturnValue}
            for (Map.Entry<String, Object> entry : assertEqualsOf(assertionsOf(variant)).entrySet()) {
                String expected = resolveRef(entry.getKey(), runtime);
                String actual = resolveRef(entry.getValue(), runtime);
                Assertions.assertEquals(expected, actual);
            }
${end}
        }
    }

${end}
}
