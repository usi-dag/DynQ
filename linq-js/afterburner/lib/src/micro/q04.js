
function query(noasm){
  return abdb.select()
      .from('lineitem')
      .field(_as(_mul("l_extendedprice","l_discount"),"res"))
      .where(_gte('l_shipdate',_date('1995-12-01')))
}

module.exports=query;
