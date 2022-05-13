
const WARMUP_ITERS = parseInt(process.env.DYNQ_WARMUP_ITER || 1500);
const BENCH_ITERS = parseInt(process.env.DYNQ_BENCH_ITER || 300);

// Bench Utils
const tpchDataPath = process.env.DYNQ_TPCH_BENCH_DATA || './data/tpch/sf0.01/';
const benchMode = process.env.DYNQ_BENCH_MODE == 'true';
const show = require('../warmup.js').show;

// TPC-H Utils
const load_tpch = require('./load_tpch.js');
const data = load_tpch.load(tpchDataPath);
const sqlQueries = require('./tpch_sql_queries');
const sqlQueriesMicro = require('./micro/tpch_sql_queries_qualified').queries;
const sqlQueriesDynamicTablesMicro = require('./micro/tpch_sql_queries_qualified').withPrefix('dynamic');
const queriesDynamicTables = require('./tpch_sql_queries_qualified').withPrefix('dynamic');

const API = require('../truffleLinq').API();

function getImplementationMicro(i, impl) {
    switch (impl) {
        case 'linqjs-calcite':
            const sql = sqlQueriesMicro[i];
            return () => API.sqlCalcite(sql);
        case 'linqjs-schema':
            return API.prepare(sqlQueries[i]);
        case 'linqjs':
            return API.prepare(sqlQueriesDynamicTablesMicro[i]);
        case 'js':
            const jsHandwritten = require(`./micro/handwritten/q${i}.js`);
            return () => jsHandwritten(rawTables);
        case 'lodash':
            const jsLodash = require(`./micro/lodash/q${i}.js`);
            return () => jsLodash(rawTables);
    }
}


const queries = [1, 2, 3, 4, 5, 6, 7];
const implementations = ['linqjs', 'js', 'lodash'];
const rawTables = [];

data.then(function (tpch) {
    for (var table in tpch) {
        API.session.registerTable(table, tpch[table], 'schema');
        API.session.registerTable("dynamic_" + table, tpch[table], 'dynamic');
        rawTables[table] = tpch[table];
    }

    if(process.argv.length == 3) {
        // impl given
        const impl = process.argv[2];
        queries.forEach(function(q) {
            if(impl == 'lodash' && q == 7) return;
            const func = getImplementationMicro(q, impl);
            show(`${q},${impl}`, func, WARMUP_ITERS, BENCH_ITERS);
        });
    } else {
        queries.forEach(function(q) {
            implementations.forEach(function(impl) {
                if(impl == 'lodash' && q == 7) return;
                const func = getImplementationMicro(q, impl);
                show(`${q},${impl}`, func, WARMUP_ITERS, BENCH_ITERS);
            });
        });
    }
}).catch(function(err) {
    console.error(err);
});
