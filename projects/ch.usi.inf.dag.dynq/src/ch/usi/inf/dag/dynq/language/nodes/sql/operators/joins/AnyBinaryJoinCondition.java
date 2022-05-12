package ch.usi.inf.dag.dynq.language.nodes.sql.operators.joins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.ConditionProfile;


public final class AnyBinaryJoinCondition extends BinaryJoinCondition {


    @Children
    private BinaryJoinCondition[] children;

    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private final ConditionProfile[] conditionProfiles;

    public AnyBinaryJoinCondition(BinaryJoinCondition[] children) {
        this.children = children;
        this.conditionProfiles = new ConditionProfile[children.length];
        for (int i = 0; i < children.length; i++) {
            conditionProfiles[i] = ConditionProfile.createCountingProfile();
        }
    }

    @ExplodeLoop
    @Override
    public boolean execute(VirtualFrame frame, Object left, Object right) throws InteropException, FrameSlotTypeException {
        for (int i = 0; i < children.length; i++) {
            BinaryJoinCondition expr = children[i];
            ConditionProfile profile = conditionProfiles[i];
            if(profile.profile(expr.execute(frame, left, right))) {
                return true;
            }
        }
        return false;
    }

}
