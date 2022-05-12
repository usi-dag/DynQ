package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.AddRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.AddRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.DivRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.DivRexTruffleNodeGen;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class AggregateAvgNode extends AggregateFunctionNode {

    @Child
    RexTruffleNode expressionNode;

    @Child
    AddRexTruffleNode addNode = AddRexTruffleNodeGen.create();

    @Child
    DivRexTruffleNode divNode = DivRexTruffleNodeGen.create();



    public AggregateAvgNode(RexTruffleNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    @Override
    public Object getInitialState() {
        return new AvgState();
    }

    @Override
    public Object execute(VirtualFrame frame, Object state, Object input) throws InteropException, FrameSlotTypeException {
        Object inputValue = expressionNode.executeWith(frame, input);
        AvgState avgState = (AvgState) state;
        if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, avgState.sum != null)) {
            if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, inputValue != null)) {
                avgState.sum = addNode.execute(avgState.sum, inputValue);
                avgState.count++;
            }
            return avgState;
        }
        if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, inputValue != null)) {
            avgState.sum = inputValue;
            avgState.count++;
        }
        return avgState;
    }

    public static final class AvgState {
        Object sum = null;
        long count = 0;
    }

    @Override
    public Object finalize(VirtualFrame frame, Object state) {
        AvgState avgState = (AvgState) state;
        if(avgState.count > 0) {
            return divNode.execute(avgState.sum, avgState.count);
        }
        return null;
    }

    @Override
    public boolean needsFinalization() {
        return true;
    }
}
