const NS_PER_SEC = 1e9;

const benchMode = process.env.DYNQ_BENCH_MODE == 'true';

function execN(exec, nWarmup, nProfiled) {
    if(!benchMode) {
        console.log("exec warmup: ", nWarmup, "bench", nProfiled);
    }
    var result;
    const times = [];
    for (var i = 0; i < nWarmup; i++) {
        const newRes = exec();
        result = newRes;
    }
    if(!benchMode) {
        console.log('Warmup finished');
    }

    for (var i = 0; i < nProfiled; i++) {
        var hrstart = process.hrtime()
        const newRes = exec();
        var hrend = process.hrtime(hrstart)
        var end = (hrend[0] * NS_PER_SEC + hrend[1]) / 1e6; // millisecond
        result = newRes;
        times.push(end);
    }
    return [result, times]
}

function warmup(exec, hint) {
    var currentTime = 0;
}

function show(msg, exec, nWarmup, nProfiled) {
    if(nWarmup == 0 && nProfiled == 1) {
        show1(msg, exec);
        return;
    }
    const [result, executionTimes] = execN(exec, nWarmup, nProfiled);
    const [avg, stDev] = averageAndStandardDeviation(executionTimes);
    if(benchMode) {
        console.log(`${msg},${avg},${stDev}`);
    } else {
        console.log(msg);
        console.log("Result:", result, "AVG:", avg+'ms', "STD(%):", stDev);
        console.log();
    }
}

function show1(msg, exec) {
    const start = new Date();
    const result = exec();
    const end = new Date() - start;
    if(benchMode) {
        console.log(`${msg},${end}`);
    } else {
        console.log(msg);
        console.log("Result: ", result);
        console.log("Time: ", end);
        console.log();
    }
}

function average(arr) {
    var sum = 0;
    for (var i = 0; i < arr.length; i++) {
        sum += arr[i];
    }
    return sum / arr.length;
}

function averageAndStandardDeviation(values){
    const avg = average(values);
    const avgSquareDiff = average(values.map(val => (val - avg)**2));
    const stDev = Math.sqrt(avgSquareDiff / (values.length - 1));
    // console.log(stDev, stDev / avg * 100)
    // const avgDiff = average(values.map(val => Math.abs(val - avg)));
    // const stdDev = Math.sqrt(avgSquareDiff);
    const relStDev = stDev / avg * 100;
    return [avg.toFixed(6), relStDev.toFixed(6)];
}

module.exports.execN = execN;
module.exports.show = show;
module.exports.show1 = show1;
