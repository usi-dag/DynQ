package ch.usi.inf.dag.dynq_r.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.DynamicStarFakeNode;
import ch.usi.inf.dag.dynq.parser.relnode_wrappers.TableScanVolcanoWrapper;
import ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.r_dataframe.RDataFrameVectorRexTruffleNodeAccessorFactory;
import ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.untyped.RDataFrameVectorRexTruffleNodeGen;
import ch.usi.inf.dag.dynq_r.language.nodes.sql.volcano.table_scans.VolcanoIteratorRDataFrameTableScanNode;
import ch.usi.inf.dag.dynq_r.runtime.objects.managed_tables.memory.DynamicRDataFrameInputArrayTable;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.sql.type.SqlTypeName;


public class RDataFrameTableScanVolcanoWrapper extends TableScanVolcanoWrapper {

    final DynamicRDataFrameInputArrayTable table;


    public RDataFrameTableScanVolcanoWrapper(DynamicRDataFrameInputArrayTable rDataFrameInputArrayTable,
                                             TableScan scan) {
        this(new VolcanoIteratorRDataFrameTableScanNode(rDataFrameInputArrayTable.getInput()),
                rDataFrameInputArrayTable, scan);
    }

    private RDataFrameTableScanVolcanoWrapper(VolcanoIteratorRDataFrameTableScanNode volcanoIteratorNode,
                                              DynamicRDataFrameInputArrayTable rDataFrameInputArrayTable,
                                              TableScan scan) {
        super(volcanoIteratorNode, scan);
        this.table = rDataFrameInputArrayTable;
    }

    @Override
    protected RexTruffleNode getDynamicObjectInteropReader(RelDataTypeField field) {
        String fieldName = field.getName();
        RelDataType type = field.getType();
        if (type.getSqlTypeName() == SqlTypeName.DYNAMIC_STAR) {
            return new DynamicStarFakeNode();
        }
        try {
            return RDataFrameVectorRexTruffleNodeAccessorFactory.create(table.getVector(fieldName));
        } catch (IllegalArgumentException e) {
            return RDataFrameVectorRexTruffleNodeGen.create(table.getVector(fieldName));
        }
    }

}
