
d <- as.Date('1995-12-01')

query <- function(tpch) {
    lineitem <- tpch[['lineitem']]
    orders <- tpch[['orders']]

    l = lineitem[lineitem$l_shipdate >= d, 'l_orderkey']
    o = orders[orders$o_orderdate >= d, c('o_orderkey', 'o_totalprice')]
    l[o, sum(o_totalprice), on=c(l_orderkey='o_orderkey')]
}