
const _ = require('lodash-joins');
const dateFrom = new Date('1995-12-01');

function query(tpch) {
    const lineitem = _.filter(tpch.lineitem, li => li.l_shipdate >= dateFrom);
    const orders = _.filter(tpch.orders, o => o.o_orderdate >= dateFrom);
    const joined = _.hashInnerJoin(orders, o => o.o_orderkey, lineitem, l => l.l_orderkey);
    // const joined = _.sortedMergeFullOuterJoin(orders, o => o.o_orderkey, lineitem, l => l.l_orderkey);

    return [{res: _.sumBy(joined, r => r.o_totalprice)}];
    // return _.sumBy(joined, 'o_totalprice');
}

module.exports = query;