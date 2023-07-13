
# DynQ - a Dynamic Query engine
DynQ was designed to bridge the gap between LINQ frameworks and dynamically typed programming languages.


### Build
DynQ has been tested on Ubuntu Linux (20.04 and 22.04) and can be built with the provided script:

`./scripts/build.sh`

### Run
DynQ can be embedded in GraalVM languages by appending its .jar files to the classpath.

Scripts to embed DynQ on GraalJS (node) and FastR are provided:
```
./scripts/node.sh
./scripts/r.sh # equivalent to RScript in GnuR
``` 


### Usage Example
All DynQ APIs are under active development and may change at any time.

Usage examples can be found in the folders `linq-js` and `linq-r`.

A simple usage example can be found in `linq-js/example.js` and run with:

`./scripts/node.sh linq-js/example.js`


### Development
DynQ is developed by Filippo Schiavio ([@flpo](https://github.com/flpo))
