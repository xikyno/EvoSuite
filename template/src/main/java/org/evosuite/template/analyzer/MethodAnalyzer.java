/*
 * 调用evosuited DependencyAnalysis,analyzeClass()扫描目标类
 */
package org.evosuite.template.analyzer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyzes a Java class using EvoSuite's TestCluster infrastructure.
 * Extracts method signatures, parameter types, and structural information
 * needed for template-based test generation.
 */
public class MethodAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(MethodAnalyzer.class);

    /**
     * Analyze a target class using EvoSuite's existing class analysis infrastructure.
     * This reuses TestClusterGenerator but discards the GA-related metadata.
     *
     * @param targetClassName fully qualified class name (e.g., "com.example.UserService")
     * @param classPath       list of classpath entries for the target project
     * @return ClassAnalysisResult containing all methods, constructors, and parameters
     * @throws RuntimeException if the class cannot be analyzed
     */
    public ClassAnalysisResult analyze(String targetClassName, List<String> classPath)
            throws RuntimeException {
        logger.info("Analyzing class: {}", targetClassName);

        // Reset TestCluster for a clean analysis
        TestCluster.reset();

        // Configure EvoSuite properties for analysis
        Properties.TARGET_CLASS = targetClassName;
        if (classPath != null && !classPath.isEmpty()) {
            ClassPathHandler.getInstance().changeTargetClassPath(classPath.toArray(new String[0]));
        }

        try {
            // Run EvoSuite's standard class analysis pipeline
            // This populates the TestCluster singleton with all methods/constructors/fields
            DependencyAnalysis.analyzeClass(targetClassName, classPath);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found: " + targetClassName, e);
        }

        // Extract analysis results from TestCluster
        return extractClassInfo(targetClassName);
    }

    /**
     * Analyze a target class using the default classpath (from system property java.class.path).
     */
    public ClassAnalysisResult analyze(String targetClassName) throws RuntimeException {
        List<String> classPath = new ArrayList<>();
        classPath.add(Properties.TARGET_CLASS);
        return analyze(targetClassName, classPath);
    }

    /**
     * Extract class information from the populated TestCluster singleton.
     */
    private ClassAnalysisResult extractClassInfo(String targetClassName) {
        TestCluster cluster = TestCluster.getInstance();
        List<GenericAccessibleObject<?>> testCalls = cluster.getTestCalls();

        List<MethodInfo> constructors = new ArrayList<>();
        List<MethodInfo> methods = new ArrayList<>();
        Set<String> imports = new HashSet<>();

        for (GenericAccessibleObject<?> call : testCalls) {
            if (call.isMethod()) {
                GenericMethod method = (GenericMethod) call;
                MethodInfo methodInfo = convertMethod(method);
                if (methodInfo.isConstructor()) {
                    constructors.add(methodInfo);
                } else {
                    methods.add(methodInfo);
                }
                // Collect imports for parameter and return types
                collectImports(methodInfo, imports);
            } else if (call.isConstructor()) {
                GenericConstructor ctor = (GenericConstructor) call;
                MethodInfo ctorInfo = convertConstructor(ctor);
                constructors.add(ctorInfo);
                collectImports(ctorInfo, imports);
            }
        }

        // Determine class metadata
        Class<?> targetClass = null;
        try {
            targetClass = cluster.getClass(targetClassName);
        } catch (ClassNotFoundException e) {
            logger.warn("Could not load class {}: {}", targetClassName, e.getMessage());
        }
        String packageName = "";
        String simpleName = targetClassName;
        boolean isInterface = false;
        boolean isAbstract = false;
        List<String> superTypes = new ArrayList<>();
        List<String> fieldNames = new ArrayList<>();
        List<String> fieldTypes = new ArrayList<>();
        List<String> annotationNames = new ArrayList<>();
        List<String> fieldAnnotationNames = new ArrayList<>();

        if (targetClass != null) {
            packageName = targetClass.getPackage() != null ? targetClass.getPackage().getName() : "";
            simpleName = targetClass.getSimpleName();
            isInterface = targetClass.isInterface();
            isAbstract = Modifier.isAbstract(targetClass.getModifiers()) && !isInterface;

            // Collect super types
            if (targetClass.getSuperclass() != null && targetClass.getSuperclass() != Object.class) {
                superTypes.add(targetClass.getSuperclass().getName());
            }
            for (Class<?> iface : targetClass.getInterfaces()) {
                superTypes.add(iface.getName());
            }

            for (Annotation annotation : targetClass.getAnnotations()) {
                annotationNames.add(annotation.annotationType().getName());
            }

            // Collect field names
            for (Field field : targetClass.getDeclaredFields()) {
                fieldNames.add(field.getName());
                fieldTypes.add(field.getType().getName());
                addImportIfNeeded(field.getType().getName(), imports);
                for (Annotation annotation : field.getAnnotations()) {
                    fieldAnnotationNames.add(annotation.annotationType().getName());
                }
            }

            // Don't import the target class itself
            imports.remove(targetClassName);
        }

        logger.info("Analysis complete: {} constructors, {} methods found",
                constructors.size(), methods.size());

        return new ClassAnalysisResult(targetClassName, packageName, simpleName,
                isInterface, isAbstract, constructors, methods,
                superTypes, fieldNames, fieldTypes, annotationNames,
                fieldAnnotationNames, imports);
    }

    /**
     * Convert a GenericMethod to MethodInfo DTO.
     */
    private MethodInfo convertMethod(GenericMethod method) {
        String name = method.getName();
        String declaringClass = method.getDeclaringClass().getName();
        Type returnType = method.getReturnType();
        Class<?> rawReturnType = method.getRawGeneratedType();
        boolean isVoid = (rawReturnType == void.class || rawReturnType == Void.class);
        boolean isStatic = method.isStatic();
        boolean isPublic = method.isPublic();

        List<ParameterInfo> parameters = extractParameters(method.getParameterTypes(),
                method.getGenericParameterTypes());
        List<String> exceptions = extractExceptions(method);

        return new MethodInfo(name, declaringClass,
                returnType.getTypeName(), rawReturnType.getSimpleName(),
                isVoid, isStatic, isPublic, false,
                parameters, exceptions, method.getMethod().getModifiers());
    }

    /**
     * Convert a GenericConstructor to MethodInfo DTO.
     */
    private MethodInfo convertConstructor(GenericConstructor ctor) {
        String name = ctor.getName();
        // Extract simple class name from constructor name
        String simpleName = name.substring(name.lastIndexOf('.') + 1);
        String declaringClass = ctor.getDeclaringClass().getName();
        String returnTypeName = declaringClass;
        boolean isPublic = ctor.isPublic();

        List<ParameterInfo> parameters = extractParameters(ctor.getParameterTypes(),
                ctor.getGenericParameterTypes());
        List<String> exceptions = extractExceptions(ctor);

        return new MethodInfo(simpleName, declaringClass,
                returnTypeName, simpleName,
                false, false, isPublic, true,
                parameters, exceptions, ctor.getConstructor().getModifiers());
    }

    /**
     * Extract parameter information from resolved and generic type arrays.
     */
    private List<ParameterInfo> extractParameters(Type[] resolvedTypes, Type[] genericTypes) {
        List<ParameterInfo> params = new ArrayList<>();
        for (int i = 0; i < resolvedTypes.length; i++) {
            Type resolvedType = resolvedTypes[i];
            Type genericType = (i < genericTypes.length) ? genericTypes[i] : resolvedType;

            GenericClass<?> gc = GenericClassFactory.get(resolvedType);

            String typeName = resolvedType.getTypeName();
            String simpleName = gc.getSimpleName();
            boolean isPrimitive = gc.isPrimitive();
            boolean isArray = gc.isArray();
            String genericStr = (!resolvedType.equals(genericType)) ? genericType.getTypeName() : null;
            boolean isInterface = gc.getRawClass() != null && gc.getRawClass().isInterface();
            boolean isAbstract = gc.isAbstract();

            params.add(new ParameterInfo("arg" + i, typeName, simpleName,
                    isPrimitive, isArray, genericStr, isInterface, isAbstract));
        }
        return params;
    }

    /**
     * Extract exception types from a method or constructor.
     */
    private List<String> extractExceptions(GenericAccessibleObject<?> accessible) {
        List<String> exceptions = new ArrayList<>();
        if (accessible.isMethod()) {
            GenericMethod method = (GenericMethod) accessible;
            for (Class<?> ex : method.getMethod().getExceptionTypes()) {
                exceptions.add(ex.getName());
            }
        } else if (accessible.isConstructor()) {
            GenericConstructor ctor = (GenericConstructor) accessible;
            for (Class<?> ex : ctor.getConstructor().getExceptionTypes()) {
                exceptions.add(ex.getName());
            }
        }
        return exceptions;
    }

    /**
     * Collect non-java.lang, non-primitive types that need import statements.
     */
    private void collectImports(MethodInfo methodInfo, Set<String> imports) {
        // Return type
        if (!methodInfo.isVoid() && !methodInfo.isConstructor()) {
            addImportIfNeeded(methodInfo.getReturnTypeName(), imports);
        }
        // Parameter types
        for (ParameterInfo param : methodInfo.getParameters()) {
            addImportIfNeeded(param.getTypeName(), imports);
        }
        // Exception types
        for (String ex : methodInfo.getThrownExceptions()) {
            addImportIfNeeded(ex, imports);
        }
    }

    /**
     * Add a type to the import set if it's not a primitive or java.lang type.
     */
    private void addImportIfNeeded(String typeName, Set<String> imports) {
        if (typeName == null || typeName.isEmpty() || typeName.equals("void")) {
            return;
        }
        // Skip primitives
        if (Arrays.asList("boolean", "byte", "short", "int", "long", "float", "double", "char")
                .contains(typeName)) {
            return;
        }
        // Skip java.lang types
        if (typeName.startsWith("java.lang.") && typeName.lastIndexOf('.') <= 9) {
            return;
        }
        // Skip array brackets
        String baseType = typeName.replaceAll("\\[\\]", "").replaceAll("<.*>", "");
        imports.add(baseType);
    }
}
