package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class BinaryContainerRexTruffleNode extends UntypedRexTruffleNode {

    @Child
    private BinaryRexForContainerTruffleNode op;

    @Child
    private RexTruffleNode left;

    @Child
    private RexTruffleNode right;


    public BinaryContainerRexTruffleNode(RexTruffleNode left, RexTruffleNode right, BinaryRexForContainerTruffleNode op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    public Object executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        Object l = left.executeWith(frame, input);
        Object r = right.executeWith(frame, input);
        return op.execute(l, r);
    }

    @Override
    public String explain() {
        return op.getClass().getSimpleName();
    }
}


