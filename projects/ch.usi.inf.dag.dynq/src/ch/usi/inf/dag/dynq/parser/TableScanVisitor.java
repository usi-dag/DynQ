package ch.usi.inf.dag.dynq.parser;

import ch.usi.inf.dag.dynq.parser.relnode_wrappers.RelNodeVolcanoWrapper;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import org.apache.calcite.rel.core.TableScan;


public interface TableScanVisitor {

    RelNodeVolcanoWrapper visit(TableScan tableScan);

    RelNodeVolcanoWrapper visit(TableScan tableScan, APISessionManagement session);
}
