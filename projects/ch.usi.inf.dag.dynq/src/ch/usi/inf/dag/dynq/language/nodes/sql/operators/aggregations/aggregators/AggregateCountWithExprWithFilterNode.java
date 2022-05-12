package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class AggregateCountWithExprWithFilterNode extends AggregateFunctionWithFilterNode {

    @Child
    RexTruffleNode expressionNode;

    @CompilerDirectives.CompilationFinal
    final Object initialState;

    public AggregateCountWithExprWithFilterNode(RexTruffleNode filterNode, RexTruffleNode expressionNode) {
        this(filterNode, expressionNode, 0L);
    }

    public AggregateCountWithExprWithFilterNode(RexTruffleNode filterNode, RexTruffleNode expressionNode, Object initialState) {
        super(filterNode);
        this.expressionNode = expressionNode;
        this.initialState = initialState;
    }

    @Override
    public Object getInitialState() {
        return initialState;
    }

    @Override
    public Object execute(VirtualFrame frame, Object state, Object input) throws InteropException, FrameSlotTypeException {
        Object inputValue = expressionNode.executeWith(frame, input);
        if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, inputValue != null)) {
            return (long) state + 1; //addNode.execute(state, inputValue);
        } else {
            return state;
        }
    }

}
