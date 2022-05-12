package ch.usi.inf.dag.dynq.language.nodes.sql.expressions;


import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.profiles.ConditionProfile;


public final class CaseRexTruffleNode extends RexTruffleNode {

    public static CaseRexTruffleNode create(RexTruffleNode condition, RexTruffleNode ifTrue, RexTruffleNode ifFalse) {
        return new CaseRexTruffleNode(condition, ifTrue, ifFalse);
    }

    @Child
    RexTruffleNode condition;

    @Child
    RexTruffleNode ifTrue;

    @Child
    RexTruffleNode ifFalse;

    private final ConditionProfile profiler = ConditionProfile.createCountingProfile();

    private CaseRexTruffleNode(RexTruffleNode condition, RexTruffleNode ifTrue, RexTruffleNode ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }


    @Override
    public Object executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        if(profiler.profile(condition.runBoolean(frame, input))) {
            return ifTrue.executeWith(frame, input);
        } else {
            return ifFalse.executeWith(frame, input);
        }
    }

}
