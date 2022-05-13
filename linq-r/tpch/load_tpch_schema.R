

path = Sys.getenv("DYNQ_TPCH_BENCH_DATA", "./data/tpch/sf0.01/")

lineitem_colnames = c('l_orderkey', 'l_partkey', 'l_suppkey', 'l_linenumber',
                      'l_quantity', 'l_extendedprice', 'l_discount', 'l_tax',
                      'l_returnflag', 'l_linestatus',
                      'l_shipdate', 'l_commitdate', 'l_receiptdate',
                      'l_shipinstruct', 'l_shipmode', 'l_comment')

lineitem_colclasses = c('integer', 'integer', 'integer', 'integer',
                        'double', 'double', 'double', 'double',
                        'character', 'character',
                        'Date', 'Date', 'Date',
                        'character', 'character', 'character')

colnames = list(
  nation=c('n_nationkey', 'n_name', 'n_regionkey', 'n_comment'),
  region=c('r_regionkey', 'r_name', 'r_comment'),
  part=c('p_partkey', 'p_name', 'p_mfgr', 'p_brand', 'p_type', 'p_size', 'p_container', 'p_retailprice', 'p_commment'),
  supplier=c('s_suppkey', 's_name', 's_address', 's_nationkey', 's_phone', 's_acctbal', 's_comment'),
  partsupp=c('ps_partkey', 'ps_suppkey', 'ps_availqty', 'ps_supplycost', 'ps_comment'),
  customer=c('c_custkey', 'c_name', 'c_address', 'c_nationkey', 'c_phone', 'c_acctbal', 'c_mktsegment', 'c_comment'),
  orders=c('o_orderkey','o_custkey','o_orderstatus','o_totalprice','o_orderdate','o_orderpriority','o_clerk', 'o_shippriority','o_comment'),
  lineitem=lineitem_colnames
)

colclasses = list(
  nation=c('integer', 'character', 'integer', 'character'),
  region=c('integer', 'character', 'character'),
  part=c('integer', 'character', 'character', 'character', 'character', 'integer', 'character', 'double', 'character'),
  supplier=c('integer', 'character', 'character', 'integer', 'character', 'double', 'character'),
  partsupp=c('integer', 'integer', 'integer', 'double', 'character'),
  customer=c('integer', 'character', 'character', 'integer', 'character', 'double', 'character', 'character'),
  orders=c('integer', 'integer', 'character', 'double', 'Date', 'character', 'character', 'integer', 'character'),
  lineitem=lineitem_colclasses
)

