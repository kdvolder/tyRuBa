#!/bin/bash
set -e
workdir=$(pwd)
JAVACC_VERSION=4.0
JAVACC_JAR=${workdir}/javacc/javacc-${JAVACC_VERSION}.jar
if [ ! -f ${JAVACC_JAR} ]; then
    mkdir -p javacc
    cd javacc
    curl -o javacc-${JAVACC_VERSION}.jar https://repo1.maven.org/maven2/net/java/dev/javacc/javacc/4.0/javacc-4.0.jar
fi
cd ${workdir}/src/tyRuBa/parser
rm -rf *.java
java -cp $JAVACC_JAR javacc TyRuBaParser.jj
