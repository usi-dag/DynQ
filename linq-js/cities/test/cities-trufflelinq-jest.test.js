// var assert = require("assert");
var cities = require('cities');
var citiesT = require("../cities-trufflelinq");

const deepClose = require('jest-matcher-deep-close-to');
const toBeDeepCloseTo = deepClose.toBeDeepCloseTo;
const toMatchCloseTo = deepClose.toMatchCloseTo;
expect.extend({toBeDeepCloseTo, toMatchCloseTo});



describe('zip_lookup()', function() {
    it('should give me 07946 based on zip code', function() {
        expect(citiesT.zip_lookup('07946')).toBeDeepCloseTo(cities.zip_lookup('07946'));
    });
});

describe('findByState', function() {
    it('should give me a list of zipcodes', function() {
        expect(citiesT.findByState('NJ')).toBeDeepCloseTo(cities.findByState('NJ'));
    })
})

describe('findByCityAndState', function() {
    it('should find me a city', function() {
        expect(citiesT.findByCityAndState('millington', 'NJ')).toBeDeepCloseTo(cities.findByCityAndState('millington', 'NJ'));
    })
})

describe('gps_lookup()', function() {
    it('should give me 07946 based on exact location', function() {
        const res = cities.gps_lookup(40.672823, -74.52011);
        const got = citiesT.gps_lookup(40.672823, -74.52011);
        expect(got['**'].zipcode).toBeDeepCloseTo(res.zipcode);
    });
});

if(citiesT.isJavaScriptSpecific()) {
    describe('gps_lookupJavaUDF()', function () {
        it('should give me 07946 based on exact location', function () {
            const res = cities.gps_lookup(40.672823, -74.52011);
            const got = citiesT.gps_lookupJavaUDF(40.672823, -74.52011);
            expect(got['**'].zipcode).toBeDeepCloseTo(res.zipcode);
        });
    });
}