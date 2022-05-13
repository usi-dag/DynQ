
const _ = require('lodash');
const dateFrom = new Date('1995-12-01');

function query(tpch) {
    return _.chain(tpch.lineitem)
        .filter(r => r.l_shipdate >= dateFrom)
        .map(function(r) {return {res: r.l_discount * r.l_extendedprice}})
        .value();
}

module.exports = query;