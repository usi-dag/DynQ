package ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;

public final class BinaryEquiJoinCondition extends BinaryJoinCondition implements Explainable {

    @Child
    RexTruffleNode leftNode;

    @Child
    RexTruffleNode rightNode;

    public BinaryEquiJoinCondition(RexTruffleNode left, RexTruffleNode right) {
        this.leftNode = left;
        this.rightNode = right;
    }

    @Override
    public boolean execute(VirtualFrame frame, Object left, Object right) throws InteropException, FrameSlotTypeException {
        Object leftResult = leftNode.executeWith(frame, left);
        Object rightResult = rightNode.executeWith(frame, right);
        // TODO try using EqualsRexTruffleNode and check perf (it can be done in fitsSimpleJoinCondition as well)
        return leftResult.equals(rightResult);
    }

}
