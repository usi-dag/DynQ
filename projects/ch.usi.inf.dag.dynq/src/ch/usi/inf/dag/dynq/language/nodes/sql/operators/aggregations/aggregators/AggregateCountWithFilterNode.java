package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.VirtualFrame;


public final class AggregateCountWithFilterNode extends AggregateFunctionWithFilterNode {

    public AggregateCountWithFilterNode(RexTruffleNode filterNode) {
        super(filterNode);
    }

    @Override
    public Object getInitialState() {
        return 0L;
    }

    @Override
    public Object execute(VirtualFrame frame, Object state, Object input) {
        return (long) state + 1;
    }

}
