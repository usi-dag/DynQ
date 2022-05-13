#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Common config
. $DIR/common.sh

# R Specific Config
export DYNQ_MERGE_RANGES="true"
export  DYNQ_PUSH_TO_CT="${DYNQ_PUSH_TO_CT:-true}"

# activate (R) language specific optimizations by default
export DYNQ_LANGUAGE=${DYNQ_LANGUAGE:="TruffleLINQ_R"}

if hash numactl 2>/dev/null; then
  numactl --cpubind=0 --membind=0 $GRAALVM/bin/Rscript $R_OPTIONS --R.BackEnd=llvm --jvm --polyglot $q $JAVA_OPTIONS $EXTRA_JAVA_OPTIONS $@
else
  $GRAALVM/bin/Rscript $R_OPTIONS --R.BackEnd=llvm --jvm --polyglot $q $JAVA_OPTIONS $EXTRA_JAVA_OPTIONS $@
fi
