package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins.LeftIteratorConsumerFinalListMapSingleKeyNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins.RightIteratorConsumerFinalListMapNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.BinaryJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import ch.usi.inf.dag.dynq.structures.AppendableLinkedList;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.graalvm.collections.Pair;

import java.util.List;


public class VolcanoIteratorInnerHashJoinWithNonEquiJoinConditionNode extends VolcanoIteratorNode {

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


    public static VolcanoIteratorInnerHashJoinWithNonEquiJoinConditionNode create(VolcanoIteratorNode buildInput,
                                                                                  VolcanoIteratorNode probeInput,
                                                                                  RexTruffleNode buildKeyGetter,
                                                                                  RexTruffleNode probeKeyGetter,
                                                                                  BinaryJoinCondition nonEquiCondition) {
        return new VolcanoIteratorInnerHashJoinWithNonEquiJoinConditionNode(buildInput, probeInput, buildKeyGetter, probeKeyGetter, nonEquiCondition);
    }

    VolcanoIteratorInnerHashJoinWithNonEquiJoinConditionNode(VolcanoIteratorNode buildInput,
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
        LeftIteratorConsumerFinalListMapSingleKeyNode leftConsumer =
                new LeftIteratorConsumerFinalListMapSingleKeyNode(buildKeyGetter);
        TruffleLinqExecutableNode left = buildInput.acceptConsumer(leftConsumer);
        RightIteratorConsumerNode rightConsumer = new RightIteratorConsumerNode(
                probeKeyGetter, leftConsumer, consumer, nonEquiCondition);
        TruffleLinqExecutableNode right = probeInput.acceptConsumer(rightConsumer);
        return new TruffleLinqExecutableHashJoinNode(left, right);
    }

    static final class RightIteratorConsumerNode extends RightIteratorConsumerFinalListMapNode {

        @Child RexTruffleNode probeKeyGetter;
        @Child BinaryJoinCondition nonEquiCondition;

        public RightIteratorConsumerNode(RexTruffleNode probeKeyGetter,
                                         LeftIteratorConsumerFinalListMapSingleKeyNode left,
                                         DataCentricConsumerNode consumerNode,
                                         BinaryJoinCondition nonEquiCondition) {
            super(consumerNode, left);
            this.probeKeyGetter = probeKeyGetter;
            this.nonEquiCondition = nonEquiCondition;
        }

        @Override
        public void execute(VirtualFrame frame, Object row)
                throws InteropException, FrameSlotTypeException, EndOfComputation {
            Object key = probeKeyGetter.executeWith(frame, row);
            AppendableLinkedList rows = TruffleBoundaryUtils.hashMapGet(getMap(), key);
            if(rows != null) {
                for(Object left : rows) {
                    if(nonEquiCondition.execute(frame, left, row)) {
                        destination.execute(frame, Pair.create(left, row));
                    }
                }
            }
        }
    }

}