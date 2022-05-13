const utils = require('./utils');
utils.init(utils.DATA_PATH);

const fixHeuristicJoin = require('./heuristicJoinFix').fixHeuristicJoin;

const API_VERSION = process.env.DYNQ_LANGUAGE || "TruffleLINQ_JS";
const API = Polyglot.eval(API_VERSION, "API");
API.session.registerAfterBurner(mem, daSchema);


const queries = require('./tpch_afterburner_queries_qualified').queries;
const queriesMicro = require('./tpch_micro_afterburner_queries').queries;

var fs = require('fs');
var dir = utils.DATA_PATH + '/expected';
if (!fs.existsSync(dir)){
    fs.mkdirSync(dir);
}

function makeTestResults(i) {
    const q = queries[i];
    console.log(queries[i]);
    const result = API.prepare(q)();
    console.log(result);
    fs.writeFileSync(`${dir}/result${i}.json`, JSON.stringify(result));
}

if(process.argv.length > 2) {
    makeTestResults(parseInt(process.argv[2]));
} else {
    for (let i = 1; i < 23; i++) {
        makeTestResults(i);
    }
    for (let i = 1; i < 8; i++) {
        const q = queriesMicro[i];
        const result = API.prepare(q)();
        fs.writeFileSync(`${dir}/result_micro${i}.json`, JSON.stringify(result));
    }
}
