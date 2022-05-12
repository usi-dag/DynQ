package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public abstract class AggregateFunctionWithFilterNode extends AggregateFunctionNode {

    @Child
    RexTruffleNode filterNode;

    public AggregateFunctionWithFilterNode(RexTruffleNode filterNode) {
        this.filterNode = filterNode;
    }

    @Override
    public boolean evaluateFilter(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
        return filterNode.runBoolean(frame, row);
    }

}
