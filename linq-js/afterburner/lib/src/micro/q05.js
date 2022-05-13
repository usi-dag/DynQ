
function query(noasm){
  return abdb.select()
      .from('lineitem')
      .field("l_extendedprice", 'l_orderkey')
      .where(_gte('l_shipdate',_date('1995-12-01')))
      .order('l_orderkey', 'l_extendedprice')
      .limit(1000)
}

module.exports=query;
