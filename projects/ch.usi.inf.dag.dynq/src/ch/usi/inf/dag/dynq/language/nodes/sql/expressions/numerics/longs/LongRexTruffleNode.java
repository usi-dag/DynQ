package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.longs;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public abstract class LongRexTruffleNode extends RexTruffleNode {

    @Override
    public abstract Long executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException;

    @Override
    public long runLong(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return executeWith(frame, input);
    }



    public static LongRexTruffleNode castAround(RexTruffleNode rexTruffleNode) {
        return new LongRexTruffleNode() {
            @Override
            public Long executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
                return runLong(frame, input);
            }

            @Override
            public long runLong(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
                return rexTruffleNode.runLong(frame, input);
            }
        };
    }


}
