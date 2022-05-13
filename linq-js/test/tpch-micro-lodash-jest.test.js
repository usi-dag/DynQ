
const API = require('../truffleLinq').API();

// TPC-H Utils
const testDataPath = process.env.DYNQ_TPCH_TEST_DATA || "../data/tpch/test/";
const load_tpch = require('../tpch/load_tpch.js');
const queries = require('../tpch/micro/tpch_sql_queries_qualified').queries;

// Lodash Queries
// const _ = require('lodash');
// const lodashQueries = _.range(1, 7).map(q => require(`../tpch/micro/lodash/q${q}`));
const lodashQueries = [undefined];
for (var i = 1; i <= 7 ; i++) {
  lodashQueries[i] = require(`../tpch/micro/lodash/q${i}`);
}

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


describe("TPC-H Tests Micro Benchmark Queries (dynamic tables)", function () {

  test("Q1", function() {
    var resultLodAsh = lodashQueries[1](rawTables);
    var resultCalcite = API.sqlCalcite(queries[1]);
    expect(resultLodAsh).toBeDeepCloseTo(resultCalcite);
  });

  test("Q2", function() {
    var resultLodAsh = lodashQueries[2](rawTables);
    var resultCalcite = API.sqlCalcite(queries[2]);
    expect(resultLodAsh).toBeDeepCloseTo(resultCalcite);
  });

  test("Q3", function() {
    var resultLodAsh = lodashQueries[3](rawTables);
    var resultCalcite = API.sqlCalcite(queries[3]);
    expect(resultLodAsh).toBeDeepCloseTo(resultCalcite);
  });

  test("Q4", function() {
    var resultLodAsh = lodashQueries[4](rawTables);
    var resultCalcite = API.sqlCalcite(queries[4]);
    expect(resultLodAsh).toBeDeepCloseTo(resultCalcite);
  });

  test("Q5", function() {
    var resultLodAsh = lodashQueries[5](rawTables);
    var resultCalcite = API.sqlCalcite(queries[5]);
    expect(resultLodAsh).toMatchCloseTo(resultCalcite, 3);
  });

  test("Q6", function() {
    var resultLodAsh = lodashQueries[6](rawTables);
    var resultCalcite = API.sqlCalcite(queries[6]);
    expect(resultLodAsh).toBeDeepCloseTo(resultCalcite);
  });

  test("Q7", function() {
    var resultLodAsh = lodashQueries[7](rawTables);
    var resultCalcite = API.sqlCalcite(queries[7]);
    expect(resultLodAsh).toMatchCloseTo(resultCalcite, 3);
  });

});
