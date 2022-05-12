package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.functions;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;


public abstract class UDFCallerRexTruffleNode extends RexTruffleNode {

    private final Object udf;

    @Children
    RexTruffleNode[] valueGetters;

    UDFCallerRexTruffleNode(Object udf, RexTruffleNode[] valueGetters) {
        this.udf = udf;
        this.valueGetters = valueGetters;
    }

    @Specialization
    Object call(VirtualFrame frame, Object row,
                @CachedLibrary(limit = "1") InteropLibrary interopCall)
            throws InteropException, FrameSlotTypeException {
        return interopCall.execute(udf, getValues(frame, row));
    }

    @ExplodeLoop
    private Object[] getValues(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
        Object[] values = new Object[valueGetters.length];
        for (int i = 0; i < valueGetters.length; i++) {
            values[i] = valueGetters[i].executeWith(frame, row);
        }
        return values;
    }


}
