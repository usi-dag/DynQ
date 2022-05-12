package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric;


import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;


public abstract class DataCentricConsumerNode extends Node implements Explainable {

    public abstract void execute(VirtualFrame frame, Object row)
            throws InteropException, FrameSlotTypeException, EndOfComputation;

    public abstract Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException;

    public void init(VirtualFrame frame) {

    }
    public void init(VirtualFrame frame, int exactSize) {
        // most of the nodes does not use size
        init(frame);
    }
    public void free(VirtualFrame frame) {

    }

}
