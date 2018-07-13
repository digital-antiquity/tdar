#!/bin/sh

JAVA_OPTS="-Djava.awt.headless=true -XX:+UseConcMarkSweepGC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp"
JAVA_OPTS="$JAVA_OPTS -Xms1024m -Xmx2048m  -XX:PermSize=256m -XX:MaxPermSize=1024m -server "

echo "HI SETENV ${JAVA_OPTS}"