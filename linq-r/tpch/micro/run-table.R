
if(!require("nanotime")) {
    install.packages("nanotime")
    require("nanotime")
}

source('linq-r/tpch/common.R')
source('linq-r/tpch/micro/common_micro.R')
source('./linq-r/tpch/load_tpch.R')

importDataTable()

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
    func = readQueryTable(q)
    for(i in 1:iter) {
        start = Sys.time()
        res = func(tpch_table)
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
    allQueriesTimes = rbind(allQueriesTimes, times)
    print(summary(times))
}

write.csv(data.frame(allQueriesSummaries), file = "./table_results.micro.csv")
write.csv(data.frame(allQueriesTimes), row.names = FALSE, file = "./table_results_rawtimes.micro.csv")