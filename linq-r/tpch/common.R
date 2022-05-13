
if(Sys.getenv("R_LIBS_USER") != "") {
    .libPaths(Sys.getenv("R_LIBS_USER"))  # add to the path
}


importDuckDB = function (preload) {
    if(!require(DBI)) {
        install.packages("DBI")
        require(DBI)
    }
    if(!require(duckdb)) {
        packageurl <- "https://cran.r-project.org/src/contrib/Archive/duckdb/duckdb_0.3.0.tar.gz"
        install.packages(packageurl, repos=NULL, type="source")
        require(duckdb)
    }
    con = dbConnect(duckdb::duckdb(), dbdir=":memory:", read_only=FALSE)


    register = if(preload) function(name, table) {
        dbWriteTable(con, name, table)
    } else function(name, table) {
        duckdb::duckdb_register(con, name, table)
    }

    start = Sys.time()
    register("nation", nation)
    register("region", region)
    register("part", part)
    register("supplier", supplier)
    register("partsupp", partsupp)
    register("customer", customer)
    register("orders", orders)
    register("lineitem", lineitem)
    end = Sys.time()
    took = as.numeric(end - start, units="secs") * 1000 # milliseconds
    print(sprintf('duckdb registration took: %f', took))

    con
}

getGraalLibraryLoc = function() {
    # TODO if unset it should install in default R package folder?
    libFolder = Sys.getenv("DYNQ_R_LIB_FOLDER", '..')
    whichJava = java.type("java.lang.System")$getProperty("java.version")
    # Version 1.8.0_251: GraalVM EE 20.1
    # Version 1.8.0_252: GraalVM CE 20.1
    # Version 1.8.0_272: GraalVM CE 20.3
    switch(whichJava,
           "1.8.0_251"=paste0(libFolder, '/R-packages-llvm-graalEE'),
           "1.8.0_252"=paste0(libFolder, '/R-packages-llvm-graalCE'),
           "1.8.0_272"=paste0(libFolder, '/R-packages-llvm-graalCE-20.3'))
}

importDataTable = function () {
    if(startsWith(version[['version.string']], 'FastR')) {
        libraryLoc = getGraalLibraryLoc()
        if(!require(data.table, lib.loc=libraryLoc)) {
            install.fastr.packages("data.table", lib=libraryLoc)
            library("data.table", lib.loc=libraryLoc)
        }
    }
    else {
        if(!require(data.table)) {
            install.packages("data.table")
            library("data.table")
        }
    }
    setDTthreads(1)
    lineitem = data.table(tpch[['lineitem']])
    orders = data.table(tpch[['orders']])
    tpch_table <<- list(lineitem=lineitem, orders=orders)
}

importTruffleLINQ = function() {
    truffleLINQ = Sys.getenv("DYNQ_LANGUAGE", 'TruffleLINQ')
    API <<- eval.polyglot(truffleLINQ, 'API')
    tableType = Sys.getenv("DYNQ_R_TABLETYPE", 'schema')
    register = function(table) {
        API$session$registerTable(table, tpch[[table]], tableType)
    }

    # Register data (with loading if internalLoad is true)
    if(!bench_mode) { start_load = proc.time() }
    register("nation")
    register("region")
    register("part")
    register("supplier")
    register("partsupp")
    register("customer")
    register("orders")
    register("lineitem")
    if(!bench_mode) {
        end = proc.time() - start_load
        print('load data took: ')
        print(end)
    }
    runGC <<- if(Sys.getenv("DYNQ_GC_EACH_ITER") == 'true') { API$gc(); }
    else { function(){} }
}

loadTPCHData = function() {
    source('./linq-r/tpch/load_tpch.R')
}

envToIntWithDefault = function(envVar, default) {
    varValue = Sys.getenv(envVar, default)
    res = strtoi(varValue)
    if(is.na(res)) 0 else res
}


getImplementation = function(nquery, impl) {
    if(impl == 'table') {
        importDataTable()
        query = readQueryTable(nquery)
        func = function() { query(tpch_table) }
    } else if (startsWith(impl, 'duckdb')) {
        con = importDuckDB(endsWith(impl, 'preload'))
        q = readQueryDuck(nquery)
        func = function() { dbGetQuery(con, q) }
    } else {
        importTruffleLINQ()
        q = readQueryTruffleLINQ(nquery)
        DYNQ_PREPARE_QUERY_API = Sys.getenv("DYNQ_PREPARE_QUERY_API", "prepare");
        prepare = function(query) {
            if(DYNQ_PREPARE_QUERY_API == "prepare") {
                API$prepare(query)
            } else {
                API$parseQuery(query)
            }
        }
        func = prepare(q)
    }
    func
}

bench_mode = Sys.getenv("DYNQ_BENCH_MODE") == TRUE

runGC = function(){}

args = commandArgs(trailingOnly=TRUE)
nIterWarmup = envToIntWithDefault("DYNQ_WARMUP_ITER", 10)
nIterBench = envToIntWithDefault("DYNQ_BENCH_ITER", 20)
iter = nIterWarmup + nIterBench
