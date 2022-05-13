
const useSchema = process.env['DYNQ_AFTERBURNER_USE_SCHEMA'] == 'true';
const useAfterBurnerPlans = process.env['DYNQ_AFTERBURNER_PLANS'] == 'true';

var queriesAft;
var queriesOpt;

if(useSchema) {
    module.exports.queries = require('./tpch_afterburner_queries').queries;
    queriesAft = require('./tpch_afterburner_queries_aftplan').queries;
    queriesOpt = require('./tpch_afterburner_queries_opt').queries;
} else {
    module.exports.queries = require('./tpch_afterburner_queries_qualified').queries;
    queriesAft = require('./tpch_afterburner_queries_qualified_aftplan').queries;
    queriesOpt = require('./tpch_afterburner_queries_qualified_opt').queries;
}

module.exports.queriesOpt = useAfterBurnerPlans ? queriesAft : queriesOpt;
