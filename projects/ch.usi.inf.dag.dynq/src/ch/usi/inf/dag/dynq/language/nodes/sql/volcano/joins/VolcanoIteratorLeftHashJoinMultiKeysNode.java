package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins.LeftIteratorConsumerMarkOnGetMapMultiKeysNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins.LeftIteratorConsumerMarkOnGetMapNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins.RightIteratorConsumerMarkOnGetMapNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ArrayRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing.ArrayBox;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing.ArrayBoxHashNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.structures.FinalMarkedArrayList;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.graalvm.collections.Pair;

import java.util.List;


public class VolcanoIteratorLeftHashJoinMultiKeysNode extends VolcanoIteratorNode {

    @Child
    VolcanoIteratorNode buildInput;

    @Child
    VolcanoIteratorNode probeInput;

    @Child
    ArrayRexTruffleNode buildKeyGetter;

    @Child
    ArrayRexTruffleNode probeKeyGetter;


    public static VolcanoIteratorLeftHashJoinMultiKeysNode create(VolcanoIteratorNode buildInput,
                                                                  VolcanoIteratorNode probeInput,
                                                                  ArrayRexTruffleNode buildKeyGetter,
                                                                  ArrayRexTruffleNode probeKeyGetter) {
        return new VolcanoIteratorLeftHashJoinMultiKeysNode(buildInput, probeInput, buildKeyGetter, probeKeyGetter);
    }

    VolcanoIteratorLeftHashJoinMultiKeysNode(VolcanoIteratorNode buildInput,
                                             VolcanoIteratorNode probeInput,
                                             ArrayRexTruffleNode buildKeyGetter,
                                             ArrayRexTruffleNode probeKeyGetter) {
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


    @Override
    public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
        LeftIteratorConsumerMarkOnGetMapNode leftConsumer =
                new LeftIteratorConsumerMarkOnGetMapMultiKeysNode(buildKeyGetter);
        TruffleLinqExecutableNode left = buildInput.acceptConsumer(leftConsumer);
        RightIteratorConsumerNode rightConsumer = new RightIteratorConsumerNode(probeKeyGetter, leftConsumer, consumer);
        TruffleLinqExecutableNode right = probeInput.acceptConsumer(rightConsumer);
        return new TruffleLinqExecutableHashJoinNode(left, right);
    }

    static final class RightIteratorConsumerNode extends RightIteratorConsumerMarkOnGetMapNode {

        @Child private ArrayRexTruffleNode probeKeyGetter;
        @Child private ArrayBoxHashNode boxHashNode;

        public RightIteratorConsumerNode(ArrayRexTruffleNode probeKeyGetter,
                                         LeftIteratorConsumerMarkOnGetMapNode left,
                                         DataCentricConsumerNode consumerNode) {
            super(consumerNode, left);
            this.probeKeyGetter = probeKeyGetter;
            this.boxHashNode = new ArrayBoxHashNode(probeKeyGetter.size());
        }

        @Override
        public void execute(VirtualFrame frame, Object row)
                throws InteropException, FrameSlotTypeException, EndOfComputation {
            Object[] data = probeKeyGetter.executeWith(frame, row);
            ArrayBox key = new ArrayBox(data, boxHashNode.hash(data));
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
