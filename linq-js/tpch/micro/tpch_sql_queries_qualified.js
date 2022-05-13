
// filter.count
const Q1 = "SELECT COUNT(*) as res FROM lineitem WHERE l_shipdate >= DATE '1995-12-01'"

// filter.sum
const Q2 = "SELECT SUM(l_discount * l_extendedprice) as res FROM lineitem WHERE l_shipdate >= DATE '1995-12-01'"

// filter.filter.sum
const Q3 = `SELECT SUM(l_discount * l_extendedprice) as res FROM lineitem
WHERE (l_shipdate >= DATE '1995-12-01') AND (l_shipdate < DATE '1997-01-01')`

// filter.map
const Q4 = "SELECT l_discount * l_extendedprice as res FROM lineitem WHERE l_shipdate >= DATE '1995-12-01'"

// filter.sort.take
const Q5 = `SELECT l_extendedprice FROM lineitem
WHERE l_shipdate >= DATE '1995-12-01'
ORDER BY l_orderkey, l_extendedprice LIMIT 1000
-- ORDER BY l_orderkey LIMIT 1000`

// filter.map.take
const Q6 = "SELECT l_discount * l_extendedprice as res FROM lineitem WHERE l_shipdate >= DATE '1995-12-01' LIMIT 1000"

// filter.XJoin(filter).sum
const Q7 = `SELECT SUM(orders.o_totalprice) as res
FROM lineitem, orders
WHERE orders.o_orderdate >= DATE '1995-12-01'
AND lineitem.l_shipdate >= DATE '1995-12-01'
AND orders.o_orderkey = lineitem.l_orderkey`

const queries = [undefined, Q1, Q2, Q3, Q4, Q5, Q6, Q7]


function withPrefix(prefix) {
    const queriesQualified = queries;
    const queriesDynamicTables = [];
    for (var i = 1; i <= 7; i++) {
        var tmp = queriesQualified[i];
        tmp = tmp.replace(/\blineitem([^\_])/g, prefix+"_lineitem$1");
        tmp = tmp.replace(/\bcustomer([^\_])/g, prefix+"_customer$1");
        tmp = tmp.replace(/\borders([^\_])/g, prefix+"_orders$1");
        tmp = tmp.replace(/\bregion([^\_])/g, prefix+"_region$1");
        tmp = tmp.replace(/\bpart([^\_])/g, prefix+"_part$1");
        tmp = tmp.replace(/\bpartsupp([^\_])/g, prefix+"_partsupp$1");
        tmp = tmp.replace(/\bsupplier([^\_])/g, prefix+"_supplier$1");

        if(i != 9) { // Q9 requires this renaming trick since it uses `nation` as alias
            tmp = tmp.replace(/\bnation([^\_])/g, prefix+"_nation$1");
        } else {
            tmp = tmp.replace(/\bnation([^\_|,])/g, prefix+"_nation$1");
        }

        queriesDynamicTables[i] = tmp;
    }
    return queriesDynamicTables;
}

module.exports.queries = queries;
module.exports.withPrefix = withPrefix;