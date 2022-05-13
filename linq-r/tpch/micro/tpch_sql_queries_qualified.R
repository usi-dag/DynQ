
# filter.count
Q1 = "SELECT COUNT(*) as res FROM lineitem WHERE l_shipdate >= DATE '1995-12-01'"

#filter.sum
Q2 = "SELECT SUM(l_discount * l_extendedprice) as res FROM lineitem WHERE l_shipdate >= DATE '1995-12-01'"

# filter.filter.sum
Q3 = "SELECT SUM(l_discount * l_extendedprice) as res FROM lineitem
WHERE (l_shipdate >= DATE '1995-12-01') AND (l_shipdate < DATE '1997-01-01')"

# filter.map
Q4 = "SELECT l_discount * l_extendedprice as res FROM lineitem WHERE l_shipdate >= DATE '1995-12-01'"

# filter.sort.take
Q5 = "SELECT l_extendedprice FROM lineitem
WHERE l_shipdate >= DATE '1995-12-01'
ORDER BY l_orderkey, l_extendedprice LIMIT 1000"

# filter.map.take
Q6 = "SELECT l_discount * l_extendedprice as res FROM lineitem WHERE l_shipdate >= DATE '1995-12-01' LIMIT 1000"

# filter.XJoin(filter).sum
Q7 = "SELECT SUM(orders.o_totalprice) as res
FROM lineitem, orders
WHERE orders.o_orderdate >= DATE '1995-12-01'
AND lineitem.l_shipdate >= DATE '1995-12-01'
AND orders.o_orderkey = lineitem.l_orderkey"

sql_queries = c(NULL, Q1, Q2, Q3, Q4, Q5, Q6, Q7)

