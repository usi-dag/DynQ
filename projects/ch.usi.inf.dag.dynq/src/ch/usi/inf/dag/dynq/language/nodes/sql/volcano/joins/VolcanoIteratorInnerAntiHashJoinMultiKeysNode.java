package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins.LeftIteratorConsumerFinalMarkedListMapNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ArrayRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing.ArrayBox;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing.ArrayBoxHashNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import ch.usi.inf.dag.dynq.structures.FinalMarkedArrayList;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.graalvm.collections.Pair;

import java.util.List;


public final class VolcanoIteratorInnerAntiHashJoinMultiKeysNode extends VolcanoIteratorNode {

    @Child
    VolcanoIteratorNode buildInput;

    @Child
    VolcanoIteratorNode probeInput;

    @Child
    ArrayRexTruffleNode buildKeyGetter;

    @Child
    ArrayRexTruffleNode probeKeyGetter;


    public VolcanoIteratorInnerAntiHashJoinMultiKeysNode(VolcanoIteratorNode buildInput,
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
        LeftIteratorConsumerNode leftConsumer = new LeftIteratorConsumerNode(buildKeyGetter);
        TruffleLinqExecutableNode left = buildInput.acceptConsumer(leftConsumer);
        RightIteratorConsumerNode rightConsumer = new RightIteratorConsumerNode(probeKeyGetter, leftConsumer, consumer);
        TruffleLinqExecutableNode right = probeInput.acceptConsumer(rightConsumer);
        return new TruffleLinqExecutableHashJoinNode(left, right);
    }

    static final class LeftIteratorConsumerNode extends LeftIteratorConsumerFinalMarkedListMapNode {

        @Child private ArrayRexTruffleNode buildKeyGetter;
        @Child private ArrayBoxHashNode boxHashNode;


        LeftIteratorConsumerNode(ArrayRexTruffleNode buildKeyGetter) {
            this.buildKeyGetter = buildKeyGetter;
            this.boxHashNode = new ArrayBoxHashNode(buildKeyGetter.size());
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
            Object[] data = buildKeyGetter.executeWith(frame, row);
            ArrayBox key = new ArrayBox(data, boxHashNode.hash(data));
            insert(key, row);
        }
    }

    static final class RightIteratorConsumerNode extends DataCentricConsumerWithDestinationNode {

        private final LeftIteratorConsumerNode left;

        @Child ArrayRexTruffleNode probeKeyGetter;
        @Child ArrayBoxHashNode boxHashNode;

        public RightIteratorConsumerNode(ArrayRexTruffleNode probeKeyGetter, LeftIteratorConsumerNode left,
                                         DataCentricConsumerNode consumerNode) {
            super(consumerNode);
            this.probeKeyGetter = probeKeyGetter;
            this.left = left;
            this.boxHashNode = new ArrayBoxHashNode(probeKeyGetter.size());
        }

        @Override
        public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
            Object[] data = probeKeyGetter.executeWith(frame, row);
            ArrayBox key = new ArrayBox(data, boxHashNode.hash(data));
            FinalMarkedArrayList rows = TruffleBoundaryUtils.hashMapGet(left.getMap(), key);
            if (rows != null) {
                rows.marked = true;
            }
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            try {
                for(FinalMarkedArrayList rows : left.getMap().values()) {
                    if(!rows.marked) {
                        for (int i = 0; i < rows.size(); i++) {
                            destination.execute(frame, rows.get(i));
                        }
                    }
                }
            } catch (EndOfComputation ignored){}
            left.freeMap();
            return destination.getFinalizedState(frame);
        }
    }

}