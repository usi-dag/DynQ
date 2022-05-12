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

import java.util.ArrayList;
import java.util.HashMap;


public final class VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeRightAggNode extends VolcanoIteratorNode {

    @Child private VolcanoIteratorNode leftChild;
    @Child private VolcanoIteratorNode rightChild;

    @Child private RexTruffleNode buildKeyGetter;
    @Child private RexTruffleNode probeKeyGetter;

    @Child private AggregationMultipleAggregatorNode aggregatorNode;

    @Child
    BinaryJoinCondition nonEquiCondition;

    public VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeRightAggNode(VolcanoIteratorNode leftChild,
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
                new GroupJoinLeftIteratorConsumerNode(buildKeyGetter);
        TruffleLinqExecutableNode left = leftChild.acceptConsumer(leftConsumer);

        DataCentricConsumerNode myConsumer;
        if(consumer == null || consumer instanceof FinalizerFillList) {
            myConsumer = new GroupJoinRightConsumerWithNoParentNode();
        } else {
            myConsumer = nonEquiCondition == null
                    ? new GroupJoinRightConsumerWithParentWithoutNonEquiCondition(consumer, leftConsumer, probeKeyGetter, aggregatorNode)
                    : new GroupJoinRightConsumerWithParentWithNonEquiCondition(consumer, leftConsumer, probeKeyGetter, nonEquiCondition, aggregatorNode);
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
        HashMap<Object, ArrayList<FixLeftPair>> map;
        @Child private RexTruffleNode buildKeyGetter;

        public GroupJoinLeftIteratorConsumerNode(RexTruffleNode buildKeyGetter) {
            this.buildKeyGetter = buildKeyGetter;
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
            Object key = buildKeyGetter.executeWith(frame, row);
            // TODO truffleboundary
            ArrayList<FixLeftPair> related = computeIfAbsent(key);
            related.add(new FixLeftPair(row));
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
            return null;
        }

        @CompilerDirectives.TruffleBoundary
        ArrayList<FixLeftPair> computeIfAbsent(Object key) {
            return map.computeIfAbsent(key, x -> new ArrayList<>());
        }
    }

    static class GroupJoinRightConsumerWithParent extends DataCentricConsumerWithDestinationNode {

        @Child private RexTruffleNode keyGetter;
        @Child AggregationMultipleAggregatorNode aggregatorNode;
        GroupJoinLeftIteratorConsumerNode left;


        public GroupJoinRightConsumerWithParent(DataCentricConsumerNode consumer,
                                                GroupJoinLeftIteratorConsumerNode left,
                                                RexTruffleNode keyGetter,
                                                AggregationMultipleAggregatorNode aggregatorNode) {
            super(consumer);
            this.left = left;
            this.aggregatorNode = aggregatorNode;
            this.keyGetter = keyGetter;
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
            Object key = keyGetter.executeWith(frame, row);
            ArrayList<FixLeftPair> related = TruffleBoundaryUtils.hashMapGet(left.map, key);
            if(related != null) {
                for (int i = 0; i < related.size(); i++) {
                    FixLeftPair pair = related.get(i);
                    Object[] right = pair.right;
                    if(right == null) {
                        right = aggregatorNode.getInitialState();
                        right[0] = key;
                        pair.right = right;
                    }
                    aggregatorNode.aggregate(frame, right, row);
                }
            }
        }


    }

    static class FixLeftPair {
        final Object left;
        Object[] right;

        public FixLeftPair(Object left) {
            this.left = left;
        }
    }

    static final class GroupJoinRightConsumerWithParentWithNonEquiCondition extends GroupJoinRightConsumerWithParent {
        @Child
        BinaryJoinCondition nonEquiCondition;

        public GroupJoinRightConsumerWithParentWithNonEquiCondition(DataCentricConsumerNode consumer,
                                                                    GroupJoinLeftIteratorConsumerNode left,
                                                                    RexTruffleNode keyGetter,
                                                                    BinaryJoinCondition nonEquiCondition,
                                                                    AggregationMultipleAggregatorNode aggregatorNode) {
            super(consumer, left, keyGetter, aggregatorNode);
            this.nonEquiCondition = nonEquiCondition;
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            try {
                for(ArrayList<FixLeftPair> value : left.map.values()) {
                    for (int i = 0; i < value.size(); i++) {
                        FixLeftPair pair = value.get(i);
                        if(pair.right != null) {
                            Object[] rightResult = aggregatorNode.finalize(frame, pair.right);
                            if(nonEquiCondition.execute(frame, pair.left, rightResult)) {
                                destination.execute(frame, Pair.create(pair.left, rightResult));
                            }
                        }
                    }
                }
            } catch (EndOfComputation end) {}
            left.map = null;
            return destination.getFinalizedState(frame);
        }
    }

    static final class GroupJoinRightConsumerWithParentWithoutNonEquiCondition extends GroupJoinRightConsumerWithParent {
        public GroupJoinRightConsumerWithParentWithoutNonEquiCondition(DataCentricConsumerNode consumer,
                                                                    GroupJoinLeftIteratorConsumerNode left,
                                                                    RexTruffleNode keyGetter,
                                                                    AggregationMultipleAggregatorNode aggregatorNode) {
            super(consumer, left, keyGetter, aggregatorNode);
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            try {
                for(ArrayList<FixLeftPair> value : left.map.values()) {
                    for (int i = 0; i < value.size(); i++) {
                        FixLeftPair pair = value.get(i);
                        if(pair.right != null) {
                            Object[] rightResult = aggregatorNode.finalize(frame, pair.right);
                            destination.execute(frame, Pair.create(pair.left, rightResult));
                        }
                    }
                }
            } catch (EndOfComputation end) {}
            left.map = null;
            return destination.getFinalizedState(frame);
        }
    }

}
