@echo off
setlocal

set "EVOSUITE_HOME=%~dp0"

if "%JAVA_HOME%"=="" (
  set "JAVA_HOME=C:\Users\slt\.jdks\jdk-17.0.19+10"
)

set "CONFIG_FILE=%~1"
set "TARGET_CLASS=%~2"
set "PROJECT_CP=%~3"

if "%CONFIG_FILE%"=="" (
  echo Usage: run.bat config.json fully.qualified.TargetClass path\to\target\classes
  exit /b 1
)

if "%TARGET_CLASS%"=="" (
  echo Missing target class.
  exit /b 1
)

if "%PROJECT_CP%"=="" (
  echo Missing project classpath.
  exit /b 1
)

set "MAVEN_REPO=%USERPROFILE%\.m2\repository"
set "CP=%EVOSUITE_HOME%template\target\evosuite-template-1.2.1-SNAPSHOT.jar"
set "CP=%CP%;%EVOSUITE_HOME%client\target\evosuite-client-1.2.1-SNAPSHOT.jar"
set "CP=%CP%;%EVOSUITE_HOME%runtime\target\evosuite-runtime-1.2.1-SNAPSHOT.jar"
set "CP=%CP%;%MAVEN_REPO%\org\ow2\asm\asm\9.7\asm-9.7.jar"
set "CP=%CP%;%MAVEN_REPO%\org\ow2\asm\asm-commons\9.7\asm-commons-9.7.jar"
set "CP=%CP%;%MAVEN_REPO%\org\ow2\asm\asm-tree\9.7\asm-tree-9.7.jar"
set "CP=%CP%;%MAVEN_REPO%\org\ow2\asm\asm-analysis\9.7\asm-analysis-9.7.jar"
set "CP=%CP%;%MAVEN_REPO%\org\ow2\asm\asm-util\9.7\asm-util-9.7.jar"
set "CP=%CP%;%MAVEN_REPO%\com\fasterxml\jackson\core\jackson-databind\2.15.2\jackson-databind-2.15.2.jar"
set "CP=%CP%;%MAVEN_REPO%\com\fasterxml\jackson\core\jackson-core\2.15.2\jackson-core-2.15.2.jar"
set "CP=%CP%;%MAVEN_REPO%\com\fasterxml\jackson\core\jackson-annotations\2.15.2\jackson-annotations-2.15.2.jar"
set "CP=%CP%;%MAVEN_REPO%\com\thoughtworks\xstream\xstream\1.4.18\xstream-1.4.18.jar"
set "CP=%CP%;%MAVEN_REPO%\io\github\x-stream\mxparser\1.2.2\mxparser-1.2.2.jar"
set "CP=%CP%;%MAVEN_REPO%\xmlpull\xmlpull\1.1.3.1\xmlpull-1.1.3.1.jar"
set "CP=%CP%;%MAVEN_REPO%\org\apache\commons\commons-lang3\3.12.0\commons-lang3-3.12.0.jar"
set "CP=%CP%;%MAVEN_REPO%\commons-io\commons-io\2.11.0\commons-io-2.11.0.jar"
set "CP=%CP%;%MAVEN_REPO%\commons-cli\commons-cli\1.4\commons-cli-1.4.jar"
set "CP=%CP%;%MAVEN_REPO%\org\slf4j\slf4j-api\1.7.32\slf4j-api-1.7.32.jar"
set "CP=%CP%;%MAVEN_REPO%\ch\qos\logback\logback-classic\1.2.3\logback-classic-1.2.3.jar"
set "CP=%CP%;%MAVEN_REPO%\ch\qos\logback\logback-core\1.2.3\logback-core-1.2.3.jar"
set "CP=%CP%;%MAVEN_REPO%\com\googlecode\gentyref\gentyref\1.2.0\gentyref-1.2.0.jar"
set "CP=%CP%;%MAVEN_REPO%\net\sf\jgrapht\jgrapht\0.8.3\jgrapht-0.8.3.jar"
set "CP=%CP%;%MAVEN_REPO%\dk\brics\automaton\automaton\1.11-8\automaton-1.11-8.jar"
set "CP=%CP%;%MAVEN_REPO%\org\mockito\mockito-core\3.12.4\mockito-core-3.12.4.jar"
set "CP=%CP%;%MAVEN_REPO%\net\bytebuddy\byte-buddy\1.11.13\byte-buddy-1.11.13.jar"
set "CP=%CP%;%MAVEN_REPO%\net\bytebuddy\byte-buddy-agent\1.11.13\byte-buddy-agent-1.11.13.jar"
set "CP=%CP%;%MAVEN_REPO%\org\objenesis\objenesis\3.2\objenesis-3.2.jar"
set "CP=%CP%;%MAVEN_REPO%\junit\junit\4.13.2\junit-4.13.2.jar"
set "CP=%CP%;%MAVEN_REPO%\org\hamcrest\hamcrest-core\1.3\hamcrest-core-1.3.jar"

"%JAVA_HOME%\bin\java.exe" ^
  --add-opens=java.base/java.lang=ALL-UNNAMED ^
  --add-opens=java.base/java.lang.reflect=ALL-UNNAMED ^
  --add-opens=java.base/java.util=ALL-UNNAMED ^
  --add-opens=java.base/java.net=ALL-UNNAMED ^
  --add-opens=java.base/java.io=ALL-UNNAMED ^
  -cp "%CP%" ^
  org.evosuite.template.cli.TemplateCommand ^
  -t "%TARGET_CLASS%" ^
  -c "%CONFIG_FILE%" ^
  -cp "%PROJECT_CP%"

endlocal
