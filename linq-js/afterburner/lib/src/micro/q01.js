
function query(){
  return abdb.select()
      .from('lineitem')
      .field(_as(_count('*'),'res'))
      .where(_gte('l_shipdate',_date('1995-12-01')))
}

module.exports=query;
