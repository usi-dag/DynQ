const utils = require('./utils');
utils.init(utils.DATA_PATH);

var fs = require('fs');
var dir = utils.DATA_PATH + '/expected';
if (!fs.existsSync(dir)){
    fs.mkdirSync(dir);
}

function check(i, q, resultFile) {
    console.log('test', i);
    const result = q();
    const expected = JSON.parse(fs.readFileSync(resultFile));

    if(result.numrows != expected.length) {
        console.error('WRONG RESULT', i, 'diff len, got', result.numrows, 'expected', expected.length);
        // process.exit();
    }
    const columnIndexes = {};
    for (var j = 0; j < result.colnames.length; j++) {
        columnIndexes[result.colnames[j]] = j;
    }
    for (let j = 0; j < result.numrows; j++) {
        const resRow = result[j];
        const expectedRow = expected[j];
        const keys = Object.keys(expectedRow);
        const rowIndex = j * result.colnames.length;
        for(key of keys) {
            const columnIndex = columnIndexes[key];
            const columnResult = result.array2[rowIndex + columnIndex];
            const columnExpectedResult = result.coltypes[columnIndex] == 4 ? String.fromCharCode(expectedRow[key]) : expectedRow[key];
            if(columnResult != columnExpectedResult) {
                if(typeof columnResult == 'number' && typeof columnExpectedResult == 'number') {
                    const columnExpectedResultFloat = new Float32Array( [ columnExpectedResult ] )[ 0 ];
                    if(columnResult.toFixed(0) != columnExpectedResultFloat.toFixed(0)) {
                        console.error('WRONG RESULT FLOAT', i, 'diff value', key, 'got', columnResult, 'expected', columnExpectedResult);
                        // process.exit();
                    }
                } else {
                    console.error('WRONG RESULT', i, 'diff value', key, 'got', columnResult, 'expected', columnExpectedResult);
                    // process.exit();
                }
            }
        }
    }
}

if(process.argv.length > 2) {
    const i = parseInt(process.argv[2]);
    const func = utils.genAfterBurnerFunction(i, 'obj');
    check(i, func, `${dir}/result${i}.json`);
} else {
    console.log('Macro (TPC-H)');
    for (let i = 1; i < 23; i++) {
        const func = utils.genAfterBurnerFunction(i, 'obj');
        check(i, func, `${dir}/result${i}.json`);
    }
    console.log('Micro (TPC-H)');
    for (let i = 1; i < 8; i++) {
        const func = utils.genAfterBurnerFunctionMicro(i, 'obj');
        check(i, func, `${dir}/result_micro${i}.json`);
    }
}

