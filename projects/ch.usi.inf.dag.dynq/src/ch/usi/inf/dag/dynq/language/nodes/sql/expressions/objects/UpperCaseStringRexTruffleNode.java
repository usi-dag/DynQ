package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;

public final class UpperCaseStringRexTruffleNode extends StringRexTruffleNode {

    @Child
    RexTruffleNode stringGetter;

    public UpperCaseStringRexTruffleNode(RexTruffleNode stringGetter) {
        this.stringGetter = stringGetter;
    }

    @Override
    public String executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return TruffleBoundaryUtils.stringUpper(stringGetter.runString(frame, input));
    }

}
