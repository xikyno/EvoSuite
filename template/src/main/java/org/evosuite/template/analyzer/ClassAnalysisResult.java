/*
 * 分析结果容器：类名、包名、构造函数列表、方法列表
 */
package org.evosuite.template.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Complete analysis result for a target class, containing all methods,
 * constructors, and structural information needed for test generation.
 */
public class ClassAnalysisResult {

    private final String className;
    private final String packageName;
    private final String simpleName;
    private final boolean interfaceType;
    private final boolean abstractType;
    private final List<MethodInfo> constructors;
    private final List<MethodInfo> methods;
    private final List<String> superTypes;
    private final List<String> fieldNames;
    private final List<String> fieldTypes;
    private final List<String> annotationNames;
    private final List<String> fieldAnnotationNames;
    private final Set<String> imports;

    public ClassAnalysisResult(String className, String packageName, String simpleName,
                               boolean interfaceType, boolean abstractType,
                               List<MethodInfo> constructors, List<MethodInfo> methods,
                               List<String> superTypes, List<String> fieldNames,
                               List<String> fieldTypes, List<String> annotationNames,
                               List<String> fieldAnnotationNames, Set<String> imports) {
        this.className = className;
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.interfaceType = interfaceType;
        this.abstractType = abstractType;
        this.constructors = Collections.unmodifiableList(new ArrayList<>(constructors));
        this.methods = Collections.unmodifiableList(new ArrayList<>(methods));
        this.superTypes = Collections.unmodifiableList(new ArrayList<>(superTypes));
        this.fieldNames = Collections.unmodifiableList(new ArrayList<>(fieldNames));
        this.fieldTypes = Collections.unmodifiableList(new ArrayList<>(fieldTypes));
        this.annotationNames = Collections.unmodifiableList(new ArrayList<>(annotationNames));
        this.fieldAnnotationNames = Collections.unmodifiableList(new ArrayList<>(fieldAnnotationNames));
        this.imports = Collections.unmodifiableSet(new HashSet<>(imports));
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public boolean isInterface() {
        return interfaceType;
    }

    public boolean isAbstract() {
        return abstractType;
    }

    public List<MethodInfo> getConstructors() {
        return constructors;
    }

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public List<String> getSuperTypes() {
        return superTypes;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public List<String> getFieldTypes() {
        return fieldTypes;
    }

    public List<String> getAnnotationNames() {
        return annotationNames;
    }

    public List<String> getFieldAnnotationNames() {
        return fieldAnnotationNames;
    }

    public Set<String> getImports() {
        return imports;
    }

    /**
     * Find a method by name. Returns the first match, or null if not found.
     */
    public MethodInfo findMethod(String name) {
        for (MethodInfo m : methods) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Get the total count of all methods (including constructors).
     */
    public int getTotalMethodCount() {
        return constructors.size() + methods.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ").append(className).append("\n");
        if (interfaceType) sb.append("  [Interface]\n");
        if (abstractType) sb.append("  [Abstract]\n");
        sb.append("  Constructors (").append(constructors.size()).append("):\n");
        for (MethodInfo c : constructors) {
            sb.append("    ").append(c.getSignature()).append("\n");
        }
        sb.append("  Methods (").append(methods.size()).append("):\n");
        for (MethodInfo m : methods) {
            sb.append("    ").append(m.getSignature()).append("\n");
        }
        return sb.toString();
    }
}
