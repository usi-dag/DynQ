package ch.usi.inf.dag.dynq_js.parser.relnode_wrappers;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.DynamicStarFakeNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.PolyglotTableScanVolcanoWrapper;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped.interop.InteropReaderFactory;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.sql.type.SqlTypeName;


public class JSTableScanVolcanoWrapper extends PolyglotTableScanVolcanoWrapper {

    public JSTableScanVolcanoWrapper(VolcanoIteratorNode volcanoIteratorNode, TableScan scan) {
        super(volcanoIteratorNode, scan);
    }

    @Override
    protected RexTruffleNode getDynamicObjectInteropReader(RelDataTypeField field) {
        if("__this__".equals(field.getName())) {
            return new DynamicStarFakeNode();
        }
        if(field.getType().getSqlTypeName() == SqlTypeName.ANY) {
            return InteropReaderFactory.get(field.getName());
        }
        return super.getDynamicObjectInteropReader(field);
    }
}
