
const dateFrom = new Date('1995-12-01');

function query(tpch) {
    const lineitem = tpch.lineitem;

    var sum = 0;
    for (var i = 0; i < lineitem.length; i++) {
        if(lineitem[i].l_shipdate >= dateFrom) {
            sum += lineitem[i].l_extendedprice * lineitem[i].l_discount;
        }
    }
    return [{res: sum}];
}

module.exports = query;