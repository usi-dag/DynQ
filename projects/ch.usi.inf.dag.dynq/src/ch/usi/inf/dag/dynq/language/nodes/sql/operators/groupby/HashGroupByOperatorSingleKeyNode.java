package ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.AggregationMultipleAggregatorNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.profiles.ConditionProfile;


public final class HashGroupByOperatorSingleKeyNode extends HashGroupByOperatorNode {

    @Child
    private AggregationMultipleAggregatorNode aggregatorNode;

    @Child
    private RexTruffleNode keyGetter;

    @Child
    private RexTruffleNode finalizer;

    private final ConditionProfile mapMissingKeyProfiler = ConditionProfile.createCountingProfile();

    @CompilerDirectives.CompilationFinal
    private final int totalArraySize;

    public HashGroupByOperatorSingleKeyNode(AggregationMultipleAggregatorNode aggregatorNode,
                                            RexTruffleNode keyGetter,
                                            RexTruffleNode finalizer) {
        this.aggregatorNode = aggregatorNode;
        this.keyGetter = keyGetter;
        this.finalizer = finalizer;
        this.totalArraySize = aggregatorNode.size() + 1;
    }

    public HashGroupByOperatorSingleKeyNode(AggregationMultipleAggregatorNode aggregatorNode,
                                            RexTruffleNode keyGetter) {
        this(aggregatorNode, keyGetter, null);
        this.finalizer = new DefaultFinalizer();
    }

    public HashGroupByState getInitialState() {
        return new HashGroupByState();
    }

    public void execute(VirtualFrame frame, HashGroupByState state, Object row) throws InteropException, FrameSlotTypeException {
        Object key = keyGetter.executeWith(frame, row);
        Object[] group = TruffleBoundaryUtils.hashMapGet(state, key);

        if(mapMissingKeyProfiler.profile(group == null)) {
            group = aggregatorNode.getInitialState();
            group[0] = key;
            TruffleBoundaryUtils.hashMapPut(state, key, group);
        }

        aggregatorNode.aggregate(frame, group, row);
    }

    public boolean hasDefaultFinalizer() {
        return finalizer instanceof DefaultFinalizer;
    }

    public Object finalize(VirtualFrame frame, HashGroupByState state) throws InteropException, FrameSlotTypeException {
        return finalizer.executeWith(frame, state);
    }

    @Override
    public HashGroupByOperatorWithConsumerNode acceptConsumer(DataCentricConsumerNode consumerNode) {
        return new HashGroupByOperatorSingleKeyWithConsumerNode(consumerNode, aggregatorNode, keyGetter);
    }


    public final class DefaultFinalizer extends RexTruffleNode {
        @Override
        public Object executeWith(VirtualFrame frame, Object input) {
            HashGroupByState groups = (HashGroupByState) input;
            Object[][] result = new Object[groups.size()][totalArraySize];
            int i = 0;
            for (Object[] preRow : groups.values()) {
                result[i++] = aggregatorNode.finalize(frame, preRow);
            }
            return result;
        }
    }

}
