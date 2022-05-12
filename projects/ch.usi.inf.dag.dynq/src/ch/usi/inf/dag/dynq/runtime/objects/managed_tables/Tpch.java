package ch.usi.inf.dag.dynq.runtime.objects.managed_tables;

import com.google.common.collect.ImmutableMap;
import org.apache.calcite.rel.type.RelProtoDataType;

import java.sql.Date;
import java.util.Map;


public class Tpch {

  // TODO custom schema definition with registered tables

  public enum TpchTableEnum {

    NATION(new ImmutableMap.Builder<String, RelProtoDataType>()
            .put("n_nationkey", mk(Long.class))
            .put("n_name", mk(String.class))
            .put("n_regionkey", mk(Long.class))
            .put("n_comment", mk(String.class))
            .build()
    ),

    REGION(new ImmutableMap.Builder<String, RelProtoDataType>()
            .put("r_regionkey", mk(Long.class))
            .put("r_name", mk(String.class))
            .put("r_comment", mk(String.class))
            .build()
    ),

    PART(new ImmutableMap.Builder<String, RelProtoDataType>()
            .put("p_partkey", mk(Long.class))
            .put("p_name", mk(String.class))
            .put("p_mfgr", mk(String.class))
            .put("p_brand", mk(String.class))
            .put("p_type", mk(String.class))
            .put("p_size", mk(Integer.class))
            .put("p_container", mk(String.class))
            .put("p_retailprice", mk(Double.class))
            .put("p_comment", mk(String.class))
            .build()
    ),

    SUPPLIER(new ImmutableMap.Builder<String, RelProtoDataType>()
            .put("s_suppkey", mk(Long.class))
            .put("s_name", mk(String.class))
            .put("s_address", mk(String.class))
            .put("s_nationkey", mk(Long.class))
            .put("s_phone", mk(String.class))
            .put("s_acctbal", mk(Double.class))
            .put("s_comment", mk(String.class))
            .build()
    ),

    PARTSUPP(new ImmutableMap.Builder<String, RelProtoDataType>()
            .put("ps_partkey", mk(Long.class))
            .put("ps_suppkey", mk(Long.class))
            .put("ps_availqty", mk(Long.class))
            .put("ps_supplycost", mk(Double.class))
            .put("ps_comment", mk(String.class))
            .build()
    ),

    CUSTOMER(new ImmutableMap.Builder<String, RelProtoDataType>()
            .put("c_custkey", mk(Long.class))
            .put("c_name", mk(String.class))
            .put("c_address", mk(String.class))
            .put("c_nationkey", mk(Long.class))
            .put("c_phone", mk(String.class))
            .put("c_acctbal", mk(Double.class))
            .put("c_mktsegment", mk(String.class))
            .put("c_comment", mk(String.class))
            .build()
    ),

    ORDERS(new ImmutableMap.Builder<String, RelProtoDataType>()
            .put("o_orderkey", mk(Long.class))
            .put("o_custkey", mk(Long.class))
            .put("o_orderstatus", mk(String.class))
            .put("o_totalprice", mk(Double.class))
            .put("o_orderdate", mk(Date.class))
            .put("o_orderpriority", mk(String.class))
            .put("o_clerk", mk(String.class))
            .put("o_shippriority", mk(Integer.class))
            .put("o_comment", mk(String.class))
            .build()
    ),

    LINEITEM(new ImmutableMap.Builder<String, RelProtoDataType>()
            .put("l_orderkey", mk(Long.class))
            .put("l_partkey", mk(Long.class))
            .put("l_suppkey", mk(Long.class))
            .put("l_linenumber", mk(Long.class))

            .put("l_quantity", mk(Double.class))
            .put("l_extendedprice", mk(Double.class))
            .put("l_discount", mk(Double.class))
            .put("l_tax", mk(Double.class))

            .put("l_returnflag", mk(String.class))
            .put("l_linestatus", mk(String.class))

            .put("l_shipdate", mk(Date.class))
            .put("l_commitdate", mk(Date.class))
            .put("l_receiptdate", mk(Date.class))

            .put("l_shipinstruct", mk(String.class))
            .put("l_shipmode", mk(String.class))
            .put("l_comment", mk(String.class))

            .build()
    ),

    ;

    final Map<String, RelProtoDataType> schemaMap;
    TpchTableEnum(Map<String, RelProtoDataType> schemaMap) {
      this.schemaMap = schemaMap;
    }

    public Map<String, RelProtoDataType> getSchemaMap() {
      return schemaMap;
    }

    static RelProtoDataType mk(Class<?> cls) {
      return  t -> t.createJavaType(cls);
    }

  }


}
