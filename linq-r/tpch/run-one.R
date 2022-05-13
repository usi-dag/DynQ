

source('linq-r/tpch/common.R')

if (length(args) != 2) {
    stop("requires two arguments (query, implementation)")
}
nquery = strtoi(args[1])
impl = args[2]

# Load Data
loadTPCHData()

func = getImplementation(nquery, impl)

options(digits=17, digits.secs = 6)

# Run
times = rep(NA, nIterBench)

for(i in 1:iter) {
    runGC()
    print(paste('start', i))
    start = Sys.time()#proc.time()
    res = func()
    end = Sys.time()#proc.time()
    print(paste('end', i))
    if(i == nIterWarmup) {
        print('Warmup done')
    }
    if(i >= nIterWarmup) {
        took = as.numeric(end - start, units="secs") * 1000 # milliseconds
        times[i - nIterWarmup] = took
    }
}
print(length(res))
print(res)
for(i in 1:min(20, length(res))) {
    print(res[i])
}
print(summary(times))
