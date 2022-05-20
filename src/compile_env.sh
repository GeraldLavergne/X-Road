#!/bin/bash
set -e

if [ -z "$XROAD_HOME" ]
then
    XROAD_HOME=$(cd "$(dirname "$0")"; pwd)
fi

ARCH=$(arch)
ARCH=${ARCH/aarch64/arm64}
ARCH=${ARCH/x86_64/amd64}

JAVA_HOME="/usr/lib/jvm/java-11-openjdk-$ARCH"
PATH="$JAVA_HOME/bin:$PATH"

export PATH JAVA_HOME XROAD_HOME
