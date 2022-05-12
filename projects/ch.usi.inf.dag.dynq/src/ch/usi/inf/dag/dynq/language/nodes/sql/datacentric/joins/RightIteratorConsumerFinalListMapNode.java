package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.structures.FinalListMap;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public abstract class RightIteratorConsumerFinalListMapNode extends AbstractRightIteratorConsumerNode {

    // Note: not a child, just needed for .reset() and .map;
    protected final LeftIteratorConsumerFinalListMapNode left;

    public RightIteratorConsumerFinalListMapNode(DataCentricConsumerNode destination, LeftIteratorConsumerFinalListMapNode left) {
        super(destination);
        this.left = left;
    }

    @Override
    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
        left.map = null;
        return super.getFinalizedState(frame);
    }

    protected FinalListMap getMap() {
        return left.map;
    }
}
