#!/bin/bash
# ============================================================
# EvoSuite Template Test Generator - 启动脚本 (Linux/Mac/Git Bash)
# 用法: ./run.sh [配置文件] [目标类名] [classpath]
# ============================================================

EVOSUITE_HOME="$(cd "$(dirname "$0")" && pwd)"

CONFIG_FILE="${1:-test-config.json}"
TARGET_CLASS="${2:-com.example.MyService}"
PROJECT_CP="${3:-.}"

MASTER_JAR="$EVOSUITE_HOME/master/target/evosuite-master-1.2.1-SNAPSHOT.jar"
TEMPLATE_JAR="$EVOSUITE_HOME/template/target/evosuite-template-1.2.1-SNAPSHOT.jar"
CLIENT_JAR="$EVOSUITE_HOME/client/target/evosuite-client-1.2.1-SNAPSHOT.jar"
RUNTIME_JAR="$EVOSUITE_HOME/runtime/target/evosuite-runtime-1.2.1-SNAPSHOT.jar"

MAVEN_REPO="$HOME/.m2/repository"
JACKSON_JAR="$MAVEN_REPO/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar"
JACKSON_CORE="$MAVEN_REPO/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar"
JACKSON_ANNS="$MAVEN_REPO/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar"
XSTREAM_JAR="$MAVEN_REPO/com/thoughtworks/xstream/xstream/1.4.18/xstream-1.4.18.jar"
MXPARSER_JAR="$MAVEN_REPO/io/github/x-stream/mxparser/1.2.2/mxparser-1.2.2.jar"

CP="$TEMPLATE_JAR:$CLIENT_JAR:$RUNTIME_JAR:$JACKSON_JAR:$JACKSON_CORE:$JACKSON_ANNS:$XSTREAM_JAR:$MXPARSER_JAR"

echo "============================================================"
echo "EvoSuite Template Test Generator"
echo "============================================================"
echo "配置文件: $CONFIG_FILE"
echo "目标类:   $TARGET_CLASS"
echo "Classpath: $PROJECT_CP"
echo "============================================================"

java -cp "$CP" org.evosuite.template.cli.TemplateCommand \
    -t "$TARGET_CLASS" \
    -c "$CONFIG_FILE" \
    -cp "$PROJECT_CP"