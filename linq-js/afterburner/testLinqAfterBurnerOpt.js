const utils = require('./utils');
utils.init(utils.DATA_PATH);

const API = utils.initDynQ();

var fs = require('fs');
var dir = utils.DATA_PATH + '/expected';
if (!fs.existsSync(dir)){
    fs.mkdirSync(dir);
}

function check(i, func, resultFile) {
    console.log('test', i);
    const result = func();
    const expected = JSON.parse(fs.readFileSync(resultFile));

    if(result.length != expected.length) {
        console.error('WRONG RESULT', i, 'diff len, got', result.length, 'expected', expected.length);
        console.error('GOT RESULT', result)
        return;
    }
    for (let j = 0; j < result.length; j++) {
        const resRow = result[j];
        const expectedRow = expected[j];
        const keys = Object.keys(expectedRow);
        for(key of keys) {
            if(resRow[key] != expectedRow[key]) {
                if(typeof resRow[key] == 'number' && typeof expectedRow[key] == 'number') {
                    if(resRow[key].toFixed(2) != expectedRow[key].toFixed(2)) {
                        console.error('WRONG RESULT', i, 'diff value', key, 'got', resRow[key], 'expected', expectedRow[key]);
                        return;
                    }
                } else {
                    console.error('WRONG RESULT', i, 'diff value', key, 'got', resRow[key], 'expected', expectedRow[key]);
                    return;
                }
            }
        }
    }
}


if(process.argv.length > 2) {
    const i = parseInt(process.argv[2]);
    var func = utils.getDynQFunction(API, i);
    check(i, func, `${dir}/result${i}.json`);
} else {
    console.log('Macro (TPC-H)');
    for (let i = 1; i < 23; i++) {
        var func = utils.getDynQFunction(API, i);
        check(i, func, `${dir}/result${i}.json`);
    }
    console.log('Micro (TPC-H)');
    Java.type('java.lang.System').setProperty("DYNQ_REORDER_JOINS", "false")
    for (let i = 1; i < 8; i++) {
        var func = utils.getDynQFunctionMicro(API, i);
        check(i, func, `${dir}/result_micro${i}.json`);
    }
}