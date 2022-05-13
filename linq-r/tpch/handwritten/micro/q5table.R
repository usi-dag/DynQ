
d <- as.Date('1995-12-01')

query <- function(tpch) {
    lineitem <- tpch[['lineitem']]
    first(lineitem[lineitem$l_shipdate >= d, ][order(l_orderkey, l_extendedprice), l_extendedprice], 1000)
}