#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

RESULT_DIR="$DIR/bench-result-api"

if [ ! -d "$RESULT_DIR" ]
then
  mkdir "$RESULT_DIR"
fi

# Bench API Throughput
echo "" > $RESULT_DIR/all.csv

$DIR/../../node.sh $DIR/bench-api-nodemark-single.js gpsLookup default #>> $RESULT_DIR/all.csv
$DIR/../../node.sh $DIR/bench-api-nodemark-single.js gpsLookup truffleLINQ #>> $RESULT_DIR/all.csv
$DIR/../../node.sh $DIR/bench-api-nodemark-single.js gpsLookupJavaUDF truffleLINQ #>> $RESULT_DIR/all.csv
