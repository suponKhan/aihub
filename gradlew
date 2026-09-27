#!/usr/bin/env sh

#
# Gradle wrapper script for Unix
#

APP_HOME="$(cd "$(dirname "$0")/.." && pwd)"
APP_NAME="Gradle"
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

# Use the first available JAVA_HOME
if [ -z "$JAVA_HOME" ] ; then
    JAVA_HOME=$(which java >/dev/null 2>&1 && dirname $(dirname $(readlink -f $(which java))) || echo "/usr")
fi

exec "$JAVA_HOME/bin/java" $DEFAULT_JVM_OPTS -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"