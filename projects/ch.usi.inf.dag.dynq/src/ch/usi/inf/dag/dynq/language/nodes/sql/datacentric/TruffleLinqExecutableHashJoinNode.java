package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric;


import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class TruffleLinqExecutableHashJoinNode extends TruffleLinqExecutableNode {
    @Child private TruffleLinqExecutableNode leftExecutable;
    @Child private TruffleLinqExecutableNode rightExecutable;

    public TruffleLinqExecutableHashJoinNode(TruffleLinqExecutableNode leftConsumer, TruffleLinqExecutableNode rightConsumer) {
        this.leftExecutable = leftConsumer;
        this.rightExecutable = rightConsumer;
    }

    @Override
    public Object execute(VirtualFrame frame) throws InteropException, FrameSlotTypeException {
        leftExecutable.execute(frame);
        return rightExecutable.execute(frame);
    }
}