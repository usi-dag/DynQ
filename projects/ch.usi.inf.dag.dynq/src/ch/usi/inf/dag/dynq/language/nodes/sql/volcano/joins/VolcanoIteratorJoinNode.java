package ch.usi.inf.dag.dynq.language.nodes.sql.volcano.joins;

import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.DataCentricConsumerWithDestinationNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.EndOfComputation;
import ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.TruffleLinqExecutableNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins.BinaryJoinCondition;
import ch.usi.inf.dag.dynq.language.nodes.sql.volcano.VolcanoIteratorNode;
import ch.usi.inf.dag.dynq.structures.FinalArrayList;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import org.graalvm.collections.Pair;

import java.util.ArrayList;
import java.util.List;

public class VolcanoIteratorJoinNode extends VolcanoIteratorNode {

    @Child
    VolcanoIteratorNode leftChild;

    @Child
    VolcanoIteratorNode rightChild;

    @Child
    BinaryJoinCondition joinCondition;

    private final boolean produceSingleValueOnLeft;


    public static VolcanoIteratorJoinNode create(VolcanoIteratorNode leftChild,
                                                 VolcanoIteratorNode rightChild,
                                                 boolean produceSingleValueOnLeft) {
        return new VolcanoIteratorJoinNode(leftChild, rightChild, null, produceSingleValueOnLeft);
    }

    VolcanoIteratorJoinNode(VolcanoIteratorNode leftChild,
                            VolcanoIteratorNode rightChild,
                            BinaryJoinCondition joinCondition,
                            boolean produceSingleValueOnLeft) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.joinCondition = joinCondition;
        this.produceSingleValueOnLeft = produceSingleValueOnLeft;
    }

    public void setJoinCondition(BinaryJoinCondition joinCondition) {
        this.joinCondition = joinCondition;
    }

    @Override
    public Class<?> getOutputRowJavaType() {
        return Pair.class;
    }

    @Override
    public boolean isMaterializerNode() {
        return leftChild.isMaterializerNode();
    }

    @Override
    public Class<?> getMaterializerClass() {
        return List.class;
    }


    @Override
    public TruffleLinqExecutableNode acceptConsumer(DataCentricConsumerNode consumer) {
        if(produceSingleValueOnLeft) {
            SingleObjectHolderDataCentricConsumerNode leftConsumer = new SingleObjectHolderDataCentricConsumerNode();
            TruffleLinqExecutableNode left = leftChild.acceptConsumer(leftConsumer);
            DataCentricConsumerNode rightConsumer = joinCondition == null
                    ? new RightIteratorSingleValueLeftConsumerWithoutEquiconditionNode(leftConsumer, consumer)
                    : new RightIteratorSingleValueLeftConsumerNode(leftConsumer, joinCondition, consumer);
            TruffleLinqExecutableNode right = rightChild.acceptConsumer(rightConsumer);
            return new TruffleLinqExecutableJoinNode(left, right);

        } else {
            MyFillListDataCentricConsumerNode leftConsumer = new MyFillListDataCentricConsumerNode();
            TruffleLinqExecutableNode left = leftChild.acceptConsumer(leftConsumer);
            DataCentricConsumerNode rightConsumer = joinCondition == null
                    ? new RightIteratorConsumerWithoutEquiconditionNode(leftConsumer, consumer)
                    : new RightIteratorConsumerNode(leftConsumer, joinCondition, consumer);
            TruffleLinqExecutableNode right = rightChild.acceptConsumer(rightConsumer);
            return new TruffleLinqExecutableJoinNode(left, right);
        }
    }


    static final class TruffleLinqExecutableJoinNode extends TruffleLinqExecutableNode {
        @Child TruffleLinqExecutableNode leftExecutable;
        @Child TruffleLinqExecutableNode rightExecutable;


        public TruffleLinqExecutableJoinNode(TruffleLinqExecutableNode leftConsumer, TruffleLinqExecutableNode rightConsumer) {
            this.leftExecutable = leftConsumer;
            this.rightExecutable = rightConsumer;
        }

        @Override
        public Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            leftExecutable.execute(frame);
            return rightExecutable.execute(frame);
        }
    }


    static final class RightIteratorConsumerWithoutEquiconditionNode extends DataCentricConsumerWithDestinationNode {

        final MyFillListDataCentricConsumerNode left;

        public RightIteratorConsumerWithoutEquiconditionNode(MyFillListDataCentricConsumerNode left,
                                                             DataCentricConsumerNode consumerNode) {
            super(consumerNode);
            this.left = left;
        }

        @Override
        public void execute(VirtualFrame frame, Object rightRow)
                throws InteropException, FrameSlotTypeException, EndOfComputation {
            FinalArrayList leftRows = left.state;
            for(int i = 0; i < leftRows.size(); i++) {
                Object leftRow = leftRows.get(i);
                destination.execute(frame, Pair.create(leftRow, rightRow));
            }
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            left.state = null;
            return super.getFinalizedState(frame);
        }
    }

    static final class RightIteratorConsumerNode extends DataCentricConsumerWithDestinationNode {

        final MyFillListDataCentricConsumerNode left;

        @Child BinaryJoinCondition joinCondition;

        public RightIteratorConsumerNode(MyFillListDataCentricConsumerNode left,
                                         BinaryJoinCondition joinCondition,
                                         DataCentricConsumerNode consumerNode) {
            super(consumerNode);
            this.left = left;
            this.joinCondition = joinCondition;
        }

        @Override
        public void execute(VirtualFrame frame, Object rightRow)
                throws InteropException, FrameSlotTypeException, EndOfComputation {
            FinalArrayList leftRows = left.state;
            for(int i = 0; i < leftRows.size(); i++) {
                Object leftRow = leftRows.get(i);
                if(joinCondition == null || joinCondition.execute(frame, leftRow, rightRow)) {
                    destination.execute(frame, Pair.create(leftRow, rightRow));
                }
            }
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            left.state = null;
            return super.getFinalizedState(frame);
        }
    }


    static final class RightIteratorSingleValueLeftConsumerWithoutEquiconditionNode extends DataCentricConsumerWithDestinationNode {

        final SingleObjectHolderDataCentricConsumerNode left;

        public RightIteratorSingleValueLeftConsumerWithoutEquiconditionNode(SingleObjectHolderDataCentricConsumerNode left,
                                                                            DataCentricConsumerNode consumerNode) {
            super(consumerNode);
            this.left = left;
        }

        @Override
        public void execute(VirtualFrame frame, Object rightRow)
                throws InteropException, FrameSlotTypeException, EndOfComputation {
            destination.execute(frame, Pair.create(left.state, rightRow));
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            left.state = null;
            return super.getFinalizedState(frame);
        }
    }

    static final class RightIteratorSingleValueLeftConsumerNode extends DataCentricConsumerWithDestinationNode {

        final SingleObjectHolderDataCentricConsumerNode left;

        @Child BinaryJoinCondition joinCondition;

        public RightIteratorSingleValueLeftConsumerNode(SingleObjectHolderDataCentricConsumerNode left,
                                                        BinaryJoinCondition joinCondition,
                                                        DataCentricConsumerNode consumerNode) {
            super(consumerNode);
            this.left = left;
            this.joinCondition = joinCondition;
        }

        @Override
        public void execute(VirtualFrame frame, Object rightRow)
                throws InteropException, FrameSlotTypeException, EndOfComputation {
            Object leftRow = left.state;
            if(joinCondition == null || joinCondition.execute(frame, leftRow, rightRow)) {
                destination.execute(frame, Pair.create(leftRow, rightRow));
            }
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
            left.state = null;
            return super.getFinalizedState(frame);
        }
    }


    private static final class MyFillListDataCentricConsumerNode extends DataCentricConsumerNode {
        static private final int INITIAL = 1024;

        FinalArrayList state;

        @Override
        public void execute(VirtualFrame frame, Object row) {
            state.add(row);
        }

        @Override
        public ArrayList<Object> getFinalizedState(VirtualFrame frame) {
            return state;
        }

        @Override
        public void init(VirtualFrame frame) {
            state = new FinalArrayList(INITIAL);
        }

        @Override
        public void init(VirtualFrame frame, int exactSize) {
            state = new FinalArrayList(exactSize);
        }
    }


    private static final class SingleObjectHolderDataCentricConsumerNode extends DataCentricConsumerNode {
        Object state;

        @Override
        public void execute(VirtualFrame frame, Object row) {
            state = row;
        }

        @Override
        public Object getFinalizedState(VirtualFrame frame) {
            return state;
        }
    }
}
