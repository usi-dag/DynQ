
const utils = require('./utils');
utils.init(utils.DATA_PATH);

res = utils.genAfterBurnerFunction(2, 'array2')();
for (let i = 0; i < res.length; i++) {
    if(i%8==0)console.log();
    console.log(i, res[i]);
}
