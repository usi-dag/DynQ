
const runLongTests = process.env.DYNQ_RUN_LONG_TESTS == "true";

const skipLongPlans = [2, 5, 7, 8, 13, 19]
function ifRunTPCH(i) {
    if(skipLongPlans.indexOf(i) > -1 && !runLongTests) {
        return it.skip;
    }
    return it;
}

module.exports.ifRunTPCH = ifRunTPCH;


expect.extend({
    toBeSameResultSetWithDateStrings(received, expected, extraMatchers = []) {
        const matcherName = 'toEqualWithDateStrings'
        const options = {
            comment: 'deep equality',
            isNot: this.isNot,
            promise: this.promise,
        }

        if(received.length != expected.length) {
            return {
                actual: received,
                expected: expected,
                message: `Different length: Expected=${expected.length}, Received:${received.length}`,
                name: matcherName,
                pass: false,
            }
        }

        if(received.length == 0) {
            return {
                actual: received,
                expected: expected,
                message: matcherHint(matcherName, undefined, undefined, options) +
                '\n\n' +
                `Expected: ${printExpected(expected)}\n` +
                `Received: ${printReceived(received)}`,
                name: matcherName,
                pass: false
            }
        }

        // assuming an array of tuples with fixed keys
        const keys = Object.keys(received[0]);
        for (let i = 0; i < received.length; i++) {
            for(var key of keys) {
                if(received[i][key] != expected[i][key]) {
                    // check if we received a date and we expect that date as string (from Calcite)
                    const r = received[i][key];
                    const e = expected[i][key];
                    if(r.getTime() != new Date(e).getTime()) {
                        const difference = `Expected: ${r}, Received: ${e}`;
                        return {
                            actual: received,
                            expected: expected,
                            message: matcherHint(matcherName, undefined, undefined, options) +
                             `\n\nDifference at index ${i}:\n\n${difference}`,
                            name: matcherName,
                            pass: pass
                        }
                    }
                }
            }
        }

        return {
            actual: received,
            expected,
            message: 'OK',
            name: matcherName,
            pass: true
        }
    },
})