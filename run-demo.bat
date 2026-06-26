@echo off
setlocal enabledelayedexpansion

set JAVA_HOME=C:\Users\slt\.jdks\openjdk-26.0.1
set EVOSUITE_HOME=%~dp0

REM Template jar
set TEMPLATE_JAR=%EVOSUITE_HOME%template\target\evosuite-template-1.2.1-SNAPSHOT.jar

REM Read classpath from Maven
set MAVEN_REPO=%USERPROFILE%\.m2\repository
set CP=%TEMPLATE_JAR%
set CP=%CP%;%EVOSUITE_HOME%client\target\evosuite-client-1.2.1-SNAPSHOT.jar
set CP=%CP%;%EVOSUITE_HOME%runtime\target\evosuite-runtime-1.2.1-SNAPSHOT.jar
set CP=%CP%;%MAVEN_REPO%\org\ow2\asm\asm\9.2\asm-9.2.jar
set CP=%CP%;%MAVEN_REPO%\org\ow2\asm\asm-commons\9.2\asm-commons-9.2.jar
set CP=%CP%;%MAVEN_REPO%\org\ow2\asm\asm-tree\9.2\asm-tree-9.2.jar
set CP=%CP%;%MAVEN_REPO%\org\ow2\asm\asm-analysis\9.2\asm-analysis-9.2.jar
set CP=%CP%;%MAVEN_REPO%\org\ow2\asm\asm-util\9.2\asm-util-9.2.jar
set CP=%CP%;%MAVEN_REPO%\com\fasterxml\jackson\core\jackson-databind\2.15.2\jackson-databind-2.15.2.jar
set CP=%CP%;%MAVEN_REPO%\com\fasterxml\jackson\core\jackson-core\2.15.2\jackson-core-2.15.2.jar
set CP=%CP%;%MAVEN_REPO%\com\fasterxml\jackson\core\jackson-annotations\2.15.2\jackson-annotations-2.15.2.jar
set CP=%CP%;%MAVEN_REPO%\com\thoughtworks\xstream\xstream\1.4.18\xstream-1.4.18.jar
set CP=%CP%;%MAVEN_REPO%\io\github\x-stream\mxparser\1.2.2\mxparser-1.2.2.jar
set CP=%CP%;%MAVEN_REPO%\xmlpull\xmlpull\1.1.3.1\xmlpull-1.1.3.1.jar
set CP=%CP%;%MAVEN_REPO%\org\apache\commons\commons-lang3\3.12.0\commons-lang3-3.12.0.jar
set CP=%CP%;%MAVEN_REPO%\commons-io\commons-io\2.11.0\commons-io-2.11.0.jar
set CP=%CP%;%MAVEN_REPO%\commons-cli\commons-cli\1.4\commons-cli-1.4.jar
set CP=%CP%;%MAVEN_REPO%\org\slf4j\slf4j-api\1.7.32\slf4j-api-1.7.32.jar
set CP=%CP%;%MAVEN_REPO%\ch\qos\logback\logback-classic\1.2.3\logback-classic-1.2.3.jar
set CP=%CP%;%MAVEN_REPO%\ch\qos\logback\logback-core\1.2.3\logback-core-1.2.3.jar
set CP=%CP%;%MAVEN_REPO%\com\googlecode\gentyref\gentyref\1.2.0\gentyref-1.2.0.jar
set CP=%CP%;%MAVEN_REPO%\net\sf\jgrapht\jgrapht\0.8.3\jgrapht-0.8.3.jar
set CP=%CP%;%MAVEN_REPO%\dk\brics\automaton\automaton\1.11-8\automaton-1.11-8.jar
set CP=%CP%;%MAVEN_REPO%\org\mockito\mockito-core\3.12.4\mockito-core-3.12.4.jar
set CP=%CP%;%MAVEN_REPO%\net\bytebuddy\byte-buddy\1.11.13\byte-buddy-1.11.13.jar
set CP=%CP%;%MAVEN_REPO%\net\bytebuddy\byte-buddy-agent\1.11.13\byte-buddy-agent-1.11.13.jar
set CP=%CP%;%MAVEN_REPO%\org\objenesis\objenesis\3.2\objenesis-3.2.jar
set CP=%CP%;%MAVEN_REPO%\org\apache\commons\commons-exec\1.3\commons-exec-1.3.jar
set CP=%CP%;%MAVEN_REPO%\org\kohsuke\graphviz-api\1.1\graphviz-api-1.1.jar
set CP=%CP%;%MAVEN_REPO%\oro\oro\2.0.8\oro-2.0.8.jar
set CP=%CP%;%MAVEN_REPO%\com\opencsv\opencsv\5.5.2\opencsv-5.5.2.jar
set CP=%CP%;%MAVEN_REPO%\org\apache\commons\commons-text\1.9\commons-text-1.9.jar
set CP=%CP%;%MAVEN_REPO%\commons-beanutils\commons-beanutils\1.9.4\commons-beanutils-1.9.4.jar
set CP=%CP%;%MAVEN_REPO%\commons-logging\commons-logging\1.2\commons-logging-1.2.jar
set CP=%CP%;%MAVEN_REPO%\commons-collections\commons-collections\3.2.2\commons-collections-3.2.2.jar
set CP=%CP%;%MAVEN_REPO%\org\apache\commons\commons-collections4\4.4\commons-collections4-4.4.jar
set CP=%CP%;%MAVEN_REPO%\javax\xml\bind\jaxb-api\2.3.0\jaxb-api-2.3.0.jar
set CP=%CP%;%MAVEN_REPO%\javax\annotation\javax.annotation-api\1.3.2\javax.annotation-api-1.3.2.jar

echo ============================================================
echo EvoSuite Template Test Generator
echo ============================================================
echo Target: com.example.Calculator
echo Config: calculator-test-config.json
echo Classpath: target/classes
echo ============================================================

"%JAVA_HOME%\bin\java.exe" --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED -cp "%CP%" org.evosuite.template.cli.TemplateCommand -t com.example.Calculator -c calculator-test-config.json -cp target/classes

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [FAIL] 生成失败，请检查错误信息
    exit /b 1
)
echo.
echo [OK] 测试生成完成！