package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.GreaterThanEqualRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.GreaterThanEqualRexTruffleNodeGen;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class AggregateMaxNode extends AggregateFunctionNode {

    @Child
    RexTruffleNode expressionNode;

    @Child
    GreaterThanEqualRexTruffleNode comparatorNode = GreaterThanEqualRexTruffleNodeGen.create();

    @CompilerDirectives.CompilationFinal
    final Object initialState;

    public AggregateMaxNode(RexTruffleNode expressionNode) {
        this(expressionNode, null);
    }

    public AggregateMaxNode(RexTruffleNode expressionNode, Object initialState) {
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

        if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, state != null)) {
            if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, inputValue != null)) {
                boolean isGte = (Boolean) comparatorNode.execute(state, inputValue);
                return isGte ? state : inputValue;
            } else {
                return state;
            }
        } else {
            return inputValue;
        }
    }

}
