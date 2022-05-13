// var assert = require("assert");
var cities = require('cities');
var citiesH = require("../cities-handwritten");

const deepClose = require('jest-matcher-deep-close-to');
const toBeDeepCloseTo = deepClose.toBeDeepCloseTo;
const toMatchCloseTo = deepClose.toMatchCloseTo;
expect.extend({toBeDeepCloseTo, toMatchCloseTo});



describe('zip_lookup()', function() {
    it('should give me 07946 based on zip code', function() {
        expect(citiesH.zip_lookup('07946')).toBeDeepCloseTo(cities.zip_lookup('07946'));
    });
});

describe('findByState', function() {
    it('should give me a list of zipcodes', function() {
        expect(citiesH.findByState('NJ')).toBeDeepCloseTo(cities.findByState('NJ'));
    })
})

describe('findByCityAndState', function() {
    it('should find me a city', function() {
        expect(citiesH.findByCityAndState('millington', 'NJ')).toBeDeepCloseTo(cities.findByCityAndState('millington', 'NJ'));
    })
})
