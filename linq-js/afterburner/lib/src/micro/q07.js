
// filter.XJoin(filter).sum
const Q7 = `SELECT SUM(orders.o_totalprice) as res
FROM lineitem, orders
WHERE orders.o_orderdate >= DATE '1995-12-01'
AND lineitem.l_shipdate >= DATE '1995-12-01'
AND orders.o_orderkey = lineitem.l_orderkey`

function query(noasm){
  return abdb.select()
      .from('lineitem').join("orders").on("l_orderkey","o_orderkey")
      .field(_as(_sum("o_totalprice"),"res"))
      .where(
          _gte('l_shipdate',_date('1995-12-01')),
          _gte('o_orderdate',_date('1995-12-01')))
      .limit(1000)
}

module.exports=query;
