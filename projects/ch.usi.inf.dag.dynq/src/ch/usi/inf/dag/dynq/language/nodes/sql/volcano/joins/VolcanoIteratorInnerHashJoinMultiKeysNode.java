package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins.LeftIteratorConsumerFinalListMapMultiKeysNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins.RightIteratorConsumerFinalListMapNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ArrayRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing.ArrayBox;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing.ArrayBoxHashNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import ch.usi.inf.dag.dynq.structures.AppendableLinkedList;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.graalvm.collections.Pair;

import java.util.List;


public class VolcanoIteratorInnerHashJoinMultiKeysNode extends VolcanoIteratorNode {

    @Child
    VolcanoIteratorNode buildInput;

    @Child
    VolcanoIteratorNode probeInput;

    @Child
    ArrayRexTruffleNode buildKeyGetter;

    @Child
    ArrayRexTruffleNode probeKeyGetter;

    public static VolcanoIteratorInnerHashJoinMultiKeysNode create(VolcanoIteratorNode buildInput,
                                                                   VolcanoIteratorNode probeInput,
                                                                   ArrayRexTruffleNode buildKeyGetter,
                                                                   ArrayRexTruffleNode probeKeyGetter) {
        return new VolcanoIteratorInnerHashJoinMultiKeysNode(buildInput, probeInput, buildKeyGetter, probeKeyGetter);
    }

    VolcanoIteratorInnerHashJoinMultiKeysNode(VolcanoIteratorNode buildInput,
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
        LeftIteratorConsumerFinalListMapMultiKeysNode leftConsumer =
                new LeftIteratorConsumerFinalListMapMultiKeysNode(buildKeyGetter);
        TruffleLinqExecutableNode left = buildInput.acceptConsumer(leftConsumer);
        RightIteratorConsumerNode rightConsumer = new RightIteratorConsumerNode(probeKeyGetter, leftConsumer, consumer);
        TruffleLinqExecutableNode right = probeInput.acceptConsumer(rightConsumer);
        return new TruffleLinqExecutableHashJoinNode(left, right);
    }

    static final class RightIteratorConsumerNode extends RightIteratorConsumerFinalListMapNode {

        @Child ArrayRexTruffleNode probeKeyGetter;
        @Child ArrayBoxHashNode boxHashNode;

        public RightIteratorConsumerNode(ArrayRexTruffleNode probeKeyGetter,
                                         LeftIteratorConsumerFinalListMapMultiKeysNode left,
                                         DataCentricConsumerNode consumerNode) {
            super(consumerNode,  left);
            this.probeKeyGetter = probeKeyGetter;
            this.boxHashNode = new ArrayBoxHashNode(probeKeyGetter.size());
        }

        @Override
        public void execute(VirtualFrame frame, Object row)
                throws InteropException, FrameSlotTypeException, EndOfComputation {
            Object[] data = probeKeyGetter.executeWith(frame, row);
            ArrayBox key = new ArrayBox(data, boxHashNode.hash(data));
            AppendableLinkedList rows = TruffleBoundaryUtils.hashMapGet(getMap(), key);
            if(rows != null) {
                for(Object left : rows) {
                    destination.execute(frame, Pair.create(left, row));
                }
            }
        }

    }
}
