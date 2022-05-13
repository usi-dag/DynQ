
const dateFrom = new Date('1995-12-01');
const dateTo = new Date('1997-01-01');

function query(tpch) {
    const lineitem = tpch.lineitem;

    var sum = 0;
    for (var i = 0; i < lineitem.length; i++) {
        const shipdate = lineitem[i].l_shipdate;
        if(shipdate >= dateFrom && shipdate < dateTo) {
            sum += lineitem[i].l_extendedprice * lineitem[i].l_discount;
        }
    }
    return [{res: sum}];
}

module.exports = query;