package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;



public abstract class StringRexTruffleNode extends RexTruffleNode {

    @Override
    public abstract String executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException;

}
