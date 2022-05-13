
const WARMUP_ITERS = parseInt(process.env.DYNQ_WARMUP_ITER || 1500);
const BENCH_ITERS = parseInt(process.env.DYNQ_BENCH_ITER || 300);

// Bench Utils
const tpchDataPath = process.env.DYNQ_TPCH_BENCH_DATA || './data/tpch/sf0.01/';
const benchMode = process.env.DYNQ_BENCH_MODE == 'true';
const show = require('./warmup.js').show;

// TPC-H Utils
const load_tpch = require('./tpch/load_tpch.js');
const data = load_tpch.load(tpchDataPath);
const sqlQueries = require('./tpch/tpch_sql_queries');
const sqlQueriesMicro = require('./tpch/micro/tpch_sql_queries_qualified').queries;
const sqlQueriesDynamicTablesMicro = require('./tpch/micro/tpch_sql_queries_qualified').withPrefix('dynamic');
const queriesDynamicTables = require('./tpch/tpch_sql_queries_qualified').withPrefix('dynamic');


const API = require('./truffleLinq').API();


const queries = [];
const rawTables = [];
function loadMacro() {
    for (var i = 1; i <= 22; i++) {
        const sql = sqlQueries[i];
        if(queries[i] == undefined) {
            queries[i] = {};
        }
        queries[i]['linqjs-schema'] = () => API.sql(sql);
        queries[i]['linqjs-calcite'] = () => API.sqlCalcite(sql);

        const sqlDynamic = queriesDynamicTables[i];
        queries[i]['linqjs'] = () => API.sql(sqlDynamic);
    }
}

const queriesMicro = [];
function loadMicro() {
    for (var i = 1; i <= 7; i++) {
        const sql = sqlQueriesMicro[i];
        if(queriesMicro[i] == undefined) {
            queriesMicro[i] = {};
        }
        queriesMicro[i]['linqjs-calcite'] = () => API.sqlCalcite(sql);
        queriesMicro[i]['linqjs'] = API.prepare(sql);

        // handwritten
        try {
            const jsHandwritten = require(`./tpch/micro/handwritten/q${i}.js`);
            queriesMicro[i]['js'] = () => jsHandwritten(rawTables);
        } catch {}

        // lodash
        try {
            const jsLodash = require(`./tpch/micro/lodash/q${i}.js`);
            queriesMicro[i]['lodash'] = () => jsLodash(rawTables);
        } catch(e) {}
    }
}

function getImplementation(i, impl) {
    switch (impl) {
        case 'linqjs-calcite':
            const sql = sqlQueries[i];
            return () => API.sqlCalcite(sql);
        case 'linqjs-schema':
            return API.prepare(sqlQueries[i]);
        case 'linqjs':
            return API.prepare(queriesDynamicTables[i]);
        case 'js':
            const jsHandwritten = require(`./tpch/handwritten/q${i}.js`);
            return () => jsHandwritten(rawTables);
        case 'lodash':
            const jsLodash = require(`./tpch/lodash/q${i}.js`);
            queriesMicro[i]['lodash'] = () => jsLodash(rawTables);
    }
}

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
            const jsHandwritten = require(`./tpch/micro/handwritten/q${i}.js`);
            return () => jsHandwritten(rawTables);
        case 'lodash':
            const jsLodash = require(`./tpch/micro/lodash/q${i}.js`);
            return () => jsLodash(rawTables);
    }
}

// Parst TPC-H data and execute benchmarks
if(!benchMode) {
    console.time('parsing');
}

data.then(function (tpch) {
    if(!benchMode) {
        console.timeEnd('parsing');
    }

    for(var table in tpch) {
        API.session.registerTable(table, tpch[table], 'schema');
        API.session.registerTable("dynamic_" + table, tpch[table], 'dynamic');
        rawTables[table] = tpch[table];
    }

    function exec(query, impl) {
        const func = getImplementation(query, impl);
        const msg = benchMode ? `${query},${impl}` : `== Query${query} with implementation ${impl}`;
        show(msg, func, WARMUP_ITERS, BENCH_ITERS);
    }

    function execMicro(query, impl) {
        const func = getImplementationMicro(query, impl);
        const msg = benchMode ? `${query},${impl}` : `== Query${query} (micro) with implementation ${impl}`;
        show(msg, func, WARMUP_ITERS, BENCH_ITERS);
    }

    function execAll(query) {
        const execImplementations = queries[query];
        for(var impl in execImplementations) {
            exec(query, impl);
        }
    }

    if(process.argv.length == 4) {
        const query = parseInt(process.argv[2]);
        const impl = process.argv[3];
        exec(query, impl);
    } else if(process.argv.length == 5 && process.argv[2] == 'micro') {
        const query = parseInt(process.argv[3]);
        const impl = process.argv[4];
        execMicro(query, impl);
    } else {
        console.error("Wrong arguments:", process.argv);
    }
}).catch(e => {
    console.log(e);
});
