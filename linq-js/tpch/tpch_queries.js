const API = require('../truffleLinq').API();
const LINQ = API.linq;



// TPC-H Q6
const Q6 = `
SELECT
    sum(l_extendedprice * l_discount) as revenue
FROM
    lineitem
WHERE
    l_shipdate >= date '1994-01-01'
    AND l_shipdate < date '1994-01-01' + interval '1' year
    AND l_discount between 0.06 - 0.01 AND 0.06 + 0.01
    AND l_quantity < 24;
`

const dateFrom = new Date('1994-01-01').getTime();
const dateTo = new Date('1995-01-01').getTime();
function predicateQ6(row) {
    return dateFrom <= row.l_shipdate && dateTo > row.l_shipdate &&
        0.05 <= row.l_discount && 0.07 >= row.l_discount &&
        row.l_quantity < 24;
}

function aggregateFuncQ6(x, row) {
    return x + row.l_extendedprice * row.l_discount;
}

function execQ6Manual(tpch) {
    const lineitem = tpch.lineitem;
    var result = 0;
    for (var i = 0; i < lineitem.length; i++) {
        const row = lineitem[i];
        if(predicateQ6(row)) {
            result += row.l_extendedprice * row.l_discount;
        }
    }
    return result;
}

function execQ6WithLinq(tpch) {
    const lineitem = tpch.lineitem;
    return LINQ
        .asEnumerable(lineitem)
        .where(predicateQ6)
        .aggregate(0, aggregateFuncQ6);
}
function execQ6WithLinqWhereJS(tpch) {
    const lineitem = tpch.lineitem;
    return LINQ
        .asEnumerable(lineitem)
        .whereJS(predicateQ6)
        .aggregate(0, aggregateFuncQ6);
}

const aggregator = [0, aggregateFuncQ6]; // TODO fix -- passing the array as args invalidate guard (arrayEquals(arguments))
function execQ6WithLinq_aggregateFromWhere(tpch) {
    return LINQ.aggregateFromWhere(tpch.lineitem, predicateQ6, aggregator);
}
function execQ6WithLinq_aggregateFromWhereJSTrick(tpch) {
    return LINQ.aggregateFromWhereJSTrick(tpch.lineitem, predicateQ6, aggregator);
}


function execQ6WithLinqSQL(tpch) {
    return LINQ.sql(Q6);
}

// TPC-H Q1
// TODO add avg
const Q1 = `
SELECT
    l_returnflag,
    l_linestatus,
    sum(l_quantity) as sum_qty,
    sum(l_extendedprice) as sum_base_price,
    sum(l_extendedprice * (1 - l_discount)) as sum_disc_price,
    sum(l_extendedprice * (1 - l_discount) * (1 + l_tax)) as sum_charge,
    count(*) as count_order
FROM
    lineitem
WHERE
    l_shipdate <= date '1998-12-01' - interval '90' day
GROUP BY
    l_returnflag,
    l_linestatus
ORDER BY
    l_returnflag,
    l_linestatus
`


const dateToQ1 = new Date('1998-09-01').getTime();
function predicateQ1(row) {
    return dateToQ1 > row.l_shipdate;
}


function execQ1Manual(tpch) {
    const lineitem = tpch.lineitem;
    const groups = {};
    for (var i = 0; i < lineitem.length; i++) {
        const row = lineitem[i];
        if(dateToQ1 > row.l_shipdate) {
            // TODO real map? // {l_returnflag: row.l_returnflag, l_linestatus: row.l_linestatus};
            const key = row.l_returnflag + "$" + row.l_linestatus;
            var group;
            if(groups.hasOwnProperty(key)) {
                group = groups[key];
            } else {
                group = {l_returnflag: row.l_returnflag, l_linestatus: row.l_linestatus, sum_qty: 0, sum_base_price: 0, sum_disc_price: 0, sum_charge: 0, count_order: 0}; // TODO avg
                groups[key] = group;
            }
            group.sum_qty += row.l_quantity;
            group.sum_base_price += row.l_extendedprice;
            group.sum_disc_price += row.l_extendedprice * (1 - row.l_discount);
            group.sum_charge += row.l_extendedprice * (1 - row.l_discount) * (1 + row.l_tax);
            group.count_order++;
        }
    }
    const result = Object.values(groups);
    return result;
    // TODO sort
    // result.sort((row1, row2) => {
    //     const l_returnflag1 = row1.l_returnflag;
    //     const l_returnflag2 = row2.l_returnflag;
    //     const l_linestatus1 = row1.l_linestatus;
    //     const l_linestatus2 = row2.l_linestatus;
    //     if(l_returnflag1 < l_returnflag2) {
    //         return 1;
    //     } else if (l_returnflag1 > l_returnflag2) {
    //         return -1;
    //     } else {
    //         if(l_linestatus1 < l_linestatus2) {
    //             return 1;
    //         } else if(l_linestatus1 > l_linestatus2) {
    //             return -1;
    //         } else {
    //             return 0;
    //         }
    //     }
    // });
}


/*
    Calcite Linq4j

    base:
    <TKey,​TResult> Enumerable<TResult> groupBy​(Function1<TSource,​TKey> keySelector, Function2<TKey,​Enumerable<TSource>,​TResult> resultSelector)
    Groups the elements of a sequence according to a specified key selector function and creates a result value from each group and its key.

    da usare:
    <TKey,​TAccumulate,​TResult> Enumerable<TResult> groupBy​(Function1<TSource,​TKey> keySelector, Function0<TAccumulate> accumulatorInitializer, Function2<TAccumulate,​TSource,​TAccumulate> accumulatorAdder, Function2<TKey,​TAccumulate,​TResult> resultSelector)
    Groups the elements of a sequence according to a specified key selector function, initializing an accumulator for each group and adding to it each time an element with the same key is seen. Creates a result value from each accumulator and its key using a specified function.
*/


function accumulatorInitializerQ1() {
    return {sum_qty: 0, sum_base_price: 0, sum_disc_price: 0, sum_charge: 0, count_order: 0};
}
function accumulatorAdderQ1(group, row) {
    group.sum_qty += row.l_quantity;
    group.sum_base_price += row.l_extendedprice;
    group.sum_disc_price += row.l_extendedprice * (1 - row.l_discount);
    group.sum_charge += row.l_extendedprice * (1 - row.l_discount) * (1 + row.l_tax);
    group.count_order++;
    return group;
}
function keySelectorQ1(row) {
    return row.l_returnflag + "$" + row.l_linestatus;
}
function resultSelectorQ1(key, group) {
    const [l_returnflag, l_linestatus] = key.split("$");
    return {l_returnflag: l_returnflag, l_linestatus: l_linestatus, sum_qty: group.sum_qty, sum_base_price: group.sum_base_price, sum_disc_price: group.sum_disc_price, sum_charge: group.sum_charge, count_order: group.count_order};
}

function sortQ1(row1, row2) {
    const l_returnflag1 = row1.l_returnflag;
    const l_returnflag2 = row2.l_returnflag;
    const l_linestatus1 = row1.l_linestatus;
    const l_linestatus2 = row2.l_linestatus;
    if(l_returnflag1 < l_returnflag2) {
        return 1;
    } else if (l_returnflag1 > l_returnflag2) {
        return -1;
    } else {
        if(l_linestatus1 < l_linestatus2) {
            return 1;
        } else if(l_linestatus1 > l_linestatus2) {
            return -1;
        } else {
            return 0;
        }
    }
}

function execQ1WithLinq(tpch) {
    const lineitem = tpch.lineitem;
    return LINQ
        .asEnumerable(lineitem)
        .where(predicateQ1)
        .groupBy(keySelectorQ1, accumulatorInitializerQ1, accumulatorAdderQ1, resultSelectorQ1)
        // .sortBy(sortQ1)
        ;
}
function execQ1WithLinqWhereJS(tpch) {
    const lineitem = tpch.lineitem;
    return LINQ
        .asEnumerable(lineitem)
        .whereJS(predicateQ1)
        .groupBy(keySelectorQ1, accumulatorInitializerQ1, accumulatorAdderQ1, resultSelectorQ1);
    // TODO sort
}


function execQ1WithLinqSQL(tpch) {
    return LINQ.sql(Q1);
}

const queries = [];
queries[1] = {manual: execQ1Manual, linq: execQ1WithLinq, linqWhereJS: execQ1WithLinqWhereJS, linqSQL: execQ1WithLinqSQL};
queries[6] = {manual: execQ6Manual, linq: execQ6WithLinq, linqWhereJS: execQ6WithLinqWhereJS, linqAgg: execQ6WithLinq_aggregateFromWhere, linqAggJS: execQ6WithLinq_aggregateFromWhereJSTrick, linqSQL: execQ6WithLinq};


function register(tpch) {
    // TODO real schema as arg
    API.session.registerTable('lineitem', tpch.lineitem, 'schema');
}

module.exports = {
    "queries": queries,
    "register": register,
    "Q1": Q1,
    "Q6": Q6,
};
