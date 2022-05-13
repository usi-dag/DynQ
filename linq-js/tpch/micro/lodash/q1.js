
const _ = require('lodash');
const dateFrom = new Date('1995-12-01');

function query(tpch) {
    return [{res: _.sumBy(tpch.lineitem, r => r.l_shipdate >= dateFrom)}];
}

module.exports = query;