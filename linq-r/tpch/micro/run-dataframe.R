
source('linq-r/tpch/micro/common_micro.R')
source('linq-r/tpch/common.R')
source('./linq-r/tpch/load_tpch.R')

args = commandArgs(trailingOnly=TRUE)
preload = length(args) == 1 && args[1] == 'preload'

con = importDuckDB(preload)

allQueriesTimes = c()

for(q in 1:tot_queries) {
    times = rep(NA, nIterBench)
    query = readQueryDuck(q)
    for(i in 1:iter) {
        start = Sys.time()
        res = dbGetQuery(con, query)
        end = Sys.time()
        if(i >= nIterWarmup) {
            took = as.numeric(end - start, units="secs") * 1000 # milliseconds
            times[i - nIterWarmup] = took
        }
    }
    allQueriesTimes = rbind(allQueriesTimes, summary(times))
    print(summary(times))
}

write.csv(data.frame(allQueriesTimes), file = if(preload) "./duckdb_preload_results.csv" else "./duckdb_results.csv")
