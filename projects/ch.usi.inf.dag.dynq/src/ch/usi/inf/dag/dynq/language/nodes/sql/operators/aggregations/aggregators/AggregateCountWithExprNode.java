package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class AggregateCountWithExprNode extends AggregateFunctionNode {

    @Child
    RexTruffleNode expressionNode;

    @CompilerDirectives.CompilationFinal
    final Object initialState;

    public AggregateCountWithExprNode(RexTruffleNode expressionNode) {
        this(expressionNode, 0L);
    }

    public AggregateCountWithExprNode(RexTruffleNode expressionNode, Object initialState) {
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
            return (long) state + 1;
        } else {
            return state;
        }
    }

}
