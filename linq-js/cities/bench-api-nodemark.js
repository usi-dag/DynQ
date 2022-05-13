

const cities = require('cities');
const citiesT = require('./cities-trufflelinq');
const citiesH = require('./cities-handwritten');


// const show = require('../warmup').show;
const benchmark = require('nodemark');

function show(msg, func, warmup1, warmaup2) {
    // for (var i = 0; i < warmaup2; i++) {
    //     func();
    // }
    benchmark(func, undefined, 20000);
    const result = benchmark(func, undefined, 10000);
    //console.log(`${msg},${result.hz()},${result.error}`);
    console.log(`${msg},${result.milliseconds()},${result.error}`);
}

show('zipLookup_default', () => cities.zipLookup('07946'), 100, 50);
show('zipLookup_truffleLINQ', () => citiesT.zipLookup('07946'), 100, 50);
show('zipLookup_js', () => citiesH.zipLookup('07946'), 100, 50);

show('findByState_default', () => cities.findByState('NJ'), 100, 50);
show('findByState_truffleLINQ', () => citiesT.findByState('NJ'), 100, 50);
show('findByState_js', () => citiesH.findByState('NJ'), 100, 50);

show('findByCityAndState_default', () => cities.findByCityAndState('millington', 'NJ'), 100, 50);
show('findByCityAndState_truffleLINQ', () => citiesT.findByCityAndState('millington', 'NJ'), 100, 50);
show('findByCityAndState_js', () => citiesH.findByCityAndState('millington', 'NJ'), 100, 50);
