
const API = require('../truffleLinq').API();


const foo = [
    {x: 1, obj: {a: 1, b: 2}, arr: [1, 2, 3]},
    {x: 1, obj: {a: 1, b: 2}, arr: [1, 2, 3]},
    {x: 2, obj: {a: 2, b: 3}, arr: [1, 2, 3]},
    {x: 10, obj: {a: 1, b: 2}, arr: [1, 2, 3]},
];

API.session.registerTable("foo", foo, "dynamic");

plus100 = x => x + 100;
API.session.registerUDF('myUDF', plus100);

gt1 = x => x > 1;
API.session.registerUDF('myPredicate', gt1);

describe("UDF Test (in memory)", function () {

    test("UDF that transform data", function() {
        var query = "select sum(myUDF(foo.x)) as res from foo";
        var result = API.sql(query);
        var expected = 0;
        for(var row of foo) {
            expected += plus100(row.x);
        }
        expect(result[0].res).toEqual(expected);
    });

    test("UDF predicate", function() {
        var query = "select count(*) as ctn from foo where myPredicate(foo.x)";
        var result = API.sql(query);
        var expected = 0;
        for(var row of foo) {
            if(gt1(row.x)) {
                expected++;
            }
        }
        expect(result[0].ctn).toEqual(expected);
    });

});