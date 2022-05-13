
var locations = require('./node_modules/cities/locations');
var haversine = require('haversine');
var _ = require('lodash');

exports.zip_lookup = exports.zipLookup = function(zipcode) {
    const zipcodeStr = String(zipcode);
    // const zip = zipcodeStr.length >= 5 ? zipcodeStr : ('0'.repeat(5-zipcodeStr.length) + zipcodeStr);
    const zip = _.padStart(zipcode, 5, 0);
    for (var i=0; i<locations.length; i++) {
        if(locations[i].zipcode === zip) {
            return locations[i];
        }
    }
};


exports.findByState = function(state) {
    state = state.toUpperCase()
    const result = [];
    var resIndex = 0;
    for (var i=0; i<locations.length; i++) {
        if(locations[i].state_abbr === state) {
            // result.push(locations[i]);
            result[resIndex++] = locations[i];
        }
    }
    return result;
}

exports.findByCityAndState = function(city, state) {
    state = state.toUpperCase()
    city = city.toUpperCase()
    for (var row of locations) {
        if(row.state_abbr === state && row.city.toUpperCase() === city) {
            return row;
        }
    }
}

exports.findByCityAndState2 = function(city, state) {
    state = state.toUpperCase()
    city = city.toUpperCase()
    for (var i=0; i<locations.length; i++) {
        const row = locations[i];
        if(row.state_abbr === state && row.city.toUpperCase() === city) {
            return row;
        }
    }
}
exports.findByCityAndState3 = function(city, state) {
    state = state.toUpperCase()
    city = city.toUpperCase()
    return locations.find(row => row.state_abbr === state && row.city.toUpperCase() === city);
}



exports.gps_lookup = exports.gpsLookup = function(latitude, longitude) {
    var minDistance = Infinity; // simulate infinity
    var minLocation = {};

    var start = {
        latitude: latitude,
        longitude: longitude
    }

    for (var i = 0; i < locations.length; i++) {
        var distance = haversine(start, locations[i]);

        if (distance < minDistance) {
            minLocation = locations[i];
            minDistance = distance;
        }
    }

    minLocation.distance = minDistance;
    return minLocation;
};
