
const API = require('../truffleLinq').API();

// TPC-H Utils
const testDataPath = process.env.DYNQ_TPCH_TEST_DATA || "../data/tpch/test/";
const load_tpch = require('../tpch/load_tpch.js');
const queries = require('../tpch/tpch_sql_queries');

// Load Data
function loadData() {
  return load_tpch.load(testDataPath).then(function (tpch) {
    for(var table in tpch) {
      API.session.registerTable(table, tpch[table], "schema");
      API.session.registerTable("dynamic_" + table, tpch[table], 'dynamic');
    }
  });
}

jest.setTimeout(120000); // 2 mins max data loading

beforeAll(loadData);

const queriesDynamicTables = require('../tpch/tpch_sql_queries_qualified').withPrefix('dynamic');

// import {toBeDeepCloseTo,toMatchCloseTo} from 'jest-matcher-deep-close-to';
const deepClose = require('jest-matcher-deep-close-to');
const toBeDeepCloseTo = deepClose.toBeDeepCloseTo;
const toMatchCloseTo = deepClose.toMatchCloseTo;
expect.extend({toBeDeepCloseTo, toMatchCloseTo});

const ifRun = require('./conditional-test').ifRunTPCH

describe("TPC-H Tests (dynamic tables)", function () {

  ifRun(1)("Q1", function() {
    var resultTruffle = API.sql(queriesDynamicTables[1]);
    var resultCalcite = API.sqlCalcite(queries[1]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(2)("Q2", function() {
    var resultTruffle = API.sql(queriesDynamicTables[2]);
    var resultCalcite = API.sqlCalcite(queries[2]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(3)("Q3", function() {
    var resultTruffle = API.sql(queriesDynamicTables[3]);
    var resultCalcite = API.sqlCalcite(queries[3]);
    expect(resultTruffle).toBeSameResultSetWithDateStrings(resultCalcite);
  });

  ifRun(4)("Q4", function() {
    var resultTruffle = API.sql(queriesDynamicTables[4]);
    var resultCalcite = API.sqlCalcite(queries[4]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(5)("Q5", function() {
    var resultTruffle = API.sql(queriesDynamicTables[5]);
    var resultCalcite = API.sqlCalcite(queries[5]);
    expect(resultTruffle).toMatchCloseTo(resultCalcite, 3);
  });

  ifRun(6)("Q6", function() {
    var resultTruffle = API.sql(queriesDynamicTables[6]);
    var resultCalcite = API.sqlCalcite(queries[6]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(7)("Q7", function() {
    var resultTruffle = API.sql(queriesDynamicTables[7]);
    var resultCalcite = API.sqlCalcite(queries[7]);
    expect(resultTruffle).toMatchCloseTo(resultCalcite, 3);
  });

  ifRun(8)("Q8", function() {
    var resultTruffle = API.sql(queriesDynamicTables[8]);
    var resultCalcite = API.sqlCalcite(queries[8]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(9)("Q9", function() {
    var resultTruffle = API.sql(queriesDynamicTables[9]);
    var resultCalcite = API.sqlCalcite(queries[9]);
    expect(resultTruffle).toMatchCloseTo(resultCalcite, 3);
  });

  ifRun(10)("Q10", function() {
    var resultTruffle = API.sql(queriesDynamicTables[10]);
    var resultCalcite = API.sqlCalcite(queries[10]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(11)("Q11", function() {
    var resultTruffle = API.sql(queriesDynamicTables[11]);
    var resultCalcite = API.sqlCalcite(queries[11]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(12)("Q12", function() {
    var resultTruffle = API.sql(queriesDynamicTables[12]);
    var resultCalcite = API.sqlCalcite(queries[12]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(13)("Q13", function() {
    var resultTruffle = API.sql(queriesDynamicTables[13]);
    var resultCalcite = API.sqlCalcite(queries[13]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(14)("Q14", function() {
    var resultTruffle = API.sql(queriesDynamicTables[14]);
    var resultCalcite = API.sqlCalcite(queries[14]);
    expect(resultTruffle).toBeDeepCloseTo(resultCalcite);
  });

  ifRun(15)("Q15", function() {
    var resultTruffle = API.sql(queriesDynamicTables[15]);
    var resultCalcite = API.sqlCalcite(queries[15]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(16)("Q16", function() {
    var resultTruffle = API.sql(queriesDynamicTables[16]);
    var resultCalcite = API.sqlCalcite(queries[16]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(17)("Q17", function() {
    var resultTruffle = API.sql(queriesDynamicTables[17]);
    var resultCalcite = API.sqlCalcite(queries[17]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(18)("Q18", function() {
    var resultTruffle = API.sql(queriesDynamicTables[18]);
    var resultCalcite = API.sqlCalcite(queries[18]);
    // expect(resultTruffle).toEqual(resultCalcite);
    // NOTE: currently dynq may return JS dates, Calcite always return string
    // TODO: fix
    expect(resultTruffle.length).toEqual(resultCalcite.length);
    // fields: "c_name","c_custkey","o_orderkey","o_orderdate","o_totalprice","sum_qty"
    const fields = ["c_name","c_custkey","o_orderkey","o_orderdate","o_totalprice","sum_qty"]
    var l = resultCalcite.length;
    for (let i = 0; i < l; i++) {
      trow = resultTruffle[i];
      crow = resultCalcite[i];
      for (const field of fields) {
        tfield = trow[field];
        cfield = crow[field];
        if(tfield instanceof Date) { //  && cfield instanceof String
          expect(tfield).toEqual(new Date(cfield));
        } else {
          expect(tfield).toEqual(cfield);
        }
      }
    }
  });

  ifRun(19)("Q19", function() {
    var resultTruffle = API.sql(queriesDynamicTables[19]);
    var resultCalcite = API.sqlCalcite(queries[19]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(20)("Q20", function() {
    var resultTruffle = API.sql(queriesDynamicTables[20]);
    var resultCalcite = API.sqlCalcite(queries[20]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(21)("Q21", function() {
    var resultTruffle = API.sql(queriesDynamicTables[21]);
    var resultCalcite = API.sqlCalcite(queries[21]);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(22)("Q22", function() {
    var resultTruffle = API.sql(queriesDynamicTables[22]);
    var resultCalcite = API.sqlCalcite(queries[22]);
    expect(resultTruffle).toBeDeepCloseTo(resultCalcite);
  });

});
