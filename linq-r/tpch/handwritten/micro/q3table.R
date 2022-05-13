
dateFrom <- as.Date('1995-12-01')
dateTo <- as.Date('1997-01-01')

query <- function(tpch) {
    lineitem <- tpch[['lineitem']]
    lineitem[
        lineitem$l_shipdate >= dateFrom & lineitem$l_shipdate < dateTo,
        sum(l_extendedprice * l_discount)]
}