#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
DYNQ="$DIR/../"
cd $DYNQ

# MX build tool
if [ ! -d "mx" ]; then
  echo "Downloading mx build tool..."
  git clone https://github.com/graalvm/mx.git
  cd mx
  git checkout 5.315.2
fi
cd $DYNQ
export PATH="`pwd`/mx:$PATH"

# GraalVM
GRAALVM_DIR="graalvm-ce-java11-21.3.0"
GRAALVM_TAR="graalvm-ce-java11-linux-amd64-21.3.0.tar.gz"
if [ ! -d "$GRAALVM_DIR" ]; then
  echo "Downloading GraalVM $GRAALVM_TAR..."
  wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.3.0/$GRAALVM_TAR
  tar xf $GRAALVM_TAR
fi
$GRAALVM_DIR/bin/gu install nodejs
$GRAALVM_DIR/bin/gu install R
export GRAALVM_DYNQ="${GRAALVM_DYNQ:-$DYNQ/$GRAALVM_DIR/}"

# OpenJDK 8 (required to build)
JDK8_DIR="openjdk1.8.0_302-jvmci-21.3-b05/"
JDK8_TAR="openjdk-8u302+06-jvmci-21.3-b05-linux-amd64.tar.gz"
if [ ! -d "$JDK8_DIR" ]; then
  echo "Downloading OpenJDK 8 JDK8_TAR..."
  wget "https://github.com/graalvm/graal-jvmci-8/releases/download/jvmci-21.3-b05/$JDK8_TAR"
  tar xf $JDK8_TAR
fi
export JAVA_HOME="$DYNQ/$JDK8_DIR"


# Calcite
if [ ! -f $DYNQ/lib/calcite/target/calcite.jar ]; then
  cd $DYNQ/lib/calcite
  mvn clean package -q -Duser.timezone=Europe/Zurich
fi

# Build DynQ
cd $DYNQ
export MX_PYTHON=python3
mx build
