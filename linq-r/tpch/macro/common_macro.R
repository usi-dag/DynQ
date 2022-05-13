
readQueryDuck = function(n) {
    sourceDir = getSrcDirectory(function(dummy) {dummy})
    useOptQuery = Sys.getenv("DYNQ_R_TPCH_USE_OPT_QUERIES", "false") == "true"
    if(useOptQuery) {
        filename = paste0(sourceDir, 'tpch_queries_duck/optimized/q', sprintf("%02d", n), '.sql')
    } else {
        filename = paste0(sourceDir, 'tpch_queries_duck/q', sprintf("%02d", n), '.sql')
    }
    paste(readLines(filename), collapse=" ")
}

readQueryTruffleLINQ = function(n) {
    source('./linq-r/tpch/macro/tpch_sql_queries_qualified.R')
    reorderJoin = Sys.getenv("DYNQ_REORDER_JOINS", "true")
    java.type('java.lang.System')$setProperty("DYNQ_REORDER_JOINS", reorderJoin)
    source('./linq-r/tpch/heuristic_join_fix.R')
    if(n %in% notUseHeuristicJoinOrder) {
        forceNotUseHeuristicJoinOrder()
    } else {
        restoreDefaultHeuristicJoinOrder()
    }
    if(n %in% notUseHeuristicJoinBushyOrder) {
        forceNotUseHeuristicJoinBushyOrder()
    } else {
        restoreDefaultHeuristicJoinBushyOrder()
    }
    sql_queries[n]
}


readQueryTable = function(n) {
    source(paste('./linq-r/tpch/handwritten/q', nquery, 'table.R', sep=''))
}


readQueryDataFrame = function(n) {
    source(paste('./linq-r/tpch/handwritten/q', nquery, '.R', sep=''))
}


tot_queries = 22
