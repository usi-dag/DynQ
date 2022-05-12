package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins.LeftIteratorConsumerMarkOnGetMapNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins.LeftIteratorConsumerMarkOnGetMapSingleKeyNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins.RightIteratorConsumerMarkOnGetMapNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.structures.FinalMarkedArrayList;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.graalvm.collections.Pair;

import java.util.List;


public class VolcanoIteratorLeftHashJoinNode extends VolcanoIteratorNode {

    @Child
    VolcanoIteratorNode buildInput;

    @Child
    VolcanoIteratorNode probeInput;

    @Child
    RexTruffleNode buildKeyGetter;

    @Child
    RexTruffleNode probeKeyGetter;

    public static VolcanoIteratorLeftHashJoinNode create(VolcanoIteratorNode buildInput,
                                                         VolcanoIteratorNode probeInput,
                                                         RexTruffleNode buildKeyGetter,
                                                         RexTruffleNode probeKeyGetter) {
        return new VolcanoIteratorLeftHashJoinNode(buildInput, probeInput, buildKeyGetter, probeKeyGetter);
    }

    public VolcanoIteratorLeftHashJoinNode(VolcanoIteratorNode buildInput, VolcanoIteratorNode probeInput,
                                           RexTruffleNode buildKeyGetter, RexTruffleNode probeKeyGetter) {
        this.buildInput = buildInput;
        this.probeInput = probeInput;
        this.buildKeyGetter = buildKeyGetter;
        this.probeKeyGetter = probeKeyGetter;
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


    // TODO equals to inner join? -- almost, only small changes in left and right executable
    @Override
    public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
        LeftIteratorConsumerMarkOnGetMapNode leftConsumer =
                new LeftIteratorConsumerMarkOnGetMapSingleKeyNode(buildKeyGetter);
        TruffleLinqExecutableNode left = buildInput.acceptConsumer(leftConsumer);
        RightIteratorConsumerNode rightConsumer = new RightIteratorConsumerNode(probeKeyGetter, leftConsumer, consumer);
        TruffleLinqExecutableNode right = probeInput.acceptConsumer(rightConsumer);
        return new TruffleLinqExecutableHashJoinNode(left, right);
    }

    static final class RightIteratorConsumerNode extends RightIteratorConsumerMarkOnGetMapNode {

        @Child RexTruffleNode probeKeyGetter;

        public RightIteratorConsumerNode(RexTruffleNode probeKeyGetter,
                                         LeftIteratorConsumerMarkOnGetMapNode left,
                                         DataCentricConsumerNode consumerNode) {
            super(consumerNode, left);
            this.probeKeyGetter = probeKeyGetter;
        }

        @Override
        public void execute(VirtualFrame frame, Object row)
                throws InteropException, FrameSlotTypeException, EndOfComputation {
            Object key = probeKeyGetter.executeWith(frame, row);
            FinalMarkedArrayList rows = getMap().getAndMark(key);
            if (rows != null) {
                for (int i = 0; i < rows.size(); i++) {
                    destination.execute(frame, Pair.create(rows.get(i), row));
                }
            }
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            try {
                for(FinalMarkedArrayList missingLeft : getMap().values()) {
                    if(!missingLeft.marked) {
                        for (int i = 0; i < missingLeft.size(); i++) {
                            destination.execute(frame, Pair.createLeft(missingLeft.get(i)));
                        }
                    }
                }
            } catch (EndOfComputation ignored) {}
            return super.getFinalizedState(frame);
        }
    }
}
