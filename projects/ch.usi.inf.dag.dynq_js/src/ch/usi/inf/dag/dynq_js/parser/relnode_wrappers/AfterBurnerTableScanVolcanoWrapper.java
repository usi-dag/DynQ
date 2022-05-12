package ch.usi.inf.dag.dynq_js.parser.relnode_wrappers;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.DynamicStarFakeNode;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.TableScanVolcanoWrapper;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.AfterBurnerColumnarItemGetter;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer.AfterBurnerColumnarItemNativeOrderByteBufferGetter;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.volcano.table_scans.VolcanoIteratorAfterBurnerTableScanNode;
import ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory.AfterBurnerDB;
import ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory.AfterBurnerInputArrayTable;
import ch.usi.inf.dag.dynq_js.runtime.managed_tables.memory.SchemaAfterBurnerInputArrayTable;
import com.oracle.truffle.api.object.DynamicObject;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.type.RelDataTypeField;


public class AfterBurnerTableScanVolcanoWrapper extends TableScanVolcanoWrapper {

    private final DynamicObject buffer;
    private final AfterBurnerDB.Table table;
    private final AfterBurnerInputArrayTable afterBurnerInputArrayTable;
    private final TableScan scan;

    public AfterBurnerTableScanVolcanoWrapper(AfterBurnerInputArrayTable table,
                                              VolcanoIteratorAfterBurnerTableScanNode volcanoIteratorNode,
                                              TableScan scan) {
        super(volcanoIteratorNode, scan);
        this.buffer = table.getBuffer();
        this.table = table.getTable();
        afterBurnerInputArrayTable = table;
        this.scan = scan;
    }


    @Override
    public RexTruffleNode getOutputDataAccessor(int index) {
        AfterBurnerDB.Column column = null;
        if(afterBurnerInputArrayTable instanceof SchemaAfterBurnerInputArrayTable) {
            column = table.getColumns()[index];
        } else {
            RelDataTypeField field = scan.getRowType().getFieldList().get(index);
            for (int i = 0; i < table.getColumns().length; i++) {
                if(field.getName().equalsIgnoreCase(table.getColumns()[i].getName())) {
                    column = table.getColumns()[i];
                    break;
                }
            }
            if(column == null) {
                if(field.isDynamicStar()) {
                    return new DynamicStarFakeNode();
                }
                throw new IllegalArgumentException("Field not found in AfterBurner DB: " + field.getName());
            }
        }
        if(useOptAccessor()) {
            try {
                return AfterBurnerColumnarItemNativeOrderByteBufferGetter.createAccessor(buffer, column);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                return AfterBurnerColumnarItemGetter.createAccessor(buffer, column);
            }
        } else {
            return AfterBurnerColumnarItemGetter.createAccessor(buffer, column);
        }
    }

    private boolean useOptAccessor() {
        return "true".equals(
                System.getProperty("DYNQ_AFTERBURNER_USE_OPT_ACCESSOR",
                        System.getenv().getOrDefault("DYNQ_AFTERBURNER_USE_OPT_ACCESSOR", "false")));

    }

    @Override
    protected RexTruffleNode getDynamicObjectInteropReader(RelDataTypeField field) {
        throw new RuntimeException("getDynamicObjectInteropReader not available for AfterBurnerTableScanVolcanoWrapper");
    }

}
