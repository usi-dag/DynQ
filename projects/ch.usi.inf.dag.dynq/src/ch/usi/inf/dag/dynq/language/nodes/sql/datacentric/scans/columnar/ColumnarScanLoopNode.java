package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.scans.columnar;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;


public final class ColumnarScanLoopNode extends Node implements RepeatingNode, Explainable {


    final private int nElements;
    private int current = 0;

    @Child
    private DataCentricConsumerNode consumerNode;

    ColumnarScanLoopNode(long nElements, DataCentricConsumerNode consumerNode) {
        this.nElements = (int)nElements;
        this.consumerNode = consumerNode;
    }

    @Override
    public boolean executeRepeating(VirtualFrame frame) {
        try {
            consumerNode.execute(frame, current);
        }
        catch (EndOfComputation ignored) {return false;}
        catch (InteropException | FrameSlotTypeException ignored) {}
        return ++current < nElements;
    }

    public void init(VirtualFrame frame) {
        current = 0;
        consumerNode.init(frame, nElements);
    }

    public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
        Object result = consumerNode.getFinalizedState(frame);
        consumerNode.free(frame);
        return result;
    }

}

