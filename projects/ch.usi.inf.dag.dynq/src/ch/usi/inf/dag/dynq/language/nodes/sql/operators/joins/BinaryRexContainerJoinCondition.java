package ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.BinaryRexForContainerTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;

public final class BinaryRexContainerJoinCondition extends BinaryJoinCondition implements Explainable {

    @Child
    BinaryRexForContainerTruffleNode op;

    @Child
    RexTruffleNode leftNode;

    @Child
    RexTruffleNode rightNode;

    public BinaryRexContainerJoinCondition(RexTruffleNode left, RexTruffleNode right, BinaryRexForContainerTruffleNode op) {
        this.leftNode = left;
        this.rightNode = right;
        this.op = op;
    }

    @Override
    public boolean execute(VirtualFrame frame, Object left, Object right) throws InteropException, FrameSlotTypeException {
        Object leftResult = leftNode.executeWith(frame, left);
        Object rightResult = rightNode.executeWith(frame, right);
        return (boolean) op.execute(leftResult, rightResult);
    }

}
