package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.AddRexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class AggregateSumNode extends AggregateFunctionNode {

    @Child
    RexTruffleNode expressionNode;

    @Child
    AddRexTruffleNode addNode = AddRexTruffleNode.create();

    @CompilerDirectives.CompilationFinal
    final Object initialState;

    public AggregateSumNode(RexTruffleNode expressionNode) {
        this.expressionNode = expressionNode;
        this.initialState = null;
    }

    @Override
    public Object getInitialState() {
        return initialState;
    }

    @Override
    public Object execute(VirtualFrame frame, Object state, Object input) throws InteropException, FrameSlotTypeException {
        Object inputValue = expressionNode.executeWith(frame, input);
        if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, state != initialState)) {
            if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, inputValue != null)) {
                return addNode.execute(state, inputValue);
            } else {
                return state;
            }
        }
        return inputValue;
    }

}
