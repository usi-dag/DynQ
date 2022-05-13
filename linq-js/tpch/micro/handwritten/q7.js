
const dateFrom = new Date('1995-12-01');

function query(tpch) {

    const lineitem = tpch.lineitem;
    const orders = tpch.orders;

    const ordersBuild = {}
    for (var i = 0; i < orders.length; i++) {
        if(orders[i].o_orderdate >= dateFrom) {
            const row = orders[i];
            const p = row.o_orderkey;
            // ordersBuild[p] = true;
            var group = ordersBuild[p];
            if(!group) {
                group = [];
                ordersBuild[p] = group;
            }
            group.push(row);
        }
    }

    var res = 0;
    for (var i = 0; i < lineitem.length; i++) {
        if(lineitem[i].l_shipdate >= dateFrom) {
            const key = lineitem[i].l_orderkey;
            const relatedOrders = ordersBuild[key];
            if (relatedOrders) {
                for (var j = 0; j < relatedOrders.length; j++) {
                    res += relatedOrders[j].o_totalprice;
                }
            }
        }
    }
    return [{res: res}];
}

module.exports = query;