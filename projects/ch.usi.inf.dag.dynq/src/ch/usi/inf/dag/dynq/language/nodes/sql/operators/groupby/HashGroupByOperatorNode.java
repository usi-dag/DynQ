package ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;


// TODO make an interface
public abstract class HashGroupByOperatorNode extends Node implements Explainable {

    public HashGroupByState getInitialState() {
        return new HashGroupByState();
    }
    public HashGroupByState getInitialState(int size) {
        return new HashGroupByState(size);
    }

    public abstract void execute(VirtualFrame frame, HashGroupByState state, Object row)
            throws InteropException, FrameSlotTypeException;


    public abstract Object finalize(VirtualFrame frame, HashGroupByState state)
            throws InteropException, FrameSlotTypeException;

    public abstract HashGroupByOperatorWithConsumerNode acceptConsumer(DataCentricConsumerNode consumerNode);

    public void init(VirtualFrame frame) {}

    public abstract boolean hasDefaultFinalizer();

}
