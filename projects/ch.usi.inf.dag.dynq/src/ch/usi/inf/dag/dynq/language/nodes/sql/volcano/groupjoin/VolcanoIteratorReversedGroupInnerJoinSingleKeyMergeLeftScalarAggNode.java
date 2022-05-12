package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.groupjoin;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.FinalizerFillList;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.EqualsRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.EqualsRexTruffleNodeGen;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.aggregations.AggregationScalarAggregatorNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.BinaryJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.graalvm.collections.Pair;


public final class VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeLeftScalarAggNode extends VolcanoIteratorNode {

    @Child private VolcanoIteratorNode leftChild;
    @Child private VolcanoIteratorNode rightChild;

    @Child private RexTruffleNode probeKeyGetter;

    @Child private AggregationScalarAggregatorNode aggregatorNode;

    @Child
    BinaryJoinCondition nonEquiCondition;

    public VolcanoIteratorReversedGroupInnerJoinSingleKeyMergeLeftScalarAggNode(VolcanoIteratorNode leftChild,
                                                                                VolcanoIteratorNode rightChild,
                                                                                RexTruffleNode probeKeyGetter,
                                                                                BinaryJoinCondition joinCondition,
                                                                                AggregationScalarAggregatorNode aggregatorNode) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.probeKeyGetter = probeKeyGetter;
        this.aggregatorNode = aggregatorNode;
        this.nonEquiCondition = joinCondition;
    }

    @Override
    public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
        GroupJoinLeftIteratorConsumerNode leftConsumer =
                new GroupJoinLeftIteratorConsumerNode(aggregatorNode);
        TruffleLinqExecutableNode left = leftChild.acceptConsumer(leftConsumer);

        DataCentricConsumerNode myConsumer = (consumer == null || consumer instanceof FinalizerFillList)
                ? new GroupJoinRightConsumerWithNoParentNode()
                : new GroupJoinRightConsumerWithParent(consumer, leftConsumer, nonEquiCondition, probeKeyGetter);

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
        @Child private AggregationScalarAggregatorNode aggregatorNode;
        Object state;

        public GroupJoinLeftIteratorConsumerNode(AggregationScalarAggregatorNode aggregatorNode) {
            this.aggregatorNode = aggregatorNode;
            state = aggregatorNode.getInitialState();
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
            state = aggregatorNode.aggregate(frame, state, row);
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) {
            state = aggregatorNode.finalize(frame, state);
            return null;
        }

        @Override
        public void init(VirtualFrame frame) {
            super.init(frame);
            state = aggregatorNode.getInitialState();
        }
    }

    static class GroupJoinRightConsumerWithParent extends DataCentricConsumerWithDestinationNode {

        @Child private RexTruffleNode keyGetter;
        GroupJoinLeftIteratorConsumerNode left;

        @Child
        BinaryJoinCondition nonEquiCondition;

        @Child
        EqualsRexTruffleNode equalsRexTruffleNode = EqualsRexTruffleNodeGen.create();

        public GroupJoinRightConsumerWithParent(DataCentricConsumerNode consumer,
                                                GroupJoinLeftIteratorConsumerNode left,
                                                BinaryJoinCondition nonEquiCondition,
                                                RexTruffleNode keyGetter) {
            super(consumer);
            this.left = left;
            this.keyGetter = keyGetter;
            this.nonEquiCondition = nonEquiCondition;
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException, EndOfComputation {
            Object key = keyGetter.executeWith(frame, row);
            if ((boolean)equalsRexTruffleNode.execute(left.state, key)) {
                if(nonEquiCondition == null || nonEquiCondition.execute(frame, left.state, row)) {
                    destination.execute(frame, Pair.create(left.state, row));
                }
            }
        }
    }
}
