
const dateFrom = new Date('1995-12-01');

function query(tpch) {
    const lineitem = tpch.lineitem;

    const res = [];
    for (var i = 0; i < lineitem.length; i++) {
        if(lineitem[i].l_shipdate >= dateFrom) {
            res.push({'res': lineitem[i].l_extendedprice * lineitem[i].l_discount});
        }
    }
    return res;
}

module.exports = query;