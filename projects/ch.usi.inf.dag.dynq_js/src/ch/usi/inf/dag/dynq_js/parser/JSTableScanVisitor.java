package ch.usi.inf.dag.dynq_js.parser;

import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.parser.TableScanVisitor;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.RelNodeVolcanoWrapper;
import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import ch.usi.inf.dag.dynq.runtime.objects.managed_tables.memory.PolyglotInputArrayTable;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.volcano.table_scans.VolcanoIteratorAfterBurnerTableScanNode;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.volcano.table_scans.VolcanoIteratorJSArrayTableScanNode;
import ch.usi.inf.dag.dynq_js.parser.relnode_wrappers.AfterBurnerTableScanVolcanoWrapper;
import ch.usi.inf.dag.dynq_js.parser.relnode_wrappers.JSTableScanVolcanoWrapper;
import ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory.AfterBurnerInputArrayTable;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.schema.Table;
import com.oracle.truffle.js.nodes.JSGuards;


public class JSTableScanVisitor implements TableScanVisitor {
    static public boolean OPTIMIZE_JS_ARRAY = "true".equals(System.getenv("DYNQ_JS_ARRAY"));

    @Override
    public RelNodeVolcanoWrapper visit(TableScan tableScan, APISessionManagement session) {
        String tableName = tableScan.getTable().getQualifiedName().get(0);
        Table table = session.getRegisteredTables().getTable(tableName);
        if(table instanceof PolyglotInputArrayTable) {
            PolyglotInputArrayTable polyglotTable = (PolyglotInputArrayTable) table;
            if (JSGuards.isJSObject(polyglotTable.getInput()) && OPTIMIZE_JS_ARRAY) {
                if (session.isDebugEnabled()) {
                    System.out.println("JS array");
                }
                VolcanoIteratorNode volcano = VolcanoIteratorJSArrayTableScanNode.create(polyglotTable.getInput(), (int) polyglotTable.getLength());
                return new JSTableScanVolcanoWrapper(volcano, tableScan);
            }
        }
        else if(table instanceof AfterBurnerInputArrayTable) {
            AfterBurnerInputArrayTable afterBurnerInputArrayTable = (AfterBurnerInputArrayTable) table;
            VolcanoIteratorAfterBurnerTableScanNode volcano = new VolcanoIteratorAfterBurnerTableScanNode((int) afterBurnerInputArrayTable.getTable().getnElements());
            return new AfterBurnerTableScanVolcanoWrapper(afterBurnerInputArrayTable, volcano, tableScan);
        }
        return null;
    }

    @Override
    public RelNodeVolcanoWrapper visit(TableScan tableScan) {
        return null;
    }
}
