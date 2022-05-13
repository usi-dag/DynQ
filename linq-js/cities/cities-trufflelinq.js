
const locations = require('./node_modules/cities/locations');
const haversine = require('haversine');
const _ = require('lodash');

const TruffleLINQ = require('../truffleLinq');
const API = TruffleLINQ.API();

API.session.registerTable('locations', locations, 'dynamic');
API.session.registerUDF('haversine', haversine);

const zipLookupQuery = API.prepareParametric(`SELECT * FROM locations WHERE zipcode=? LIMIT 1`);
const findByStateQuery = API.prepareParametric(`SELECT * FROM locations WHERE state_abbr=?`);
const findByCityAndStateQuery = API.prepareParametric(`SELECT * FROM locations WHERE state_abbr=? AND UPPER(city)=? LIMIT 1`);
const gpsLookupQuery = API.prepareParametric(`SELECT * FROM locations ORDER BY haversine(__this__, ?) LIMIT 1`);

exports.zip_lookup = exports.zipLookup = function(zipcode) {
    const zip = _.padStart(String(zipcode), 5, 0);
    return zipLookupQuery(zip);
};

exports.findByState = function(state) {
    return findByStateQuery(state.toUpperCase());
}

exports.findByCityAndState = function(city, state) {
    return findByCityAndStateQuery(state.toUpperCase(), city.toUpperCase());
}

exports.gps_lookup = exports.gpsLookup = function(latitude, longitude) {
    return gpsLookupQuery({latitude: latitude, longitude: longitude});
}

exports.isJavaScriptSpecific = () => TruffleLINQ.isJavaScriptSpecific();

if(TruffleLINQ.isJavaScriptSpecific()) {
    const gpsLookupJavaUDFQuery = API.prepareParametric(`SELECT * FROM locations ORDER BY haversineJavaUDF(__this__, ?) LIMIT 1`);
    exports.gps_lookupJavaUDF = function(latitude, longitude) {
        return gpsLookupJavaUDFQuery({latitude: latitude, longitude: longitude});
    }
}
