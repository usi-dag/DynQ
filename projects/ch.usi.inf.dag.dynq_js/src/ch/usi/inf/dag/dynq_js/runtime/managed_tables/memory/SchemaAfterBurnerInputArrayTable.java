package ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory;


import com.google.common.collect.ImmutableList;
import com.oracle.truffle.api.object.DynamicObject;
import org.apache.calcite.adapter.enumerable.EnumerableTableScan;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.sql.SqlCollation;
import org.apache.calcite.util.ImmutableBitSet;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;


public class SchemaAfterBurnerInputArrayTable extends AfterBurnerInputArrayTable {
    private RelDataType dataType = null;

    public SchemaAfterBurnerInputArrayTable(DynamicObject jsArrayBuffer, AfterBurnerDB.Table table) {
        super(jsArrayBuffer, table);
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        if(dataType == null) {
            List<RelDataType> fieldTypesConverted = new LinkedList<>();
            List<String> fieldNames = new LinkedList<>();
            for (AfterBurnerDB.Column column : table.getColumns()) {
                RelDataType afterBurnerType = new BasicAfterBurnerType(typeFactory.getTypeSystem(), column.getDataType());
                if(column.getDataType() == AfterBurnerDB.AfterBurnerDataType.STRING) {
                    // TODO fix this, it should work out of the box but Calcite throws NullPointerException with no Charset/Collation
                    afterBurnerType = typeFactory.createTypeWithCharsetAndCollation(afterBurnerType, StandardCharsets.ISO_8859_1, SqlCollation.IMPLICIT);
                }
                fieldTypesConverted.add(afterBurnerType);
                fieldNames.add(column.getName().toLowerCase());
            }
            dataType = typeFactory.createStructType(fieldTypesConverted, fieldNames);
        }
        return dataType;
    }

    @Override
    public Statistic getStatistic() {
        return Statistics.of(nElements, ImmutableList.of());
    }

    @Override
    public void resetDataType() {
        dataType = null;
    }

    public AfterBurnerDB.Table getTable() {
        return table;
    }

    public DynamicObject getBuffer() {
        return jsArrayBuffer;
    }

    @Override
    public RelNode toRel(RelOptTable.ToRelContext context, RelOptTable relOptTable) {
        return EnumerableTableScan.create(context.getCluster(), relOptTable);
    }

}
