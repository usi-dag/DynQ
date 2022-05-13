//////////////////////////////////////////////////////////////////////////////
var inNode=(typeof window == 'undefined' );
if(typeof module == 'undefined'){
  module={};
} else { 
}
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////

function query11(noasm){
  sup_nat=abdb.select()
    .from("supplier").join("nation").on("s_nationkey","n_nationkey")
    .field("s_suppkey")
    .where(_eq("n_name",'GERMANY'))
    .materialize(noasm);

  ps_sup=abdb.select()
    .from("partsupp").join(sup_nat).on("ps_suppkey","s_suppkey")
    .field("ps_partkey",_as(_sum(_mul("ps_supplycost","ps_availqty")),"value1"))
    .group("ps_partkey")
    .materialize(noasm);

  thresh=abdb.select()
    .from(ps_sup)
    .field(_sum("value1")).eval(noasm);

  return abdb.select()
    .from(ps_sup)
    .field("ps_partkey","value1")
    .where(_gt("value1",thresh*0.0001))
    .order("-value1");
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
if(inNode){
  module.exports=query11;
} else delete module;
//////////////////////////////////////////////////////////////////////////////
