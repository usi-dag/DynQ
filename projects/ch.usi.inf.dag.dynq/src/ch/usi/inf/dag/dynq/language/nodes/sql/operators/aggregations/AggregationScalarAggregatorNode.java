package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations;

import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateFunctionNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;


public final class AggregationScalarAggregatorNode extends Node implements Explainable {

    @Child
    private AggregateFunctionNode aggregatorNode;

    public AggregationScalarAggregatorNode(AggregateFunctionNode aggregatorNode) {
        this.aggregatorNode = aggregatorNode;
    }

    public Object getInitialState() {
        return aggregatorNode.getInitialState();
    }

    public Object aggregate(VirtualFrame frame, Object state, Object input) throws InteropException, FrameSlotTypeException {
        return aggregatorNode.execute(frame, state, input);
    }

    public Object finalize(VirtualFrame frame, Object state) {
        return aggregatorNode.finalize(frame, state);
    }
}
