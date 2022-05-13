
const API = require('../truffleLinq').API();

// TPC-H Utils
const testDataPath = process.env.DYNQ_TPCH_TEST_DATA || "../data/tpch/test/";
const load_tpch = require('../tpch/load_tpch.js');
const queries = require('../tpch/micro/tpch_sql_queries_qualified').queries;
const queriesDynamicTables = require('../tpch/micro/tpch_sql_queries_qualified').withPrefix('dynamic');

// Load Data
function loadData() {
  return load_tpch.load(testDataPath).then(function (tpch) {
    for(var table in tpch) {
      API.session.registerTable(table, tpch[table], 'schema');
      API.session.registerTable("dynamic_" + table, tpch[table], 'dynamic');
    }
  });
}

jest.setTimeout(120000); // 2 mins max data loading
beforeAll(loadData);

const deepClose = require('jest-matcher-deep-close-to');
const toBeDeepCloseTo = deepClose.toBeDeepCloseTo;
const toMatchCloseTo = deepClose.toMatchCloseTo;
expect.extend({toBeDeepCloseTo, toMatchCloseTo});


describe("TPC-H Tests Micro Benchmark Queries (dynamic tables)", function () {

  test("Q1", function() {
    var resultTruffle = API.sql(queriesDynamicTables[1]);
    var resultCalcite = API.sqlCalcite(queries[1]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  test("Q2", function() {
    var resultTruffle = API.sql(queriesDynamicTables[2]);
    var resultCalcite = API.sqlCalcite(queries[2]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  test("Q3", function() {
    var resultTruffle = API.sql(queriesDynamicTables[3]);
    var resultCalcite = API.sqlCalcite(queries[3]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  test("Q4", function() {
    var resultTruffle = API.sql(queriesDynamicTables[4]);
    var resultCalcite = API.sqlCalcite(queries[4]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  test("Q5", function() {
    var resultTruffle = API.sql(queriesDynamicTables[5]);
    var resultCalcite = API.sqlCalcite(queries[5]);
    expect(resultTruffle).toMatchCloseTo(resultCalcite, 3);
  });

  test("Q6", function() {
    var resultTruffle = API.sql(queriesDynamicTables[6]);
    var resultCalcite = API.sqlCalcite(queries[6]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  test("Q7", function() {
    var resultTruffle = API.sql(queriesDynamicTables[7]);
    var resultCalcite = API.sqlCalcite(queries[7]);
    expect(resultTruffle).toMatchCloseTo(resultCalcite, 3);
  });

});
