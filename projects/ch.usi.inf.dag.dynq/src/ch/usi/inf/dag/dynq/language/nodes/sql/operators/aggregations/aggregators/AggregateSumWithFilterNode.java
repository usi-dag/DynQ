package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.AddRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.AddRexTruffleNodeGen;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class AggregateSumWithFilterNode extends AggregateFunctionWithFilterNode {

    private static final Integer INITIAL = 0;

    @Child
    RexTruffleNode expressionNode;

    @Child
    AddRexTruffleNode addNode = AddRexTruffleNodeGen.create();

    @CompilerDirectives.CompilationFinal
    final Object initialState;

    public AggregateSumWithFilterNode(RexTruffleNode filterNode, RexTruffleNode expressionNode) {
        super(filterNode);
        this.expressionNode = expressionNode;
        this.initialState = AggregateSumWithFilterNode.INITIAL;
    }

    @Override
    public Object getInitialState() {
        return initialState;
    }

    @Override
    public Object execute(VirtualFrame frame, Object state, Object input) throws InteropException, FrameSlotTypeException {
        Object inputValue = expressionNode.executeWith(frame, input);
        if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, state != initialState)) {
            if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, inputValue != INITIAL)) {
                return addNode.execute(state, inputValue);
            } else {
                return state;
            }
        }
        return inputValue;
    }

}
