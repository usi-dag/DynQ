
const dateFrom = new Date('1995-12-01');

function query(tpch) {
    const lineitem = tpch.lineitem;

    const res = [];
    for (var i = 0; i < lineitem.length; i++) {
        if(lineitem[i].l_shipdate >= dateFrom) {
            res.push({'res': lineitem[i].l_discount * lineitem[i].l_extendedprice});
            if(res.length == 1000) {
                return res;
            }
        }
    }
    return res;
}

module.exports = query;