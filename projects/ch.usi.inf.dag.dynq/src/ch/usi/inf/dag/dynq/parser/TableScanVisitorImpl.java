package ch.usi.inf.dag.dynq.parser;

import ch.usi.inf.dag.dynq.parser.relnode_wrappers.RelNodeVolcanoWrapper;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import org.apache.calcite.rel.core.TableScan;


public class TableScanVisitorImpl implements TableScanVisitor {
    @Override
    public RelNodeVolcanoWrapper visit(TableScan tableScan) {
        return null;
    }

    @Override
    public RelNodeVolcanoWrapper visit(TableScan tableScan, APISessionManagement session) {
        return null;
    }
}
