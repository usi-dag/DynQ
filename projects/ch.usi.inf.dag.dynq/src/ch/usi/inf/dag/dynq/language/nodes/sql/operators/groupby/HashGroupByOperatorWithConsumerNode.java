package ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


// TODO HashGroupByOperatorNode interface and then extends DataCentricWithDestination
public abstract class HashGroupByOperatorWithConsumerNode extends HashGroupByOperatorNode {

    @Child
    DataCentricConsumerNode consumerNode;

    public HashGroupByOperatorWithConsumerNode(DataCentricConsumerNode consumerNode) {
        this.consumerNode = consumerNode;
    }

    public abstract Object finalize(VirtualFrame frame, HashGroupByState state)
            throws InteropException, FrameSlotTypeException;

    @Override
    public HashGroupByOperatorWithConsumerNode acceptConsumer(DataCentricConsumerNode consumerNode) {
        this.consumerNode = consumerNode;
        return this;
    }

    @Override
    public boolean hasDefaultFinalizer() {
        return true;
    }
}
