package ch.usi.inf.dag.dynq_js.language.nodes.sql.volcano.table_scans;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.scans.columnar.ColumnarTableScanCTNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.scans.columnar.ColumnarTableScanWithLoopCTNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.scans.columnar.ColumnarTableScanWithLoopNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.table_scans.VolcanoIteratorPolyglotTableScanNode;


public final class VolcanoIteratorAfterBurnerTableScanNode extends VolcanoIteratorNode {

    private final long nElements; // TODO get rid

    public VolcanoIteratorAfterBurnerTableScanNode(long nElements) {
        this.nElements = nElements;
    }

    @Override
    public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
        if(VolcanoIteratorPolyglotTableScanNode.PUSH_TO_LOOP_NODE_CT) {
            return new ColumnarTableScanWithLoopCTNode(nElements, consumer);
        }
        if(VolcanoIteratorPolyglotTableScanNode.PUSH_TO_CT) {
            return new ColumnarTableScanCTNode(nElements, consumer);
        }
        if(VolcanoIteratorPolyglotTableScanNode.PUSH_TO_LOOP_NODE) {
            return new ColumnarTableScanWithLoopNode(nElements, consumer);
        } else {
            throw new RuntimeException("Currently AfterBurner TableScan supports only LoopNode");
        }
    }
}
