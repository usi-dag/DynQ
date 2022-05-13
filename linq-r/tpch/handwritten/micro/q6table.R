
d <- as.Date('1995-12-01')

query <- function(tpch) {
    lineitem <- tpch[['lineitem']]
    first(lineitem[lineitem$l_shipdate >= d, l_extendedprice * l_discount], 1000)
}