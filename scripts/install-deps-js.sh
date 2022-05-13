#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

DYNQ_HOME=$DIR/..

LINQJS=$DYNQ_HOME/linq-js

cd $LINQJS
$GRAALVM/bin/npm install

cd $LINQJS/cities
$GRAALVM/bin/npm install
