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
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.graalvm.collections.Pair;

import java.util.HashMap;
import java.util.LinkedList;


public final class VolcanoIteratorGroupLeftJoinSingleKeyNode extends VolcanoIteratorNode {

    @Child private VolcanoIteratorNode leftChild;
    @Child private VolcanoIteratorNode rightChild;

    @Child private RexTruffleNode buildKeyGetter;
    @Child private RexTruffleNode probeKeyGetter;

    @Child private AggregationMultipleAggregatorNode aggregatorNode;

    public VolcanoIteratorGroupLeftJoinSingleKeyNode(VolcanoIteratorNode leftChild,
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
        GroupJoinLeftIteratorConsumerNode leftConsumer =
                new GroupJoinLeftIteratorConsumerNode(buildKeyGetter, aggregatorNode);
        TruffleLinqExecutableNode left = leftChild.acceptConsumer(leftConsumer);

        DataCentricConsumerNode myConsumer = (consumer == null || consumer instanceof FinalizerFillList)
                ? new GroupJoinRightConsumerWithNoParentNode()
                : new GroupJoinRightConsumerWithParent(consumer, leftConsumer, probeKeyGetter, aggregatorNode);

        TruffleLinqExecutableNode right = rightChild.acceptConsumer(myConsumer);

        leftConsumer.setRightConsumer((GroupJoinRightConsumerWithParent)myConsumer);
        return new TruffleLinqExecutableHashJoinNode(left, right);
    }


    private static final class GroupJoinLeftIteratorConsumerNode extends DataCentricConsumerNode {

        @Child private RexTruffleNode buildKeyGetter;
        @Child private AggregationMultipleAggregatorNode aggregatorNode;

        private final ConditionProfile mapMissingKeyProfiler = ConditionProfile.createCountingProfile();

        HashGroupLeftJoinByState state;

        GroupJoinRightConsumerWithParent rightConsumer;

        public void setRightConsumer(GroupJoinRightConsumerWithParent rightConsumer) {
            this.rightConsumer = rightConsumer;
        }

        public GroupJoinLeftIteratorConsumerNode(RexTruffleNode buildKeyGetter,
                                                 AggregationMultipleAggregatorNode aggregatorNode) {
            this.buildKeyGetter = buildKeyGetter;
            this.aggregatorNode = aggregatorNode;
        }

        @Override
        public void init(VirtualFrame frame) {
            state = new HashGroupLeftJoinByState();
        }

        @Override
        public void init(VirtualFrame frame, int exactSize) {
            state = new HashGroupLeftJoinByState(exactSize);
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
            Object key = buildKeyGetter.executeWith(frame, row);
            GroupLeftJoinState state = TruffleBoundaryUtils.hashMapGet(this.state, key);
            if(mapMissingKeyProfiler.profile(state == null)) {
                Object[] groupData = aggregatorNode.getInitialState();
                groupData[0] = key;
                TruffleBoundaryUtils.hashMapPut(this.state, key, new GroupLeftJoinStateSingleElement(groupData, row));
            } else if(state instanceof GroupLeftJoinStateMultipleElements) {
                ((GroupLeftJoinStateMultipleElements)state).leftElements.add(row);
            } else {
                GroupLeftJoinStateMultipleElements els = new GroupLeftJoinStateMultipleElements(state.group);
                els.leftElements.add(((GroupLeftJoinStateSingleElement)state).element);
                els.leftElements.add(row);
                TruffleBoundaryUtils.hashMapPut(this.state, key, els);
                rightConsumer.setNotAllSingleElement();
            }
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

        @CompilerDirectives.CompilationFinal boolean isAllSingleElement = true;

        public void setNotAllSingleElement() {
            isAllSingleElement = false;
        }

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
            HashGroupLeftJoinByState state = left.state;
            GroupLeftJoinState group = TruffleBoundaryUtils.hashMapGet(state, key);
            if(group != null) {
                group.marked = true;
                if(isAllSingleElement || group instanceof GroupLeftJoinStateSingleElement) {
                    aggregatorNode.aggregate(frame, group.group, Pair.create(((GroupLeftJoinStateSingleElement)group).element, row));
                } else {
                    for (Object left : ((GroupLeftJoinStateMultipleElements)group).leftElements) {
                        aggregatorNode.aggregate(frame, group.group, Pair.create(left, row));
                    }
                }
            }
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            try {
                for (GroupLeftJoinState preRow : left.state.values()) {
                    if(!preRow.marked) {
                        if(isAllSingleElement || preRow instanceof GroupLeftJoinStateSingleElement) {
                            aggregatorNode.aggregate(frame, preRow.group, Pair.createLeft(((GroupLeftJoinStateSingleElement)preRow).element));
                        } else {
                            for (Object left : ((GroupLeftJoinStateMultipleElements)preRow).leftElements) {
                                aggregatorNode.aggregate(frame, preRow.group, Pair.createLeft(left));
                            }
                        }
                    }
                    destination.execute(frame, aggregatorNode.finalize(frame, preRow.group));
                }
            } catch (EndOfComputation ignored) {}
            left.state = null;
            return destination.getFinalizedState(frame);
        }
    }


    static abstract private class GroupLeftJoinState {
        boolean marked = false;
        final Object[] group;
        public GroupLeftJoinState(Object[] group) {
            this.group = group;
        }
    }

    static private final class GroupLeftJoinStateSingleElement extends GroupLeftJoinState {
        final Object element;
        public GroupLeftJoinStateSingleElement(Object[] group, Object element) {
            super(group);
            this.element = element;
        }
    }

    static private final class GroupLeftJoinStateMultipleElements extends GroupLeftJoinState {
        final LinkedList<Object> leftElements = new LinkedList<>();
        public GroupLeftJoinStateMultipleElements(Object[] group) {
            super(group);
        }
    }

    static private final class HashGroupLeftJoinByState extends HashMap<Object, GroupLeftJoinState> {
        private static final long serialVersionUID = 5826577958657344391L;

        public HashGroupLeftJoinByState(int initialCapacity) {
            super(initialCapacity);
        }

        public HashGroupLeftJoinByState() {
        }
    }
}