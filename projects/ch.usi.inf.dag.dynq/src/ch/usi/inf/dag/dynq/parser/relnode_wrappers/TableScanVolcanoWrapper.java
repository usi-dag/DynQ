package ch.usi.inf.dag.dynq.parser.relnode_wrappers;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.type.RelDataTypeField;

public abstract class TableScanVolcanoWrapper extends TableScan implements RelNodeVolcanoWrapper  {

    private final TableScan scan;
    private final VolcanoIteratorNode volcanoIteratorNode;


    public TableScanVolcanoWrapper(VolcanoIteratorNode volcanoIteratorNode, TableScan scan) {
        super(scan.getCluster(), scan.getTraitSet(), scan.getHints(), scan.getTable());
        this.volcanoIteratorNode = volcanoIteratorNode;
        this.scan = scan;
    }

    @Override
    public VolcanoIteratorNode getVolcanoIteratorNode() {
        return volcanoIteratorNode;
    }

    @Override
    public RexTruffleNode getInputDataAccessor(int index) {
        throw new RuntimeException("Should never reach this, table scans have no input");
    }


    @Override
    public RexTruffleNode getOutputDataAccessor(int index) {
        return getDynamicObjectInteropReader(scan.getRowType().getFieldList().get(index));
    }


    protected abstract RexTruffleNode getDynamicObjectInteropReader(RelDataTypeField field);


}
