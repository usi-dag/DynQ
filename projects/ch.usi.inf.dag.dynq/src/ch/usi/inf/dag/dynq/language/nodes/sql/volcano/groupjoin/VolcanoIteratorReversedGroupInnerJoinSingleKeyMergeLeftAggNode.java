package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.groupjoin;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.FinalizerFillList;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.AggregationMultipleAggregatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.BinaryJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.graalvm.collections.Pair;

import java.util.HashMap;
import java.util.Map;


public final class VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeLeftAggNode extends VolcanoIteratorNode {

    @Child
    private VolcanoIteratorNode leftChild;
    @Child
    private VolcanoIteratorNode rightChild;

    @Child
    private RexTruffleNode buildKeyGetter;
    @Child
    private RexTruffleNode probeKeyGetter;

    @Child
    private AggregationMultipleAggregatorNode aggregatorNode;

    @Child
    BinaryJoinCondition nonEquiCondition;

    public VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeLeftAggNode(VolcanoIteratorNode leftChild,
                                                                          VolcanoIteratorNode rightChild,
                                                                          RexTruffleNode buildKeyGetter,
                                                                          RexTruffleNode probeKeyGetter,
                                                                          BinaryJoinCondition joinCondition,
                                                                          AggregationMultipleAggregatorNode aggregatorNode) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.buildKeyGetter = buildKeyGetter;
        this.probeKeyGetter = probeKeyGetter;
        this.aggregatorNode = aggregatorNode;
        this.nonEquiCondition = joinCondition;
    }

    @Override
    public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
        GroupJoinLeftIteratorConsumerNode leftConsumer =
                new GroupJoinLeftIteratorConsumerNode(buildKeyGetter, aggregatorNode);
        TruffleLinqExecutableNode left = leftChild.acceptConsumer(leftConsumer);

        DataCentricConsumerNode myConsumer;
        if (consumer == null || consumer instanceof FinalizerFillList) {
            myConsumer = new GroupJoinRightConsumerWithNoParentNode();
        } else {
            myConsumer = nonEquiCondition == null
                    ? new GroupJoinRightConsumerWithParentWithoutNonEquiCondition(consumer, leftConsumer, probeKeyGetter)
                    : new GroupJoinRightConsumerWithParentWithNonEquiCondition(consumer, leftConsumer, probeKeyGetter, nonEquiCondition);
        }

        TruffleLinqExecutableNode right = rightChild.acceptConsumer(myConsumer);

        return new TruffleLinqExecutableHashJoinNode(left, right);
    }

    private static final class GroupJoinRightConsumerWithNoParentNode extends DataCentricConsumerNode {

        GroupJoinRightConsumerWithNoParentNode() {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public void execute(VirtualFrame frame, Object row) {

        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) {
            return null;
        }
    }


    static class GroupJoinLeftIteratorConsumerNode extends DataCentricConsumerNode {
        HashMap<Object, Object[]> map;
        @Child
        private RexTruffleNode buildKeyGetter;
        @Child
        private AggregationMultipleAggregatorNode aggregatorNode;

        public GroupJoinLeftIteratorConsumerNode(RexTruffleNode buildKeyGetter,
                                                 AggregationMultipleAggregatorNode aggregatorNode) {
            this.buildKeyGetter = buildKeyGetter;
            this.aggregatorNode = aggregatorNode;
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
            Object key = buildKeyGetter.executeWith(frame, row);
            Object[] aggregated = computeIfAbsent(key);
            aggregatorNode.aggregate(frame, aggregated, row);
        }

        @Override
        public void init(VirtualFrame frame) {
            map = new HashMap<>();
        }

        @Override
        public void init(VirtualFrame frame, int exactSize) {
            map = new HashMap<>(exactSize);
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) {
            // TODO maybe this can be improved
            if (aggregatorNode.needsFinalization()) {
                for (Map.Entry<Object, Object[]> entry : map.entrySet()) {
                    entry.setValue(aggregatorNode.finalize(frame, entry.getValue()));
                }
            }
            return null;
        }

        @CompilerDirectives.TruffleBoundary(allowInlining = true)
        Object[] computeIfAbsent(Object key) {
            return map.computeIfAbsent(key, x -> {
                Object[] state = aggregatorNode.getInitialState();
                state[0] = key;
                return state;
            });
        }
    }

    static abstract class GroupJoinRightConsumerWithParent extends DataCentricConsumerWithDestinationNode {

        @Child
        RexTruffleNode keyGetter;
        GroupJoinLeftIteratorConsumerNode left;

        public GroupJoinRightConsumerWithParent(DataCentricConsumerNode consumer,
                                                GroupJoinLeftIteratorConsumerNode left,
                                                RexTruffleNode keyGetter) {
            super(consumer);
            this.left = left;
            this.keyGetter = keyGetter;
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            Object result = super.getFinalizedState(frame);
            left.map = null;
            return result;
        }
    }

    static final class GroupJoinRightConsumerWithParentWithNonEquiCondition extends GroupJoinRightConsumerWithParent {
        @Child
        BinaryJoinCondition nonEquiCondition;

        public GroupJoinRightConsumerWithParentWithNonEquiCondition(DataCentricConsumerNode consumer,
                                                                    GroupJoinLeftIteratorConsumerNode left,
                                                                    RexTruffleNode keyGetter,
                                                                    BinaryJoinCondition nonEquiCondition) {
            super(consumer, left, keyGetter);
            this.nonEquiCondition = nonEquiCondition;
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException, EndOfComputation {
            Object key = keyGetter.executeWith(frame, row);
            Object[] aggregated = TruffleBoundaryUtils.hashMapGet(left.map, key);
            if (aggregated != null && nonEquiCondition.execute(frame, aggregated, row)) {
                destination.execute(frame, Pair.create(aggregated, row));
            }
        }
    }

    static final class GroupJoinRightConsumerWithParentWithoutNonEquiCondition extends GroupJoinRightConsumerWithParent {
        public GroupJoinRightConsumerWithParentWithoutNonEquiCondition(DataCentricConsumerNode consumer,
                                                                       GroupJoinLeftIteratorConsumerNode left,
                                                                       RexTruffleNode keyGetter) {
            super(consumer, left, keyGetter);
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException, EndOfComputation {
            Object key = keyGetter.executeWith(frame, row);
            Object[] aggregated = TruffleBoundaryUtils.hashMapGet(left.map, key);
            if (aggregated != null) {
                destination.execute(frame, Pair.create(aggregated, row));
            }
        }
    }


}