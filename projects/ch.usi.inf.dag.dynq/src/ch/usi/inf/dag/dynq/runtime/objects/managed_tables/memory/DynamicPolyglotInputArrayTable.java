package ch.usi.inf.dag.dynq.runtime.objects.managed_tables.memory;


import ch.usi.inf.dag.dynq.runtime.objects.managed_tables.ResettableDynamicRecordTable;
import org.apache.calcite.adapter.enumerable.EnumerableTableScan;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.DynamicRecordTypeImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.TranslatableTable;

import java.util.Collections;


public class DynamicPolyglotInputArrayTable extends PolyglotInputArrayTable
        implements TranslatableTable, ResettableDynamicRecordTable {
    private RelDataType dataType = null;

    public DynamicPolyglotInputArrayTable(Object input) {
        super(input, Collections.emptyMap());
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        if(dataType == null) {
            dataType = new DynamicRecordTypeImpl(typeFactory);
            dataType.getField("__this__", true, true);
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
