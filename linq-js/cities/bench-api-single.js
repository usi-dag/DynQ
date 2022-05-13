

const cities = require('cities');
const citiesT = require('./cities-trufflelinq');
const citiesH = require('./cities-handwritten');


const show = require('../warmup').show;
const benchmark = require('nodemark');

const warmupIter = 50000;
const benchIter = 1000;

if(process.argv.length != 4) {
    console.error("Wrong number of arguments, expected 2: API, implementation");
    process.exit();
}


var api = process.argv[2];
var implName = process.argv[3];

var impl;
if(implName == 'default') {
    impl = cities;
} else if(implName == 'truffleLINQ') {
    impl = citiesT;
} else if(implName == 'js') {
    impl = citiesH;
} else {
    console.error(implName + " -- wrong implementation, expected: default, truffleLINQ, js");
    process.exit();
}

var exec;
if(api == 'zipLookup') {
    exec = () => impl.zipLookup('07946');
} else if(api == 'findByState') {
    exec = () => impl.findByState('NJ');
} else if(api == 'findByCityAndState') {
    exec = () => impl.findByCityAndState('millington', 'NJ');
} else if(api == 'gpsLookup') {
    exec = () => impl.gps_lookup(40.672823, -74.52011);
} else if(api == 'gpsLookupJavaUDF') {
    api = 'gpsLookup'
    implName = "truffleLINQ(Java UDF)"
    exec = () => impl.gps_lookupJavaUDF(40.672823, -74.52011);
} else {
    console.error(api + " -- wrong API, expected: zipLookup, findByState, findByCityAndState, gpsLookup");
    process.exit();
}

if(api == 'gpsLookup') {
    show(api+'_'+implName, exec, 5000, 1000);
} else {
    show(api+'_'+implName, exec, warmupIter, benchIter);
}
