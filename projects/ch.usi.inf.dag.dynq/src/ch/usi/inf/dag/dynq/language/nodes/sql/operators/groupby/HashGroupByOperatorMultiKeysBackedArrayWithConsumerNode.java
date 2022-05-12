package ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ArrayRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.AggregationMultipleAggregatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing.ArrayBox;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing.ArrayBoxHashNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.ConditionProfile;


public final class HashGroupByOperatorMultiKeysBackedArrayWithConsumerNode extends HashGroupByOperatorWithConsumerNode {

    @Child
    private AggregationMultipleAggregatorNode aggregatorNode;

    @Child
    private ArrayRexTruffleNode keyGetter;

    @Child
    private ArrayBoxHashNode boxHashNode;

    @CompilerDirectives.CompilationFinal
    private final int keyGetterSize;

    private final ConditionProfile mapMissingKeyProfiler = ConditionProfile.createCountingProfile();

    public HashGroupByOperatorMultiKeysBackedArrayWithConsumerNode(DataCentricConsumerNode consumer,
                                                                   AggregationMultipleAggregatorNode aggregatorNode,
                                                                   ArrayRexTruffleNode keyGetter) {
        super(consumer);
        this.aggregatorNode = aggregatorNode;
        this.keyGetter = keyGetter;
        this.keyGetterSize = keyGetter.size();
        this.boxHashNode = new ArrayBoxHashNode(keyGetterSize);
    }

    public HashGroupByState getInitialState() {
        return new HashGroupByState();
    }

    public void execute(VirtualFrame frame, HashGroupByState state, Object row) throws InteropException, FrameSlotTypeException {
        Object[] data = keyGetter.executeWith(frame, row);
        ArrayBox key = new ArrayBox(data, boxHashNode.hash(data));
        Object[] group = TruffleBoundaryUtils.hashMapGet(state, key);

        if(mapMissingKeyProfiler.profile(group == null)) {
            group = aggregatorNode.getInitialState();
            fillKeys(group, data);
            TruffleBoundaryUtils.hashMapPut(state, key, group);
        }

        aggregatorNode.aggregate(frame, group, row);
    }

    @ExplodeLoop
    private void fillKeys(Object[] group, Object[] data) {
        for (int i = 0; i < keyGetterSize; i++) {
            group[i] = data[i];
        }
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