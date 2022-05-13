if(!require("nanotime")) {
    install.packages("nanotime")
    require("nanotime")
}

source('linq-r/tpch/common.R')
source('./linq-r/tpch/load_tpch.R')

args = commandArgs(trailingOnly=TRUE)
preload = length(args) == 1 && args[1] == 'preload'

con = importDuckDB(preload)
options(digits.secs = 6)

#allQueriesTimes = c()
allQueriesTimes = data.frame(matrix(ncol=1, nrow=0))
allQueriesSummaries = data.frame(matrix(ncol=6, nrow=0))
colnames(allQueriesTimes) <- c("times")
colnames(allQueriesSummaries) <- names(summary(c(Inf)))

options(digits.secs = 9)
options(digits = 20)


for(q in 1:tot_queries) {
    print(paste('query', q))
    times = rep(NA, nIterBench)
    query = readQueryDuck(q)
    for(i in 1:iter) {
        start = Sys.time()
        res = dbGetQuery(con, query)
        end = Sys.time()
        if(i >= nIterWarmup) {
            #took = as.numeric(end - start, units="secs") * 1000 # milliseconds
            # improve precision
            s = nanotime(start)
            e = nanotime(end)
            took = as.numeric(e - s) / (1000*1000) # milliseconds
            times[i - nIterWarmup] = took
        }
    }
    allQueriesSummaries = rbind(allQueriesSummaries, summary(times))
    allQueriesTimes = rbind(allQueriesTimes, c(times))
    print(summary(times))
}

write.csv(data.frame(allQueriesSummaries), file = if(preload) "./duckdb_preload_results.csv" else "./duckdb_results.csv")
write.csv(data.frame(allQueriesTimes), row.names = FALSE, file = if(preload) "./duckdb_preload_results_rawtimes.csv" else "./duckdb_results_rawtimes.csv")