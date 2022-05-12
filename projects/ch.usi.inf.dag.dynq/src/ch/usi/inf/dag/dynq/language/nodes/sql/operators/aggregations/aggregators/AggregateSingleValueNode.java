package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class AggregateSingleValueNode extends AggregateFunctionNode {

    private static final Integer INITIAL = null;

    @Child
    RexTruffleNode expressionNode;

    public AggregateSingleValueNode(RexTruffleNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    @Override
    public Object getInitialState() {
        return INITIAL;
    }

    @Override
    public Object execute(VirtualFrame frame, Object state, Object input) throws InteropException, FrameSlotTypeException {
        return expressionNode.executeWith(frame, input);
    }

}
