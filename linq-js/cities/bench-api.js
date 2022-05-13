

const cities = require('cities');
const citiesT = require('./cities-trufflelinq');
const citiesH = require('./cities-handwritten');


const show = require('../warmup').show;

const warmupIter = 50000;
const benchIter = 1000;

show('zipLookup_default', () => cities.zipLookup('07946'), warmupIter, benchIter);
show('zipLookup_truffleLINQ', () => citiesT.zipLookup('07946'), warmupIter, benchIter);
show('zipLookup_js', () => citiesH.zipLookup('07946'), warmupIter, benchIter);

show('findByState_default', () => cities.findByState('NJ'), warmupIter, benchIter);
show('findByState_truffleLINQ', () => citiesT.findByState('NJ'), warmupIter, benchIter);
show('findByState_js', () => citiesH.findByState('NJ'), warmupIter, benchIter);

show('findByCityAndState_default', () => cities.findByCityAndState('millington', 'NJ'), warmupIter, benchIter);
show('findByCityAndState_truffleLINQ', () => citiesT.findByCityAndState('millington', 'NJ'), warmupIter, benchIter);
show('findByCityAndState_js', () => citiesH.findByCityAndState('millington', 'NJ'), warmupIter, benchIter);
