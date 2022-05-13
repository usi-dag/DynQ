
// Trick for force not to use heuristic join order

const notUseHeuristicJoinOrder = [5, 10, 16]
const defaultUseHeuristicJoinOrder = process.env["DYNQ_USE_HEURISTIC_JOIN_ORDER"]

const defaultUseHeuristicJoinBushyOrder = process.env["DYNQ_USE_BUSHY"]
const notUseHeuristicJoinBushyOrder = [9]

function forceNotUseHeuristicJoinOrder() {
    Java.type('java.lang.System').setProperty("DYNQ_USE_HEURISTIC_JOIN_ORDER", "false")
}

function restoreDefaultHeuristicJoinOrder() {
    Java.type('java.lang.System').setProperty("DYNQ_USE_HEURISTIC_JOIN_ORDER", defaultUseHeuristicJoinOrder)
}

function forceNotUseHeuristicJoinBushyOrder() {
    Java.type('java.lang.System').setProperty("DYNQ_USE_BUSHY", "false")
}
function restoreDefaultHeuristicJoinBushyOrder() {
    Java.type('java.lang.System').setProperty("DYNQ_USE_BUSHY", defaultUseHeuristicJoinBushyOrder)
}
function restoreAllDefault() {
    restoreDefaultHeuristicJoinOrder();
    restoreDefaultHeuristicJoinBushyOrder();
}

module.exports.fixHeuristicJoin = function (q) {
    if(notUseHeuristicJoinOrder.indexOf(q) != -1) {
        forceNotUseHeuristicJoinOrder();
    } else {
        restoreDefaultHeuristicJoinOrder();
    }
    if(notUseHeuristicJoinBushyOrder.indexOf(q) != -1) {
        forceNotUseHeuristicJoinBushyOrder();
    } else {
        restoreDefaultHeuristicJoinBushyOrder();
    }
}

module.exports.restoreAllDefault = restoreAllDefault;