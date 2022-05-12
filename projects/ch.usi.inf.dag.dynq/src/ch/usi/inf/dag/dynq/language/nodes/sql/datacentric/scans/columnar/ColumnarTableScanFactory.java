package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.scans.columnar;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;

public class ColumnarTableScanFactory {

    public static final boolean PUSH_TO_LOOP_NODE_CT = "true".equals(System.getenv("DYNQ_PUSH_TO_LOOP_NODE_CT"));
    public static final boolean PUSH_TO_CT = "true".equals(System.getenv("DYNQ_PUSH_TO_CT"));
    public static final boolean PUSH_TO_LOOP_NODE = "true".equals(System.getenv("DYNQ_PUSH_TO_LOOP_NODE"));

    public static TruffleLinqExecutableNode createScan(long nElements, DataCentricConsumerNode consumer) {
        if(PUSH_TO_LOOP_NODE_CT) {
            return new ColumnarTableScanWithLoopCTNode(nElements, consumer);
        }
        if(PUSH_TO_CT) {
            return new ColumnarTableScanCTNode(nElements, consumer);
        }
        if(PUSH_TO_LOOP_NODE) {
            return new ColumnarTableScanWithLoopNode(nElements, consumer);
        }
        return new ColumnarTableScanNode(nElements, consumer);
    }

}
