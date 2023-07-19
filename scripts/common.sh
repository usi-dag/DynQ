#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

DYNQ_HOME=$DIR/..

GRAALVM_DIR="graalvm-ce-java11-21.3.0"
export GRAALVM_DYNQ="${GRAALVM_DYNQ:-$DYNQ_HOME/$GRAALVM_DIR/}"
export GRAALVM=$GRAALVM_DYNQ

if [ -z "$DYNQ_JAR" ]
  then
    export DYNQ_JAR="$DYNQ_HOME/mxbuild/dists/dynq.jar"
    export DYNQ_JAR+=":$DYNQ_HOME/mxbuild/dists/dynq-js.jar"
    export DYNQ_JAR+=":$DYNQ_HOME/mxbuild/dists/dynq-r.jar"
fi

JAVA_OPTIONS="--vm.Dtruffle.class.path.append=$DYNQ_JAR --vm.cp=$DYNQ_JAR "

JAVA_OPTIONS+=" --experimental-options --engine.MultiTier=false  --engine.TraversingCompilationQueue=false "

export JAVA_OPTIONS


##### DynQ Configuration #####

# Planning - Calcite
export DYNQ_MERGE_AGG_PROJ="${DYNQ_MERGE_AGG_PROJ:-true}"
export DYNQ_USE_AVG_NODE="${DYNQ_USE_AVG_NODE:-true}"
export DYNQ_USE_SEMI_JOIN="${DYNQ_USE_SEMI_JOIN:-true}"
export DYNQ_USE_ANTI_JOIN="${DYNQ_USE_ANTI_JOIN:-true}"
export DYNQ_USE_BUSHY="${DYNQ_USE_BUSHY:-true}"
export DYNQ_USE_HEURISTIC_JOIN_ORDER="${DYNQ_USE_HEURISTIC_JOIN_ORDER:-true}"


# Planning - DynQ
export DYNQ_MERGE_RANGES="${DYNQ_MERGE_RANGES:-true}"
export DYNQ_OPTIMIZE_EQ="${DYNQ_OPTIMIZE_EQ:-true}"
export DYNQ_MERGE_OR_INTO_IN="${DYNQ_MERGE_OR_INTO_IN:-true}"
export DYNQ_USE_GROUPJOIN="${DYNQ_USE_GROUPJOIN:-true}"
export DYNQ_USE_REVERSED_GROUPJOIN="${DYNQ_USE_REVERSED_GROUPJOIN:-true}"
export DYNQ_USE_REVERSED_SCALAR_GROUPJOIN="${DYNQ_USE_REVERSED_SCALAR_GROUPJOIN:-true}"
export DYNQ_USE_SCALAR_AGGREGATION="${DYNQ_USE_SCALAR_AGGREGATION:-true}"
export DYNQ_REUSE_AGGREGATIONS="${DYNQ_REUSE_AGGREGATIONS:-true}"
export DYNQ_REMOVE_SIMPLE_PROJECTIONS="${DYNQ_REMOVE_SIMPLE_PROJECTIONS:-true}"

export DYNQ_PUSH_TO_LOOP_NODE="${DYNQ_PUSH_TO_LOOP_NODE:-false}"
export DYNQ_PUSH_TO_CT="${DYNQ_PUSH_TO_CT:-true}"

# DynQ Engine - Implementations
export DYNQ_CT_MORSEL_SIZE="${DYNQ_CT_MORSEL_SIZE:-100}"

# Benchmarks
export DYNQ_GC_EACH_ITER="${DYNQ_GC_EACH_ITER:-false}"
export DYNQ_PREPARE_QUERY_API="${DYNQ_PREPARE_QUERY_API:-parseQuery}"

##### Language-Specific Configuration

# R
export DYNQ_R_TABLETYPE="${DYNQ_R_TABLETYPE:-"dynamic"}" # dynamic -- schema
export DYNQ_R_USE_NA_CHECKS="${DYNQ_R_USE_NA_CHECKS:-true}"

# JS
export DYNQ_JS_ARRAY="${DYNQ_JS_ARRAY:-true}"
