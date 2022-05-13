
const API = require('../truffleLinq').API();

// TPC-H Utils
const testDataPath = process.env.DYNQ_TPCH_TEST_DATA || "../data/tpch/test/";
const load_tpch = require('../tpch/load_tpch.js');
const queries = require('../tpch/tpch_sql_queries');

// Hand Written Queries
const q1handwritten = require('../tpch/handwritten/q1');
const q6handwritten = require('../tpch/handwritten/q6');
const q14handwritten = require('../tpch/handwritten/q14');


// Load Data
const rawTables = {};
function loadData() {
  return load_tpch.load(testDataPath).then(function (tpch) {
    for(var table in tpch) {
      API.session.registerTable(table, tpch[table], "schema");
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

// no need to test them again, they are now stable
describe.skip("TPC-H Tests", function () {

  test("Q1", function() {
    var query = queries[1];
    var resultHandWritten = q1handwritten(rawTables);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultCalcite).toBeDeepCloseTo(resultHandWritten);
  });

  test("Q6", function() {
    var query = queries[6];
    var resultHandWritten = q6handwritten(rawTables);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultCalcite).toBeDeepCloseTo(resultHandWritten);
  });

  test("Q14", function() {
    var query = queries[14];
    var resultHandWritten = q14handwritten(rawTables);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultCalcite).toBeDeepCloseTo(resultHandWritten);
  });

});
