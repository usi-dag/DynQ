const utils = require('./utils');
utils.init(utils.DATA_PATH);

const fixHeuristicJoin = require('./heuristicJoinFix').fixHeuristicJoin;
const show = require('../warmup.js').show;

const DYNQ_PREPARE_QUERY_API = process.env.DYNQ_PREPARE_QUERY_API || "prepare";
const API_VERSION = process.env.DYNQ_LANGUAGE || "TruffleLINQ";
const API = Polyglot.eval(API_VERSION, "API");
API.session.registerAfterBurner(mem, daSchema);
const prepare = API[DYNQ_PREPARE_QUERY_API];

const queryModule = require('./tpch_afterburner_queries_macro')
const queries = queryModule.queries;

function run(q) {
    fixHeuristicJoin(q);
    const func = prepare(queries[q]);
    show(`Q${q}`, func, utils.WARMUP_ITERS, utils.BENCH_ITERS);
}


if(process.argv.length > 2) {
    run(parseInt(process.argv[2]));
} else {
    for (let i = 1; i < 23; i++) {
        run(i);
    }
}
