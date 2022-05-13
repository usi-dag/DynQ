
const _ = require('lodash');
const dateFrom = new Date('1995-12-01');
const dateTo = new Date('1997-01-01');

function query(tpch) {
    return [{res: _.sumBy(tpch.lineitem, r => r.l_shipdate >= dateFrom &&  r.l_shipdate < dateTo ? r.l_discount * r.l_extendedprice : 0)}];
}

module.exports = query;