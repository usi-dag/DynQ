package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.integers;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public abstract class IntegerRexTruffleNode extends RexTruffleNode {

    @Override
    public abstract Integer executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException;


    @Override
    public int runInt(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return executeWith(frame, input);
    }

}
