package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class IsNotNullRexTruffleNode extends BooleanRexTruffleNode {

    @Child
    RexTruffleNode itemGetter;

    public IsNotNullRexTruffleNode(RexTruffleNode stringGetter) {
        this.itemGetter = stringGetter;
    }

    @Override
    public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return itemGetter.executeWith(frame, input) != null;
    }

}

