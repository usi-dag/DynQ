package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.ConditionProfile;

public final class MultiKeysRexComparatorNode extends RexComparatorNode {

    @Children
    RexTruffleNode[] keyGetters;

    @Children
    SingleValueComparatorNode[] comparators;

    @CompilerDirectives.CompilationFinal(dimensions = 1)
    final ConditionProfile[] profilersNotZero;


    public MultiKeysRexComparatorNode(RexTruffleNode[] keyGetters, boolean[] reverse) {
        this.keyGetters = keyGetters;
        this.comparators = new SingleValueComparatorNode[keyGetters.length];
        this.profilersNotZero = new ConditionProfile[keyGetters.length];
        for (int i = 0; i < keyGetters.length; i++) {
            comparators[i] = SingleValueComparatorNodeGen.create(reverse[i]);
            profilersNotZero[i] = ConditionProfile.createCountingProfile();
        }
    }

    @Override
    @ExplodeLoop
    public Integer compare(VirtualFrame frame, Object fst, Object snd) throws InteropException, FrameSlotTypeException {
        for (int i = 0; i < keyGetters.length; i++) {
            Object fstVal = keyGetters[i].executeWith(frame, fst);
            Object sndVal = keyGetters[i].executeWith(frame, snd);
            int comparison = comparators[i].execute(fstVal, sndVal);
            if(profilersNotZero[i].profile(comparison != 0)) {
                return comparison;
            }
        }
        return 0;
    }
}
