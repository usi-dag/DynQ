package ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory;


import org.apache.calcite.adapter.enumerable.EnumerableTableScan;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.SqlCollation;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;


public class RDataFrameInputArrayWithSchemaTable extends DynamicRDataFrameInputArrayTable {

    private RelDataType dataType = null;
    RDataFrameTable.Table table;

    public RDataFrameInputArrayWithSchemaTable(RDataFrameTable.Table table) {
        super(table.getrList(), (int) table.getnElements());
        this.table = table;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        if(dataType == null) {
            List<RelDataType> fieldTypesConverted = new LinkedList<>();
            List<String> fieldNames = new LinkedList<>();
            for (RDataFrameTable.Column column : table.getColumns()) {
                RelDataType colType = new BasicRDataFrameType(typeFactory.getTypeSystem(), column.getDataType());
                if(column.getDataType() == RDataFrameTable.RDataFrameTableDataType.STRING) {
//                     TODO fix this, it should work out of the box but Calcite throws NullPointerException with no Charset/Collation
                    colType = typeFactory.createTypeWithCharsetAndCollation(colType, StandardCharsets.ISO_8859_1, SqlCollation.IMPLICIT);
                }
                fieldTypesConverted.add(colType);
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
