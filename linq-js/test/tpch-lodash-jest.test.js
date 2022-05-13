
const API = require('../truffleLinq').API();

// TPC-H Utils
const testDataPath = process.env.DYNQ_TPCH_TEST_DATA || "../data/tpch/test/";
const load_tpch = require('../tpch/load_tpch.js');
const queries = require('../tpch/tpch_sql_queries');

// Lodash Queries
const q1lodash = require('../tpch/lodash/q1');
const q6lodash = require('../tpch/lodash/q6');
const q14lodash = require('../tpch/lodash/q14');


// Load Data
const rawTables = {};
function loadData() {
  return load_tpch.load(testDataPath).then(function (tpch) {
    for(var table in tpch) {
      API.session.registerTable(table, tpch[table], 'schema');
      rawTables[table] = tpch[table];
    }
  });
}

jest.setTimeout(120000); // 2 mins max data loading

beforeAll(loadData);

const deepClose = require('jest-matcher-deep-close-to');
const toBeDeepCloseTo = deepClose.toBeDeepCloseTo;
const toMatchCloseTo = deepClose.toMatchCloseTo;
expect.extend({toBeDeepCloseTo, toMatchCloseTo});


describe("TPC-H Tests", function () {

  test("Q1", function() {
    var query = queries[1];
    var resultLodash = q1lodash(rawTables);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultCalcite).toBeDeepCloseTo(resultLodash);
  });

  test("Q6", function() {
    var query = queries[6];
    var resultLodash = q6lodash(rawTables);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultCalcite).toBeDeepCloseTo(resultLodash);
  });

  test("Q14", function() {
    var query = queries[14];
    var resultLodash = q14lodash(rawTables);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultCalcite).toBeDeepCloseTo(resultLodash);
  });

});
