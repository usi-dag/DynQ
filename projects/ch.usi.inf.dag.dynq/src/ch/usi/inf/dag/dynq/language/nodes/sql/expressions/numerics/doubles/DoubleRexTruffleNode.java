package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.doubles;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public abstract class DoubleRexTruffleNode extends RexTruffleNode {

    @Override
    public abstract Double executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException;

    @Override
    public double runDouble(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return executeWith(frame, input);
    }

}
