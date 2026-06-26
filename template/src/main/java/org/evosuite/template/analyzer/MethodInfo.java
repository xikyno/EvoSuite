/*
 * 单个方法/构造函数的描述：名称、参数列表、返回类型、异常
 */
package org.evosuite.template.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single method or constructor with its parameter information.
 */
public class MethodInfo {

    private final String name;
    private final String declaringClass;
    private final String returnTypeName;
    private final String simpleReturnTypeName;
    private final boolean voidReturn;
    private final boolean staticMethod;
    private final boolean publicMethod;
    private final boolean constructor;
    private final List<ParameterInfo> parameters;
    private final List<String> thrownExceptions;
    private final int modifiers;

    public MethodInfo(String name, String declaringClass, String returnTypeName,
                      String simpleReturnTypeName, boolean voidReturn, boolean staticMethod,
                      boolean publicMethod, boolean constructor,
                      List<ParameterInfo> parameters, List<String> thrownExceptions,
                      int modifiers) {
        this.name = name;
        this.declaringClass = declaringClass;
        this.returnTypeName = returnTypeName;
        this.simpleReturnTypeName = simpleReturnTypeName;
        this.voidReturn = voidReturn;
        this.staticMethod = staticMethod;
        this.publicMethod = publicMethod;
        this.constructor = constructor;
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        this.thrownExceptions = Collections.unmodifiableList(new ArrayList<>(thrownExceptions));
        this.modifiers = modifiers;
    }

    public String getName() {
        return name;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public String getReturnTypeName() {
        return returnTypeName;
    }

    public String getSimpleReturnTypeName() {
        return simpleReturnTypeName;
    }

    public boolean isVoid() {
        return voidReturn;
    }

    public boolean isStatic() {
        return staticMethod;
    }

    public boolean isPublic() {
        return publicMethod;
    }

    public boolean isConstructor() {
        return constructor;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public List<String> getThrownExceptions() {
        return thrownExceptions;
    }

    public int getModifiers() {
        return modifiers;
    }

    /**
     * Returns the method signature as a human-readable string.
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        if (publicMethod) sb.append("public ");
        if (staticMethod) sb.append("static ");
        sb.append(returnTypeName).append(" ").append(name).append("(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameters.get(i).toString());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getSignature();
    }
}