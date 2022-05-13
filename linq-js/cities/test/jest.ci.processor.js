const fs = require('fs');
const path = require('path');

const tapFile = process.env.DYNQ_TEST_TAP_FILENAME || "tap-results/test-results.tap";

module.exports = (result) => {
    if (false) { // process.env.CI
        const text = [
            `1..${result.numTotalTests}`
        ];
        const tests = result.testResults
            .map(file => file.testResults)
    .reduce((memo, test) => [...memo, ...test], []);

        tests.forEach((test, idx) => {
            if (test.status === 'passed') {
            text.push(`ok ${idx + 1} ${test.fullName}`);
        } else if (test.status === 'failed') {
            text.push(`not ok ${idx + 1} ${test.fullName}`);
            // text.push(test.failureMessages.join('\n'));
        } else if (test.status === 'pending') {
            text.push(`ok ${idx + 1} ${test.fullName} # SKIP -`);
        }
    });

        text.push(`# tests ${result.numTotalTests}`);
        text.push(`# pass ${result.numPassedTests}`);
        text.push(`# fail ${result.numFailedTests}`);
        try {
            fs.writeFileSync(tapFile, text.join('\n'));
        } catch (err) {
            console.log('Failed to write tap results', err.stack);
        }

    }
    return result;
};