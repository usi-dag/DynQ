package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableHashJoinNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import ch.usi.inf.dag.dynq.structures.FinalMarkedArrayList;
import ch.usi.inf.dag.dynq.structures.FinalMarkedListMap;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.graalvm.collections.Pair;

import java.util.List;


public final class VolcanoIteratorInnerSemiHashJoinNode extends VolcanoIteratorNode {

    @Child
    VolcanoIteratorNode buildInput;

    @Child
    VolcanoIteratorNode probeInput;

    @Child
    RexTruffleNode buildKeyGetter;

    @Child
    RexTruffleNode probeKeyGetter;


    public VolcanoIteratorInnerSemiHashJoinNode(VolcanoIteratorNode buildInput, VolcanoIteratorNode probeInput,
                                                RexTruffleNode buildKeyGetter, RexTruffleNode probeKeyGetter) {
        this.buildInput = buildInput;
        this.probeInput = probeInput;
        this.buildKeyGetter = buildKeyGetter;
        this.probeKeyGetter = probeKeyGetter;
    }

    @Override
    public boolean isMaterializerNode() {
        return true; // TODO check
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

    static final class LeftIteratorConsumerNode extends DataCentricConsumerNode {
        private FinalMarkedListMap map = new FinalMarkedListMap();

        @Child
        RexTruffleNode buildKeyGetter;

        public LeftIteratorConsumerNode(RexTruffleNode buildKeyGetter) {
            this.buildKeyGetter = buildKeyGetter;
        }

        @Override
        public void init(VirtualFrame frame) {
            map = new FinalMarkedListMap();
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

        @Child RexTruffleNode probeKeyGetter;

        public RightIteratorConsumerNode(RexTruffleNode probeKeyGetter, LeftIteratorConsumerNode left,
                                         DataCentricConsumerNode consumerNode) {
            super(consumerNode);
            this.probeKeyGetter = probeKeyGetter;
            this.left = left;
        }

        @Override
        public void execute(VirtualFrame frame, Object row)
                throws InteropException, FrameSlotTypeException, EndOfComputation {
            Object key = probeKeyGetter.executeWith(frame, row);
            FinalMarkedArrayList rows = TruffleBoundaryUtils.hashMapGet(left.map, key);
            if (rows != null) {
                if(!rows.marked) {
                    rows.marked = true;
                    for (int i = 0; i < rows.size(); i++) {
                        destination.execute(frame, rows.get(i));
                    }
                }
            }
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            left.free(frame);
            return destination.getFinalizedState(frame);
        }
    }

}