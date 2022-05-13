
const loc = require('./node_modules/cities/locations');
const cities = require('cities');
const citiesT = require('./cities-trufflelinq');
const citiesH = require('./cities-handwritten');

_ = require('lodash')

const show = require('../warmup').show;

// const res = cities.gps_lookup(40.672823, -74.52011);
// const got = citiesT.gps_lookup(40.672823, -74.52011);
// console.log(res);
// console.log(got);
// console.log(citiesT.gps_lookupJavaUDF(40.672823, -74.52011));
// console.log(citiesT.gps_lookup(40.672823, -74.52011));

// show('', () => citiesT.gps_lookupJavaUDF(40.672823, -74.52011), 200, 100);
// show('', () => citiesT.gps_lookup(40.672823, -74.52011), 200, 100);

// console.log(cities.findByState('NJ').length);

show('', () => citiesT.zip_lookup('07946'), 100, 10000000);
// show('', () => citiesT.findByState('NJ'), 50000, 10000000);
// show('', () => citiesH.findByCityAndState3('millington', 'nj'), 50000, 1000);
// show('', () => citiesH.findByCityAndState2('millington', 'nj'), 50000, 1000);
// show('', () => citiesH.findByCityAndState('millington', 'nj'), 50000, 1000);
// show('', () => citiesH.findByCityAndState3('millington', 'nj'), 50000, 1000);

/*
console.log(cities.zip_lookup('07946'));
console.log(citiesT.zip_lookup('07946'));

console.log(cities.findByState('NJ'));
console.log(citiesT.findByState('NJ'));

console.log(cities.findByCityAndState('millington', 'nj'));
console.log(citiesT.findByCityAndState('millington', 'nj'));
*/