
d <- as.Date('1995-12-01')

query <- function(tpch) {
    lineitem <- tpch[['lineitem']]
    lineitem[lineitem$l_shipdate >= d, sum(l_extendedprice * l_discount)]
}