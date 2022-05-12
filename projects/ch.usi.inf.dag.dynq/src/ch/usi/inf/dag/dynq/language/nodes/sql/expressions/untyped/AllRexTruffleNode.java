package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanRexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.ConditionProfile;


public final class AllRexTruffleNode extends BooleanRexTruffleNode {

    @Children
    private RexTruffleNode[] children;

    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private final ConditionProfile[] conditionProfiles;

    public AllRexTruffleNode(RexTruffleNode[] children) {
        this.children = children;
        this.conditionProfiles = new ConditionProfile[children.length];
        for (int i = 0; i < children.length; i++) {
            conditionProfiles[i] = ConditionProfile.createCountingProfile();
        }
    }

    @Override
    public Boolean executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return runBoolean(frame, input);
    }

    @Override
    @ExplodeLoop
    public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        for (int i = 0; i < children.length; i++) {
            RexTruffleNode expr = children[i];
            ConditionProfile profile = conditionProfiles[i];
            if(!profile.profile(expr.runBoolean(frame, input))) {
                return false;
            }
        }
        return true;
    }
    
}
