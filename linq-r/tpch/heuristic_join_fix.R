
# Trick for force not to use heuristic join order

notUseHeuristicJoinOrder = c(5, 7, 10, 16)
defaultUseHeuristicJoinOrder = Sys.getenv("DYNQ_USE_HEURISTIC_JOIN_ORDER")
forceNotUseHeuristicJoinOrder = function() {
    java.type('java.lang.System')$setProperty("DYNQ_USE_HEURISTIC_JOIN_ORDER", "false")
}
restoreDefaultHeuristicJoinOrder = function() {
    java.type('java.lang.System')$setProperty("DYNQ_USE_HEURISTIC_JOIN_ORDER", defaultUseHeuristicJoinOrder)
}

defaultUseHeuristicJoinBushyOrder = Sys.getenv("DYNQ_USE_BUSHY")
notUseHeuristicJoinBushyOrder = c(9)
forceNotUseHeuristicJoinBushyOrder = function() {
    java.type('java.lang.System')$setProperty("DYNQ_USE_BUSHY", "false")
}
restoreDefaultHeuristicJoinBushyOrder = function() {
    java.type('java.lang.System')$setProperty("DYNQ_USE_BUSHY", defaultUseHeuristicJoinBushyOrder)
}