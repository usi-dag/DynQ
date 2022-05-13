
const API = require('../truffleLinq').API();

// TPC-H Utils
const testDataPath = process.env.DYNQ_TPCH_TEST_DATA || "../data/tpch/test/";
const load_tpch = require('../tpch/load_tpch.js');
const queries = require('../tpch/tpch_sql_queries');

// Load Data
function loadData() {
  return load_tpch.load(testDataPath).then(function (tpch) {
    for(var table in tpch) {
      API.session.registerTable(table, tpch[table], 'schema');
      // API.session.registerTable("dynamic_" + table, tpch[table], 'dynamic');
    }
  });
}

jest.setTimeout(120000); // 2 mins max data loading

beforeAll(loadData);

// import {toBeDeepCloseTo,toMatchCloseTo} from 'jest-matcher-deep-close-to';
const deepClose = require('jest-matcher-deep-close-to');
const toBeDeepCloseTo = deepClose.toBeDeepCloseTo;
const toMatchCloseTo = deepClose.toMatchCloseTo;
expect.extend({toBeDeepCloseTo, toMatchCloseTo});


// It should be simply like this, but it does not work... Why?!?

// const qs = [1,3,4,6,9,10,11,12,13,14,15,16,17,18,19,21,22];
/*
const qs = [1,3,6,9,10,11,12,14,15,18];
test.each(qs)('Q%i', (i) => {
  // var query = queries[i];
  var resultTruffle = API.sql(queries[i]);
  var resultCalcite = API.sqlCalcite(queries[i]);
  expect(resultTruffle).toEqual(resultCalcite);
});
*/


const ifRun = require('./conditional-test').ifRunTPCH

describe("TPC-H Tests", function () {

  ifRun(1)("Q1", function() {
    var query = queries[1];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(2)("Q2", function() {
    var query = queries[2];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(3)("Q3", function() {
    var query = queries[3];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(4)("Q4", function() {
    var query = queries[4];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(5)("Q5", function() {
    var query = queries[5];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toMatchCloseTo(resultCalcite, 3);
  });

  ifRun(6)("Q6", function() {
    var query = queries[6];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(7)("Q7", function() {
    var query = queries[7];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toMatchCloseTo(resultCalcite, 3);
  });

  ifRun(8)("Q8", function() {
    var query = queries[8];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(9)("Q9", function() {
    var query = queries[9];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toMatchCloseTo(resultCalcite, 3);
    // expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(10)("Q10", function() {
    var query = queries[10];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(11)("Q11", function() {
    var query = queries[11];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(12)("Q12", function() {
    var query = queries[12];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(13)("Q13", function() {
    var query = queries[13];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(14)("Q14", function() {
    var query = queries[14];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toBeDeepCloseTo(resultCalcite);
  });

  ifRun(15)("Q15", function() {
    var query = queries[15];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(16)("Q16", function() {
    var query = queries[16];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(17)("Q17", function() {
    var query = queries[17];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(18)("Q18", function() {
    var query = queries[18];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(19)("Q19", function() {
    var query = queries[19];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(20)("Q20", function() {
    var query = queries[20];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(21)("Q21", function() {
    var query = queries[21];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toEqual(resultCalcite);
  });

  ifRun(22)("Q22", function() {
    var query = queries[22];
    var resultTruffle = API.sql(query);
    var resultCalcite = API.sqlCalcite(query);
    expect(resultTruffle).toBeDeepCloseTo(resultCalcite);
  });

});
