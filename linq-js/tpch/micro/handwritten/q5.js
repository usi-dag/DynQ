
const dateFrom = new Date('1995-12-01');
const PQ = require('js-priority-queue');

function comp(l1, l2) {
    if(l1.l_orderkey < l2.l_orderkey) {
        return -1;
    } else if(l1.l_orderkey > l2.l_orderkey) {
        return 1;
    } else {
        if(l1.l_extendedprice < l2.l_extendedprice) {
            return -1;
        } else if(l1.l_extendedprice > l2.l_extendedprice) {
            return 1;
        } else {
            return 0;
        }
    }
}

function query(tpch) {
    const lineitem = tpch.lineitem;

    /*

    const res1 = [];
    var idx = 0;
    for (var i = 0; i < lineitem.length; i++) {
        if(lineitem[i].l_shipdate >= dateFrom) {
            res1.push(lineitem[i]);
        }
    }

    res1.sort(function(l1, l2) {
        if(l1.l_orderkey < l2.l_orderkey) {
            return -1;
        } else if(l1.l_orderkey > l2.l_orderkey) {
            return 1;
        } else {
            if(l1.l_extendedprice < l2.l_extendedprice) {
                return -1;
            } else if(l1.l_extendedprice > l2.l_extendedprice) {
                return 1;
            } else {
                return 0;
            }
        }
    });


    const res = res1.slice(0, 1000);
     */

    const pq = new PQ({comparator: comp});
    for (var i = 0; i < lineitem.length; i++) {
        if(lineitem[i].l_shipdate >= dateFrom) {
            pq.queue(lineitem[i]);
        }
    }
    const res = [];
    const m = Math.min(pq.length, 1000);
    for (var i = 0; i < m; i++) {
        res[i] = {l_extendedprice: pq.dequeue().l_extendedprice};
    }
    return res;
    // return res.map(function(l){ return {l_extendedprice: l.l_extendedprice}});
}

module.exports = query;