
const _ = require('lodash');
const dateFrom = new Date('1995-12-01');

function query(tpch) {
    return _.chain(tpch.lineitem)
        .filter(r => r.l_shipdate >= dateFrom)
        .map(function(r) {return {res: r.l_extendedprice * r.l_discount}})
        .take(1000)
        .value();
}

module.exports = query;