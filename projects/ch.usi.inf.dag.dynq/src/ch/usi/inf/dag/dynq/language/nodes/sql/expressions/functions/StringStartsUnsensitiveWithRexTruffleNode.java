package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.functions;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanRexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class StringStartsUnsensitiveWithRexTruffleNode extends BooleanRexTruffleNode {

    @Child private RexTruffleNode valueGetter;
    @Child private RexTruffleNode patternGetter;

    public StringStartsUnsensitiveWithRexTruffleNode(RexTruffleNode valueGetter, RexTruffleNode patternGetter) {
        this.valueGetter = valueGetter;
        this.patternGetter = patternGetter;
    }

    @Override
    public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        String pattern = patternGetter.runString(frame, input);
        String value = valueGetter.runString(frame, input);
        if(pattern.length() > value.length()) {
            return false;
        }
        return value.toUpperCase().startsWith(pattern.toUpperCase());
    }

}
