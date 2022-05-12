package ch.usi.inf.dag.dynq_r.parser;

import ch.usi.inf.dag.dynq.parser.TableScanVisitor;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.RelNodeVolcanoWrapper;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import ch.usi.inf.dag.dynq_r.parser.relnode_wrappers.RDataFrameTableScanVolcanoWrapper;
import ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory.DynamicRDataFrameInputArrayTable;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.schema.Table;


public class RTableScanVisitor implements TableScanVisitor {

    @Override
    public RelNodeVolcanoWrapper visit(TableScan tableScan) {
        return null;
    }

    @Override
    public RelNodeVolcanoWrapper visit(TableScan tableScan, APISessionManagement session) {
        String tableName = tableScan.getTable().getQualifiedName().get(0);
        Table table = session.getRegisteredTables().getTable(tableName);
        if(table instanceof DynamicRDataFrameInputArrayTable) {
            DynamicRDataFrameInputArrayTable rDataFrameInputArrayTable = (DynamicRDataFrameInputArrayTable) table;
            return new RDataFrameTableScanVolcanoWrapper(rDataFrameInputArrayTable, tableScan);
        }
        return null;
    }
}
