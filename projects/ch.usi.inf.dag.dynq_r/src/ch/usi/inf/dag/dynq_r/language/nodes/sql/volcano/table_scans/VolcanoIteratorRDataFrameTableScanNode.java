package ch.usi.inf.dag.dynq_r.language.nodes.sql.volcano.table_scans;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.scans.columnar.ColumnarTableScanFactory;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;


public final class VolcanoIteratorRDataFrameTableScanNode extends VolcanoIteratorNode {

    private final RList input;
    private final long nElements;

    public VolcanoIteratorRDataFrameTableScanNode(RList input) {
        this.input = input;
        this.nElements = getInputSize(input);
    }

    public RList getInput() {
        return input;
    }

    public static long getInputSize(RList input) {
        Object[] data = (Object[]) input.getData();
        return ((RAbstractVector)data[0]).getLength();
    }

    @Override
    public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
        return ColumnarTableScanFactory.createScan(nElements, consumer);
    }

    @Override
    public String explain() {
        return super.explain() + "(" + nElements + ")";
    }
}
