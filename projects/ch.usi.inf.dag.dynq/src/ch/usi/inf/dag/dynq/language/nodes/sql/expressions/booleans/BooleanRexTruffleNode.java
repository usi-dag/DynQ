package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public abstract class BooleanRexTruffleNode extends RexTruffleNode {

    @Override // TODO final?
    public Boolean executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return runBoolean(frame, input);
    }

    @Override
    public abstract boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException;

}
