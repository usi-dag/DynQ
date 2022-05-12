package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.BinaryJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import ch.usi.inf.dag.dynq.structures.AppendableLinkedList;
import ch.usi.inf.dag.dynq.structures.FinalListMap;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.graalvm.collections.Pair;

import java.util.HashSet;
import java.util.List;


public final class VolcanoIteratorInnerSemiHashJoinWithNonEquiJoinConditionNode extends VolcanoIteratorNode {

    @Child
    VolcanoIteratorNode buildInput;

    @Child
    VolcanoIteratorNode probeInput;

    @Child
    RexTruffleNode buildKeyGetter;

    @Child
    RexTruffleNode probeKeyGetter;

    @Child
    BinaryJoinCondition nonEquiCondition;


    public VolcanoIteratorInnerSemiHashJoinWithNonEquiJoinConditionNode(VolcanoIteratorNode buildInput,
                                                                        VolcanoIteratorNode probeInput,
                                                                        RexTruffleNode buildKeyGetter,
                                                                        RexTruffleNode probeKeyGetter,
                                                                        BinaryJoinCondition nonEquiCondition) {
        this.buildInput = buildInput;
        this.probeInput = probeInput;
        this.buildKeyGetter = buildKeyGetter;
        this.probeKeyGetter = probeKeyGetter;
        this.nonEquiCondition = nonEquiCondition;
    }


    @Override
    public boolean isMaterializerNode() {
        return true;
    }

    @Override
    public Class<?> getMaterializerClass() {
        return List.class;
    }

    @Override
    public Class<?> getOutputRowJavaType() {
        return Pair.class;
    }

    @Override
    public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
        // TODO note: currently unused -- recheck if we will use this
        LeftIteratorConsumerNode leftConsumer = new LeftIteratorConsumerNode(buildKeyGetter);
        TruffleLinqExecutableNode left = buildInput.acceptConsumer(leftConsumer);
        RightIteratorConsumerNode rightConsumer = new RightIteratorConsumerNode(
                probeKeyGetter, leftConsumer, consumer, nonEquiCondition);
        TruffleLinqExecutableNode right = probeInput.acceptConsumer(rightConsumer);
        return new TruffleLinqExecutableHashJoinNode(left, right);
    }

    static final class LeftIteratorConsumerNode extends DataCentricConsumerNode {
        private FinalListMap map = new FinalListMap();

        @Child
        RexTruffleNode buildKeyGetter;

        public LeftIteratorConsumerNode(RexTruffleNode buildKeyGetter) {
            this.buildKeyGetter = buildKeyGetter;
        }

        @Override
        public void init(VirtualFrame frame) {
            map = new FinalListMap();
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
            map.insert(buildKeyGetter.executeWith(frame, row), row);
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) {
            return map;
        }
    }

    static final class RightIteratorConsumerNode extends DataCentricConsumerWithDestinationNode {

        private final LeftIteratorConsumerNode left;
        private HashSet<Object> visited = new HashSet<>();

        @Child RexTruffleNode probeKeyGetter;

        @Child BinaryJoinCondition nonEquiCondition;

        public RightIteratorConsumerNode(RexTruffleNode probeKeyGetter,
                                         LeftIteratorConsumerNode left,
                                         DataCentricConsumerNode consumerNode,
                                         BinaryJoinCondition nonEquiCondition) {
            super(consumerNode);
            this.probeKeyGetter = probeKeyGetter;
            this.left = left;
            this.nonEquiCondition = nonEquiCondition;
        }


        @Override
        public void init(VirtualFrame frame) {
            visited = new HashSet<>();
            super.init(frame);
        }

        @Override
        public void free(VirtualFrame frame) {
            visited = null;
            super.free(frame);
        }


        @Override
        public void execute(VirtualFrame frame, Object row)
                throws InteropException, FrameSlotTypeException, EndOfComputation {
            Object key = probeKeyGetter.executeWith(frame, row);
            AppendableLinkedList rows = TruffleBoundaryUtils.hashMapGet(left.map, key);
            if (rows != null) {
                for (Object buildRow : rows) {
                    if (nonEquiCondition.execute(frame, buildRow, row)) {
                        // TODO improve: very likely this if can be put before the *for loop*
                        //  i.e., either the full list has already be passed to consumer
                        //  or the full list must be passed to consumer
                        // TODO use marked like SemiHashJoin
                        if (!TruffleBoundaryUtils.hashSetContains(visited, buildRow)) {
                            TruffleBoundaryUtils.hashSetAdd(visited, buildRow);
                            destination.execute(frame, buildRow);
                        }
                    }
                }
            }
        }
    }
}