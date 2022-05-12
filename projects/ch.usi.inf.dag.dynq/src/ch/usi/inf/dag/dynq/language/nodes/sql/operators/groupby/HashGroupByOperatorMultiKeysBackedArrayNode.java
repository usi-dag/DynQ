package ch.usi.inf.dag.dynq.language.nodes.sql.operators.groupby;


import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
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


public final class HashGroupByOperatorMultiKeysBackedArrayNode extends HashGroupByOperatorNode {

    @Child
    private AggregationMultipleAggregatorNode aggregatorNode;

    @Child
    private ArrayRexTruffleNode keyGetter;

    @Child
    private RexTruffleNode finalizer;

    @Child
    private ArrayBoxHashNode boxHashNode;

    @CompilerDirectives.CompilationFinal
    private final int totalArraySize;

    @CompilerDirectives.CompilationFinal
    private final int keyGetterSize;

    private final ConditionProfile mapMissingKeyProfiler = ConditionProfile.createCountingProfile();

    public HashGroupByOperatorMultiKeysBackedArrayNode(AggregationMultipleAggregatorNode aggregatorNode,
                                                       ArrayRexTruffleNode keyGetter,
                                                       RexTruffleNode finalizer) {
        this.aggregatorNode = aggregatorNode;
        this.keyGetter = keyGetter;
        this.finalizer = finalizer;
        this.keyGetterSize = keyGetter.size();
        this.totalArraySize = keyGetterSize + aggregatorNode.size();
        this.boxHashNode = new ArrayBoxHashNode(keyGetterSize);
    }

    public HashGroupByOperatorMultiKeysBackedArrayNode(AggregationMultipleAggregatorNode aggregatorNode,
                                                       ArrayRexTruffleNode keyGetter) {
        this(aggregatorNode, keyGetter, null);
        this.finalizer = new DefaultFinalizer();
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

    public boolean hasDefaultFinalizer() {
        return finalizer instanceof DefaultFinalizer;
    }

    public Object finalize(VirtualFrame frame, HashGroupByState state) throws InteropException, FrameSlotTypeException {
        return finalizer.executeWith(frame, state);
    }

    @Override
    public HashGroupByOperatorWithConsumerNode acceptConsumer(DataCentricConsumerNode consumerNode) {
        return new HashGroupByOperatorMultiKeysBackedArrayWithConsumerNode(consumerNode, aggregatorNode, keyGetter);
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
