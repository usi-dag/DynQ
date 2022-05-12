package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;

import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;


public abstract class AggregateFunctionNode extends Node implements Explainable {

    public abstract Object getInitialState();

    public abstract Object execute(VirtualFrame frame, Object state, Object input) throws InteropException, FrameSlotTypeException;

    public Object finalize(VirtualFrame frame, Object state) {
        return state;
    }

    public boolean evaluateFilter(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
        return true;
    }

    public boolean needsFinalization() {
        return false;
    }
}
