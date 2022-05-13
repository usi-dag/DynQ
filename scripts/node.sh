#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Common config
. $DIR/common.sh

# activate (JS) language specific optimizations by default
export DYNQ_LANGUAGE=${DYNQ_LANGUAGE:="TruffleLINQ_JS"}


if hash numactl 2>/dev/null; then
  numactl --cpubind=0 --membind=0 $GRAALVM/bin/node --jvm --polyglot $JAVA_OPTIONS $EXTRA_JAVA_OPTIONS $@
else
  $GRAALVM/bin/node --jvm --polyglot $JAVA_OPTIONS $EXTRA_JAVA_OPTIONS $@
fi