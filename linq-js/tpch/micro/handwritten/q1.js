
const dateFrom = new Date('1995-12-01');

function query(tpch) {
    const lineitem = tpch.lineitem;

    var count = 0;
    for (var i = 0; i < lineitem.length; i++) {
        if(lineitem[i].l_shipdate >= dateFrom) {
            count++;
        }
    }
    return [{res: count}];
}

module.exports = query;