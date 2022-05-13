
readQueryDuck = function(n) {
    sourceDir = getSrcDirectory(function(dummy) {dummy})
    filename = paste0(sourceDir, 'tpch_queries_duck/micro/q', n, '.sql')
    paste(readLines(filename), collapse=" ")
}

readQueryTruffleLINQ = function(n) {
    source('./linq-r/tpch/micro/tpch_sql_queries_qualified.R')
    sql_queries[n]
}

readQueryTable = function(n) {
    source(paste('./linq-r/tpch/handwritten/micro/q', n, 'table.R', sep=''))
    query
}


readQueryDataFrame = function(n) {
    source(paste('./linq-r/tpch/handwritten/micro/q', n, '.R', sep=''))
}

tot_queries = 7
