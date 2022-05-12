package ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations;

import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.aggregators.AggregateFunctionNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;

import java.util.Arrays;


public final class AggregationMultipleAggregatorNode extends Node implements Explainable {

    public static AggregationMultipleAggregatorNode create(AggregateFunctionNode[] aggregatorNodes, int leftHoles) {
        return new AggregationMultipleAggregatorNode(aggregatorNodes, leftHoles);
    }

    public static AggregationMultipleAggregatorNode create(AggregateFunctionNode[] aggregatorNodes) {
        return new AggregationMultipleAggregatorNode(aggregatorNodes);
    }

    @Children
    private AggregateFunctionNode[] aggregatorNodes;

    private final boolean needsFinalization;

    // how many empty elements to be filled with group by keys
    @CompilerDirectives.CompilationFinal private final int leftHoles;
    @CompilerDirectives.CompilationFinal private final int totalSize;

    public AggregationMultipleAggregatorNode(AggregateFunctionNode[] aggregatorNodes, int leftHoles) {
        this.aggregatorNodes = aggregatorNodes;
        this.leftHoles = leftHoles;
        this.totalSize = leftHoles + aggregatorNodes.length;
        needsFinalization = Arrays.stream(aggregatorNodes).anyMatch(AggregateFunctionNode::needsFinalization);
    }

    public AggregationMultipleAggregatorNode(AggregateFunctionNode[] aggregatorNodes) {
        this(aggregatorNodes, 0);
    }

    public int size() {
        return leftHoles + aggregatorNodes.length;
    }

//    @Override
    @ExplodeLoop
    public Object[] getInitialState() {
        Object[] state = new Object[totalSize];
        for (int i = leftHoles; i < totalSize; i++) {
            state[i] = aggregatorNodes[i-leftHoles].getInitialState();
        }
        return state;
    }

//    @Specialization
    @ExplodeLoop
    public void aggregate(VirtualFrame frame, Object[] state, Object input) throws InteropException, FrameSlotTypeException {
        for (int i = leftHoles; i < totalSize; i++) {
            AggregateFunctionNode aggregateFunctionNode = aggregatorNodes[i-leftHoles];
            if(aggregateFunctionNode.evaluateFilter(frame, input)) {
                state[i] = aggregateFunctionNode.execute(frame, state[i], input);
            }
        }
    }


//    @Override
    @ExplodeLoop
    public Object[] finalize(VirtualFrame frame, Object[] stateArray) {
//        Object[] stateArray = (Object[]) state;
        if(!needsFinalization) {
            return stateArray;
        }
        for (int i = leftHoles; i < totalSize; i++) {
            stateArray[i] = aggregatorNodes[i-leftHoles].finalize(frame, stateArray[i]);
        }
        return stateArray;
    }

//    @Override
    public boolean needsFinalization() {
        return needsFinalization;
    }
}
