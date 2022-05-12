package ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.AggregationMultipleAggregatorNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.profiles.ConditionProfile;


public final class HashGroupByOperatorSingleKeyWithConsumerNode extends HashGroupByOperatorWithConsumerNode {

    @Child
    private AggregationMultipleAggregatorNode aggregatorNode;

    @Child
    private RexTruffleNode keyGetter;

    private final ConditionProfile mapMissingKeyProfiler = ConditionProfile.createCountingProfile();

    public HashGroupByOperatorSingleKeyWithConsumerNode(DataCentricConsumerNode consumer,
                                                        AggregationMultipleAggregatorNode aggregatorNode,
                                                        RexTruffleNode keyGetter) {
        super(consumer);
        this.aggregatorNode = aggregatorNode;
        this.keyGetter = keyGetter;
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

    @Override
    public Object finalize(VirtualFrame frame, HashGroupByState groups) throws InteropException, FrameSlotTypeException {
        consumerNode.init(frame, groups.size());
        try {
            for (Object[] preRow : groups.values()) {
                consumerNode.execute(frame, aggregatorNode.finalize(frame, preRow));
            }
        } catch (EndOfComputation ignored) {}
        Object result = consumerNode.getFinalizedState(frame);
        consumerNode.free(frame);
        return result;
    }

}
