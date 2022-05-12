package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.groupjoin;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.FinalizerFillList;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.AggregationMultipleAggregatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.graalvm.collections.Pair;

import java.util.HashMap;
import java.util.LinkedList;


public final class VolcanoIteratorGroupInnerJoinSingleKeyNode extends VolcanoIteratorNode {

    @Child private VolcanoIteratorNode leftChild;
    @Child private VolcanoIteratorNode rightChild;

    @Child private RexTruffleNode buildKeyGetter;
    @Child private RexTruffleNode probeKeyGetter;

    @Child private AggregationMultipleAggregatorNode aggregatorNode;

    public VolcanoIteratorGroupInnerJoinSingleKeyNode(VolcanoIteratorNode leftChild,
                                                      VolcanoIteratorNode rightChild,
                                                      RexTruffleNode buildKeyGetter,
                                                      RexTruffleNode probeKeyGetter,
                                                      AggregationMultipleAggregatorNode aggregatorNode) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.buildKeyGetter = buildKeyGetter;
        this.probeKeyGetter = probeKeyGetter;
        this.aggregatorNode = aggregatorNode;
    }

    @Override
    public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
        GroupJoinLeftIteratorConsumerNode leftConsumer = new GroupJoinLeftIteratorConsumerNode(buildKeyGetter);
        TruffleLinqExecutableNode left = leftChild.acceptConsumer(leftConsumer);

        DataCentricConsumerNode myConsumer = (consumer == null || consumer instanceof FinalizerFillList)
                ? new GroupJoinRightConsumerWithNoParentNode()
                : new GroupJoinRightConsumerWithParent(consumer, leftConsumer, probeKeyGetter, aggregatorNode);

        TruffleLinqExecutableNode right = rightChild.acceptConsumer(myConsumer);

        return new TruffleLinqExecutableHashJoinNode(left, right);
    }

    private static final class GroupJoinLeftIteratorConsumerNode extends DataCentricConsumerNode {

        @Child private RexTruffleNode buildKeyGetter;

        HashGroupJoinByState state;

        public GroupJoinLeftIteratorConsumerNode(RexTruffleNode buildKeyGetter) {
            this.buildKeyGetter = buildKeyGetter;
        }

        @Override
        public void init(VirtualFrame frame) {
            state = new HashGroupJoinByState();
        }

        @Override
        public void init(VirtualFrame frame, int exactSize) {
            state = new HashGroupJoinByState(exactSize);
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
            Object key = buildKeyGetter.executeWith(frame, row);
            getOrCompute(key).leftElements.add(row);
        }

        @CompilerDirectives.TruffleBoundary(allowInlining = true)
        private GroupJoinState getOrCompute(Object key) {
            return state.computeIfAbsent(key, x -> new GroupJoinState());
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) {
            return null;
        }

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


    private static final class GroupJoinRightConsumerWithParent extends DataCentricConsumerWithDestinationNode {
        private final GroupJoinLeftIteratorConsumerNode left;

        @Child private RexTruffleNode probeKeyGetter;
        @Child private AggregationMultipleAggregatorNode aggregatorNode;


        public GroupJoinRightConsumerWithParent(DataCentricConsumerNode destination,
                                                GroupJoinLeftIteratorConsumerNode left,
                                                RexTruffleNode probeKeyGetter,
                                                AggregationMultipleAggregatorNode aggregatorNode) {
            super(destination);
            this.left = left;
            this.probeKeyGetter = probeKeyGetter;
            this.aggregatorNode = aggregatorNode;
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
            Object key = probeKeyGetter.executeWith(frame, row);
            HashGroupJoinByState state = left.state;
            GroupJoinState group = TruffleBoundaryUtils.hashMapGet(state, key);
            if(group != null) {
                // TODO maybe better executing aggregatorNode.getInitialState() in left node
                if(group.group == null) {
                    group.group = aggregatorNode.getInitialState();
                    group.group[0] = key;
                }
                for (Object left : group.leftElements) aggregatorNode.aggregate(frame, group.group, Pair.create(left, row));
            }
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            HashGroupJoinByState groups = left.state;
            try {
                for (GroupJoinState preRow : groups.values()) {
                    destination.execute(frame, aggregatorNode.finalize(frame, preRow.group));
                }
            } catch (EndOfComputation ignored) {}
            return destination.getFinalizedState(frame);
        }
    }

    static private final class GroupJoinState {
        Object[] group;
        final LinkedList<Object> leftElements = new LinkedList<>();
    }
    static private final class HashGroupJoinByState extends HashMap<Object, GroupJoinState> {
        private static final long serialVersionUID = 1433950348018053348L;

        public HashGroupJoinByState(int initialCapacity) {
            super(initialCapacity);
        }

        public HashGroupJoinByState() {
        }
    }
}
