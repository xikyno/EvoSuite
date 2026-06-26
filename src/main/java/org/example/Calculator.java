package org.example;

/**
 * 一个简单的示例类，用于演示模板测试生成
 */
public class Calculator {
    private final String name;

    public Calculator(String name) {
        this.name = name;
    }

    public int add(int a, int b) {
        return a + b;
    }

    public int divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为零");
        }
        return a / b;
    }

    public String getName() {
        return name;
    }

    public String greet(String user) {
        if (user == null || user.isEmpty()) {
            return "Hello, 陌生人!";
        }
        return "Hello, " + user + "! 我是 " + name;
    }

    public static int multiply(int a, int b) {
        return a * b;
    }
}