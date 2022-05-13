console.log("A")
const API = require('./truffleLinq').API();

console.log("B")

const foo = [
    {x: 1, y: 2},
    {x: 1, y: 3},
    {x: 10, y: 20},
    {x: 100, y: 200},
];
const bar = [
    {z: 2, w: "str"},
    {z: 2, w: new Date("2000-01-01")},
    {z: 2},
    {x: 1, y: 3},
    {x: 10, y: 20},
    {x: 100, y: 200},
];


API.session.registerTable("foo", foo, "dynamic");
API.session.registerTable("bar", bar, "dynamic");
console.log("C")


var sql = "select count(*) from foo where x>1";
console.log(sql);
console.log(API.sql(sql));


var sql = "select bar.w from foo, bar where foo.y = bar.z";
console.log(sql);
console.log(API.sql(sql));


// UDFs

API.session.registerUDF('myUDF', x => x + 100);
var sql = "select sum(myUDF(foo.x)) as res from foo";
console.log(sql);
console.log(API.sql(sql));

API.session.registerUDF('myPredicate', x => x > 1);
var sql = "select count(*) from foo where myPredicate(foo.x)";
console.log(sql);
console.log(API.sql(sql));
