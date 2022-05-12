package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric;


import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public abstract class DataCentricConsumerWithDestinationNode extends DataCentricConsumerNode {

    @Child protected DataCentricConsumerNode destination;

    public DataCentricConsumerWithDestinationNode(DataCentricConsumerNode destination) {
        this.destination = destination;
    }

    @Override
    public void init(VirtualFrame frame) {
        destination.init(frame);
    }

    @Override
    public void init(VirtualFrame frame, int exactSize) {
        destination.init(frame, exactSize);
    }

    @Override
    public void free(VirtualFrame frame) {
        destination.free(frame);
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
        return destination.getFinalizedState(frame);
    }

}
