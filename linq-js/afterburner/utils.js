
// Common Config

const WARMUP_ITERS = parseInt(process.env.DYNQ_WARMUP_ITER || 300);
const BENCH_ITERS = parseInt(process.env.DYNQ_BENCH_ITER || 100);
const NOASM = process.env.AFTERBURNER_NOASM == 'true';
const DEBUG = process.env.AFTERBURNER_DEBUG == 'true';
const DEFAULT_CODEGEN_PURPOSE = process.env.AFTERBURNER_CODEGEN_PURPOSE || 'array2';
const DATA_PATH = process.env.AFTERBURNER_DATA || "../../../../../../tools/afterburner/data/sf0_1";
const RUN_MODE = process.env['AFTERBURNER_RUN_MODE'] || 'orig';
const BENCH_MODE = process.env.DYNQ_BENCH_MODE == 'true';

// DynQ Config
const API_VERSION = process.env.DYNQ_LANGUAGE || "TruffleLINQ_JS";
const DYNQ_PREPARE_QUERY_API = process.env.DYNQ_PREPARE_QUERY_API || "parseQuery";
const DYNQ_DEBUG = process.env.DYNQ_DEBUG == 'true';
const USE_DYNQ_HARDCODED_QUERIES = process.env.DYNQ_USE_AFTERBURNER_HARDCODED_QUERIES == 'true';

// DynQ Queries
const fixHeuristicJoin = require('./heuristicJoinFix').fixHeuristicJoin;
const queryModule = require('./tpch_afterburner_queries_macro');
const queries = queryModule.queriesOpt;
const queriesOrig = queryModule.queries;
const queriesMicro = require('./tpch_micro_afterburner_queries').queries;


function _genAfterBurnerFunction(dir, i, purpose) {
    if(purpose === undefined) purpose = DEFAULT_CODEGEN_PURPOSE;
    const qStr = ('0'+new String(i)).slice(-2);
    const query = require(dir + qStr);
    if(RUN_MODE == 'cached') {
        const cached = query(NOASM);
        const code = NOASM ? cached.toVanilla(purpose) : cached.toString(purpose);
        if(DEBUG) {
            console.log('QUERY', query);
            console.log('CODE', code);
        }
        return new Function('ignore', code);
    } else {
        if(purpose == 'mat') {
            return () => query(NOASM).materialize(NOASM);
        } else if(purpose == 'array2') {
            return () => query(NOASM).toArray2(NOASM);
        } else if(purpose == 'obj') {
            return () => query(NOASM).toOBJ(NOASM);
        }
    }
}

function genAfterBurnerFunction(i, purpose) {
    return _genAfterBurnerFunction("./lib/src/tpch/q", i, purpose);
}
function genAfterBurnerFunctionMicro(i, purpose) {
    return _genAfterBurnerFunction("./lib/src/micro/q", i, purpose);
}


function init(path) {
    const src = './src';

    global.store = require('./lib/src/core/store');
    global.aSchema = require('./lib/src/core/aSchema');
    global.Afterburner = require('./lib/src/core/afterburner').Afterburner;
    global.queryResult = require('./lib/src/core/queryResult');
    global.fsql2sql = require('./lib/src/core/fsql2sql.js');

    global.abdb = new Afterburner();
    global.FSi = new fsql2sql();
    global.daSchema = new aSchema();
    global.abdb = abdb;
    global.printSchema = require('./lib/src/core/common.js').printSchema;
    global.queryResult = require('./lib/src/core/queryResult.js');

    printSchema = require('./lib/src/core/common.js').printSchema;
    global.alert = function (x) { console.log(x); };
    global.tpch_f1 = 1;

    const fs = require('fs');

    const files = [];
    fs.readdirSync(path).forEach(file => {
        if(file.endsWith('.tbl')) {
            files.push(path + '/' + file)
        }
    });
    daSchema = new aSchema();
    const dataSource = require('./lib/src/core/dataSource');
    const aTable = require('./lib/src/core/aTable');
    //var ds= new dataSource(files, function(){newTable= new aTable(ds);},0);
    files.forEach(f => {
        if(!BENCH_MODE) {
            console.time(f);
        }
        var ds = new dataSource(f);
        var t = new aTable(ds);
        // daSchema.addTable(t);
        if(!BENCH_MODE) {
            console.timeEnd(f);
        }
    })

}

function initDynQ() {
    const API = Polyglot.eval(API_VERSION, "API");
    API.session.registerAfterBurner(mem, daSchema);
    return API;
}

function prepareDynQFunction() {

}
function getDynQFunction(dynq, i) {
    const norm = s => s.replace(/(\r\n|\n|\r|\t)/gm, " ").replace(/\s+/g, ' ');
    var func = dynq.session.prepareHardCodedTPCH(i);
    if((!USE_DYNQ_HARDCODED_QUERIES) || func == undefined) {
        const prepare = dynq[DYNQ_PREPARE_QUERY_API];
        var sql;
        if(norm(queries[i]) == norm(queriesOrig[i])) {
            Java.type('java.lang.System').setProperty("DYNQ_REORDER_JOINS", "true")
            fixHeuristicJoin(i);
            if(DYNQ_DEBUG) {
                console.log("Use default query")
            }
        } else {
            Java.type('java.lang.System').setProperty("DYNQ_REORDER_JOINS", "false")
            if(DYNQ_DEBUG) {
                console.log("Use optimized query")
            }
        }
        func = prepare(queries[i]);
    }
    return func;
}

function getDynQFunctionMicro(dynq, i) {
    const prepare = dynq[DYNQ_PREPARE_QUERY_API];
    return prepare(queriesMicro[i])
}

module.exports.WARMUP_ITERS = WARMUP_ITERS;
module.exports.BENCH_ITERS = BENCH_ITERS;
module.exports.NOASM = NOASM;
module.exports.DEBUG = DEBUG;
module.exports.DEFAULT_CODEGEN_PURPOSE = DEFAULT_CODEGEN_PURPOSE;
module.exports.DATA_PATH = DATA_PATH;
module.exports.RUN_MODE = RUN_MODE;

module.exports.init = init;

module.exports.genAfterBurnerFunction = genAfterBurnerFunction;
module.exports.genAfterBurnerFunctionMicro = genAfterBurnerFunctionMicro;

module.exports.initDynQ = initDynQ;
module.exports.getDynQFunction = getDynQFunction;
module.exports.getDynQFunctionMicro = getDynQFunctionMicro;
