/*
 * 单个参数：类型、是否基本类型、是否数组
 */
package org.evosuite.template.analyzer;

/**
 * Represents a single method parameter with its type information.
 */
public class ParameterInfo {

    private final String name;
    private final String typeName;
    private final String simpleTypeName;
    private final boolean primitive;
    private final boolean array;
    private final String genericType;
    private final boolean interfaceType;
    private final boolean abstractType;

    public ParameterInfo(String name, String typeName, String simpleTypeName,
                         boolean primitive, boolean array, String genericType,
                         boolean interfaceType, boolean abstractType) {
        this.name = name;
        this.typeName = typeName;
        this.simpleTypeName = simpleTypeName;
        this.primitive = primitive;
        this.array = array;
        this.genericType = genericType;
        this.interfaceType = interfaceType;
        this.abstractType = abstractType;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getSimpleTypeName() {
        return simpleTypeName;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public boolean isArray() {
        return array;
    }

    public String getGenericType() {
        return genericType;
    }

    public boolean isInterface() {
        return interfaceType;
    }

    public boolean isAbstract() {
        return abstractType;
    }

    /**
     * Returns a default value literal for this parameter type.
     * Used when no explicit value is provided in the test data config.
     */
    public String getDefaultValue() {
        if (primitive) {
            switch (typeName) {
                case "boolean": return "false";
                case "byte":    return "(byte) 0";
                case "short":   return "(short) 0";
                case "int":     return "0";
                case "long":    return "0L";
                case "float":   return "0.0f";
                case "double":  return "0.0";
                case "char":    return "'\\u0000'";
                default:        return "null";
            }
        }
        return "null";
    }

    @Override
    public String toString() {
        return typeName + " " + name;
    }
}