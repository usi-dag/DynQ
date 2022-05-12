package ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory;


import org.apache.calcite.adapter.enumerable.EnumerableTableScan;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.LinkedList;
import java.util.List;


public class RDataFrameInputArrayWithSchemaAnyTypeTable extends DynamicRDataFrameInputArrayTable {
    // TODO implement QueryableTable and find a way to make planning work in Calcite engine

    private RelDataType dataType = null;
    RDataFrameTable.Table table;

    public RDataFrameInputArrayWithSchemaAnyTypeTable(RDataFrameTable.Table table) {
        super(table.getrList(), (int) table.getnElements());
        this.table = table;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        if(dataType == null) {
            List<RelDataType> fieldTypesConverted = new LinkedList<>();
            List<String> fieldNames = new LinkedList<>();
            for (RDataFrameTable.Column column : table.getColumns()) {
                RelDataType colType = typeFactory.createSqlType(SqlTypeName.ANY);
                boolean seenNa = column.getVector().access().na.neverSeenNA();
                fieldTypesConverted.add(typeFactory.createTypeWithNullability(colType, !seenNa));
                fieldNames.add(column.getName().toLowerCase());
            }
            dataType = typeFactory.createStructType(fieldTypesConverted, fieldNames);
        }
        return dataType;
    }

    @Override
    public void resetDataType() {
        dataType = null;
    }

    @Override
    public RelNode toRel(RelOptTable.ToRelContext context, RelOptTable relOptTable) {
        return EnumerableTableScan.create(context.getCluster(), relOptTable);
    }

}
