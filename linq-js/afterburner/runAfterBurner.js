const utils = require('./utils');
utils.init(utils.DATA_PATH);

const show = require('../warmup.js').show;

function run(q) {
    const func = utils.genAfterBurnerFunction(q);
    show(`Q${q}`, func, utils.WARMUP_ITERS, utils.BENCH_ITERS);
}

if(process.argv.length > 2) {
    run(parseInt(process.argv[2]));
} else {
    for (let i = 1; i < 23; i++) {
        run(i);
    }
}
