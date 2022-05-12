package ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public class OnlyLeftBinaryJoinCondition extends BinaryJoinCondition {

    @Child
    RexTruffleNode leftCondition;

    public OnlyLeftBinaryJoinCondition(RexTruffleNode leftCondition) {
        this.leftCondition = leftCondition;
    }

    @Override
    public boolean execute(VirtualFrame frame, Object left, Object right) throws InteropException, FrameSlotTypeException {
        return leftCondition.runBoolean(frame, left);
    }
}
