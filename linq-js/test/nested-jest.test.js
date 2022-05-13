
const API = require('../truffleLinq').API();


const foo = [
    {x: 1, obj: {a: 1, b: 2}, arr: [1, 2, 3]},
    {x: 1, obj: {a: 1, b: 2}, arr: [1, 2, 3]},
    {x: 2, obj: {a: 2, b: 3}, arr: [1, 2, 3]},
    {x: 10, obj: {a: 1, b: 2}, arr: [1, 2, 3]},
];

const bar = [
    {z: 2, w: "str"},
    {z: 2, w: new Date("2000-01-01")},
    {z: 2},
    {x: 1, y: 3},
    {x: 10, y: 20},
    {x: 100, y: 200, nest1: {nest2: {a: 1}}},
    {x: 100, y: 200, nest1: {nest2: {a: 2}}},
];

API.session.registerTable("foo", foo, "dynamic");
API.session.registerTable("bar", bar, "dynamic");


describe("Nested data (in memory)", function () {

    test("Accessing nested field", function() {
        var query = "select count(*) as ctn from foo where foo.obj['a']>1";
        var result = API.sql(query);
        expect(result[0].ctn).toEqual(1);
    });

    test("Accessing nested field and array elements", function() {
        var query = "select foo.arr[0] as arrayElement from foo where foo.obj['a'] = 2";
        var result = API.sql(query);
        expect(result[0].arrayElement).toEqual(1);
    });

    test("Accessing nested field (multiple levels)", function() {
        var query = "select count(*) as ctn from bar where bar.nest1['nest2']['a'] = 1";
        var result = API.sql(query);
        expect(result[0].ctn).toEqual(1);
    });

    test("Expressions with nested fields", function() {
        var query = "select count(*) as ctn from foo where foo.obj['a'] + foo.obj['b'] > foo.x";
        var result = API.sql(query);
        expect(result[0].ctn).toEqual(3);
    });

    test("Expressions with nested fields and array elements", function() {
        var query = "select count(*) as ctn from foo where foo.arr[0] + foo.arr[1] + foo.arr[2] > foo.x";
        var result = API.sql(query);
        expect(result[0].ctn).toEqual(3);
    });

});