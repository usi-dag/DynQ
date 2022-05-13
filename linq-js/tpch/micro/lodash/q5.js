
const _ = require('lodash');
const dateFrom = new Date('1995-12-01');

function query(tpch) {
    return _.chain(tpch.lineitem)
        .filter(r => r.l_shipdate >= dateFrom)
        .sortBy(['l_orderkey', 'l_extendedprice'])
        .map(function(r) {return {l_extendedprice: r.l_extendedprice}})
        .take(1000)
        .value();
}

module.exports = query;
