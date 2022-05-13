
bench_mode = Sys.getenv("DYNQ_BENCH_MODE") == "true"

if(!startsWith(version[['version.string']], 'FastR')) {
    if(!require(fst)) {
        install.packages("fst")
        library(fst)
    }
} else {
    libraryLoc = getGraalLibraryLoc()
    if(!require(fst, lib.loc=libraryLoc)) {
        fastr.setToolchain("llvm")
        install.packages("fst", lib=libraryLoc)
        library(fst, lib.loc=libraryLoc)
    }
}

source("linq-r/tpch/load_tpch_schema.R")

fixNumColClasses <- function(cols) {
    if(!startsWith(version[['version.string']], 'FastR')) {
        append(cols, 'character')
    } else {
        cols
    }
}
fixNumCols <- function(cols, prefix) {
    if(!startsWith(version[['version.string']], 'FastR')) {
        append(cols, paste0(prefix, 'character'))
    } else {
        cols
    }
}

importCsvTable <- function(name, prefix) {
    data <- read.csv(
      paste0(path, '/', name, '.tbl'),
      sep='|',
      header=FALSE,
      comment.char = "",
      col.names=fixNumCols(colnames[[name]], prefix),
      colClasses=fixNumColClasses(colclasses[[name]]))
    if(!startsWith(version[['version.string']], 'FastR')) {
        remove_col <- names(data) %in% c(paste0(prefix, 'character'))
        data[,!remove_col]
    } else {
        data
    }
}

importTable <- function(name, prefix) {
    fstPath = paste0(path, '/',  name, '.fst')
    if(!file.exists(fstPath)) {
        df = importCsvTable(name, prefix)
        write.fst(df, fstPath, compress=0)
        df
    } else {
        read.fst(fstPath)
    }
}

if(!bench_mode) { start_load = proc.time() }
nation=importTable('nation', 'n_')
region=importTable('region', 'r_')
part=importTable('part', 'p_')
supplier=importTable('supplier', 's_')
partsupp=importTable('partsupp', 'ps_')
customer=importTable('customer', 'c_')
orders=importTable('orders', 'o_')
lineitem=importTable('lineitem', 'l_')
if(!bench_mode) {
    end = proc.time() - start_load
    print('load data took: ')
    print(end)
}

tpch <- list(
  nation=nation,
  region=region,
  part=part,
  supplier=supplier,
  partsupp=partsupp,
  customer=customer,
  orders=orders,
  lineitem=lineitem
)
