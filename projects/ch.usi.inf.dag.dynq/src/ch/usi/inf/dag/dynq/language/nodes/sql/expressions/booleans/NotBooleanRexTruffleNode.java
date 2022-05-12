package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class NotBooleanRexTruffleNode extends BooleanRexTruffleNode {

    @Child
    private RexTruffleNode child;

    public NotBooleanRexTruffleNode(RexTruffleNode child) {
        this.child = child;
    }

    @Override
    public Boolean executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return runBoolean(frame, input);
    }

    @Override
    public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return !child.runBoolean(frame, input);
    }
    
}
