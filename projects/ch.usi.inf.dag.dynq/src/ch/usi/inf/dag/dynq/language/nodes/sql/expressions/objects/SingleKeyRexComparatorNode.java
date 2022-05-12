package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public final class SingleKeyRexComparatorNode extends RexComparatorNode {

    @Child
    RexTruffleNode keyGetter;

    @Child
    SingleValueComparatorNode comparator;

    public SingleKeyRexComparatorNode(RexTruffleNode keyGetter, boolean reverse) {
        this.keyGetter = keyGetter;
        this.comparator = SingleValueComparatorNodeGen.create(reverse);
    }

    @Override
    @ExplodeLoop
    public Integer compare(VirtualFrame frame, Object fst, Object snd) throws InteropException, FrameSlotTypeException {
        Object fstVal = keyGetter.executeWith(frame, fst);
        Object sndVal = keyGetter.executeWith(frame, snd);
        return comparator.execute(fstVal, sndVal);
    }
}
