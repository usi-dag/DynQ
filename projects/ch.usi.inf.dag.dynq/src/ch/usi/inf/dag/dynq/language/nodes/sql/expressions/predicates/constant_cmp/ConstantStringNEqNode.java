package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.constant_cmp;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.ExplodeLoop;


public final class ConstantStringNEqNode extends BooleanRexTruffleNode {


    private final String constant;

    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private final char[] chars;

    @Child
    RexTruffleNode inputGetter;
    public ConstantStringNEqNode(String constant, RexTruffleNode inputGetter) {
        this.constant = constant;
        this.chars = constant.toCharArray();
        this.inputGetter = inputGetter;
    }

    @Override
    @ExplodeLoop
    public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        String inputString = inputGetter.runString(frame, input);
        if(chars.length != inputString.length()) {
            return true;
        }
        for (int i = 0; i < chars.length; i++) {
            if(chars[i] != TruffleBoundaryUtils.getCharAt(inputString, i)) return true;
        }
        return false;
    }

    @Override
    public String explain() {
        return super.explain() + "(" + constant + ")";
    }

}
