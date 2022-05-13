#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

RESULT_DIR="$DIR/bench-result-api"

if [ ! -d "$RESULT_DIR" ]
then
  mkdir "$RESULT_DIR"
fi

# Bench API Throughput
echo "" > $RESULT_DIR/all.csv

API="zipLookup findByState findByCityAndState"
IMPL="default truffleLINQ js"
#IMPL="truffleLINQ"

for api in $API; do
  for impl in $IMPL; do
    $DIR/../../node.sh $DIR/bench-api-nodemark-single.js $api $impl #>> $RESULT_DIR/all.csv
  done
done
