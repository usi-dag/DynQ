package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.LessThanEqualRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.LessThanEqualRexTruffleNodeGen;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class AggregateMinNode extends AggregateFunctionNode {

    @Child
    RexTruffleNode expressionNode;

    @Child
    LessThanEqualRexTruffleNode lessThanNode = LessThanEqualRexTruffleNodeGen.create();

    @CompilerDirectives.CompilationFinal
    final Object initialState;

    public AggregateMinNode(RexTruffleNode expressionNode) {
        this(expressionNode, null);
    }

    public AggregateMinNode(RexTruffleNode expressionNode, Object initialState) {
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
                boolean isLte = (Boolean) lessThanNode.execute(state, inputValue);
                return isLte ? state : inputValue;
            } else {
                return state;
            }
        } else {
            return inputValue;
        }
    }

}
