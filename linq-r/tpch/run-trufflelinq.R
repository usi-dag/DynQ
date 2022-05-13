source('linq-r/tpch/common.R')


bench_mode = Sys.getenv("DYNQ_BENCH_MODE") == TRUE

# Load Data
loadTPCHData()

importTruffleLINQ()

allQueriesTimes = data.frame(matrix(ncol=1, nrow=0))
allQueriesSummaries = data.frame(matrix(ncol=6, nrow=0))
colnames(allQueriesTimes) <- c("times")
colnames(allQueriesSummaries) <- names(summary(c(Inf)))

options(digits.secs = 9)
options(digits = 20)

DYNQ_PREPARE_QUERY_API = Sys.getenv("DYNQ_PREPARE_QUERY_API", "prepare");
prepare = function(query) {
    if(DYNQ_PREPARE_QUERY_API == "prepare") {
        API$prepare(query)
    } else {
        API$parseQuery(query)
    }
}

javaNano = java.type('java.lang.System')$nanoTime

for(q in 1:tot_queries) {
    print(paste('query', q))
    if(q %in% skip) {
        allQueriesSummaries = rbind(allQueriesTimes, summary(c(Inf)))
        allQueriesTimes = rbind(allQueriesTimes, c())
    } else {
        times = rep(NA, nIterBench)
        query = readQueryTruffleLINQ(q)
        func = prepare(query)
        for(i in 1:iter) {
            runGC();
            start = javaNano() #Sys.time()
            res = func()
            end = javaNano() #Sys.time()
            if(i >= nIterWarmup) {
                #note: does not work in interop...
                #s = nanotime(start)
                #e = nanotime(end)
                #took = as.numeric(e - s) / (1000*1000) # milliseconds

                # just use java nanotime
                took = (end - start) / (1000*1000) # milliseconds
                times[i - nIterWarmup] = took
            }
        }
        allQueriesSummaries = rbind(allQueriesSummaries, summary(times))
        allQueriesTimes = rbind(allQueriesTimes, c(times))
        print(summary(times))
    }
}

write.csv(allQueriesSummaries, file = "./trufflelinq_compiled_results.csv")
write.csv(allQueriesTimes, row.names = FALSE, file = "./trufflelinq_compiled_results_rawtimes.csv")

