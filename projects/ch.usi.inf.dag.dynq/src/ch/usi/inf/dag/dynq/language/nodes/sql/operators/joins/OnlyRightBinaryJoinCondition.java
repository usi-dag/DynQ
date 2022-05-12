package ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public class OnlyRightBinaryJoinCondition extends BinaryJoinCondition {

    @Child
    RexTruffleNode rightCondition;

    public OnlyRightBinaryJoinCondition(RexTruffleNode rightCondition) {
        this.rightCondition = rightCondition;
    }

    @Override
    public boolean execute(VirtualFrame frame, Object left, Object right) throws InteropException, FrameSlotTypeException {
        return rightCondition.runBoolean(frame, right);
    }
}
