package ch.usi.inf.dag.dynq.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.dates.LocalDateFromInteropRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.doubles.DoubleFromInteropRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.integers.IntegerFromInteropRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.longs.LongFromInteropRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.StringFromInteropRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.DynamicStarFakeNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.interop.InteropReaderFactory;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeField;


public class PolyglotTableScanVolcanoWrapper extends TableScanVolcanoWrapper {


    public PolyglotTableScanVolcanoWrapper(VolcanoIteratorNode volcanoIteratorNode, TableScan scan) {
        super(volcanoIteratorNode, scan);
    }

    @Override
    protected RexTruffleNode getDynamicObjectInteropReader(RelDataTypeField field) {
        String fieldName = field.getName();
        if(fieldName.equals("__this__")) {
            return new DynamicStarFakeNode();
        }
        RelDataType type = field.getType();
        switch (type.getSqlTypeName()) {
            case DOUBLE:
            case FLOAT:
                return DoubleFromInteropRexTruffleNodeGen.create(fieldName);
            case DATE:
                return LocalDateFromInteropRexTruffleNodeGen.create(fieldName);
            case INTEGER:
                return IntegerFromInteropRexTruffleNodeGen.create(fieldName);
            case CHAR:
            case VARCHAR:
                return StringFromInteropRexTruffleNodeGen.create(fieldName);
            case BIGINT:
                return LongFromInteropRexTruffleNodeGen.create(fieldName);
            case ANY:
                return InteropReaderFactory.get(fieldName);
            case DYNAMIC_STAR:
                return new DynamicStarFakeNode();
        }
        throw new RuntimeException("Unexpected field type in RexTruffleNode: " + type);
    }

}
