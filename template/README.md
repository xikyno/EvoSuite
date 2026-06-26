# EvoSuite Template Test Generator

基于模板的 JUnit 测试自动生成工具，作为 EvoSuite 的扩展模块。

## 特性

- **类分析**：自动分析 Java 类的所有方法、参数、返回类型
- **模板化生成**：使用自定义模板引擎生成 JUnit4 测试代码
- **配置驱动**：测试数据通过 JSON 或 XML 文件配置
- **离线运行**：零外部依赖，无需网络连接
- **无 LLM 依赖**：纯确定性生成，不依赖任何大模型

## 快速开始

### 1. 准备配置文件

创建 `test-config.json`：

```json
{
  "targetClass": "com.example.MyService",
  "testPackage": "com.example.test",
  "outputDirectory": "./generated-tests",
  "testCases": [
    {
      "testName": "testMyMethod",
      "description": "测试 myMethod 方法",
      "methodCalls": [
        { "constructorFor": "com.example.MyService", "resultVariable": "service" },
        { "methodName": "myMethod", "targetVariable": "service",
          "resultVariable": "result", "parameters": ["test", 42] }
      ],
      "assertions": {
        "assertNotNull": "$ref:result"
      }
    }
  ]
}
```

### 2. 运行生成

```bash
# 通过 EvoSuite 主入口
java -jar evosuite-master.jar -generateTemplate test-config.json -class com.example.MyService -projectCP target/classes

# 或通过独立 CLI
java -cp evosuite-template.jar:... org.evosuite.template.cli.TemplateCommand -t com.example.MyService -c test-config.json -cp target/classes
```

### 3. 查看生成的测试

生成的测试文件位于 `generated-tests/com/example/test/MyServiceTest.java`。

## 配置文件格式

### 支持的操作

| 操作 | 说明 |
|------|------|
| `constructorFor` | 调用构造函数创建对象 |
| `methodName` + `targetVariable` | 调用实例方法 |
| `methodName` (无 `targetVariable`) | 调用静态方法 |
| `resultVariable` | 存储方法返回值 |
| `expectedException` | 期望抛出的异常 |

### 断言类型

| 断言 | 说明 |
|------|------|
| `assertNotNull` | 验证不为 null |
| `assertNull` | 验证为 null |
| `assertTrue` / `assertFalse` | 布尔验证 |
| `assertEquals` | 值相等验证 |
| `assertSame` | 引用同一性验证 |

### 特殊值引用

- `$ref:variableName` — 引用之前的结果变量
- `$ref:null` — 显式 null 值

## 自定义模板

可以通过 `--template` 参数指定自定义模板文件。模板语法：

```
${variable}              — 变量替换
${obj.property}          — 属性访问
${foreach x in list}...${end}  — 迭代
${if condition}...${else}...${end}  — 条件
${! include template}    — 引入其他模板
```

## 架构

```
template/
├── analyzer/  — 类分析（复用 EvoSuite TestClusterGenerator）
├── config/    — JSON/XML 配置加载
├── engine/    — 自定义模板引擎
├── writer/    — JUnit 代码生成
└── cli/       — 命令行接口
```

## 许可证

LGPL-3.0（与 EvoSuite 一致）