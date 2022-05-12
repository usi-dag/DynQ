package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;

import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.Node;


public abstract class RexComparatorNode extends Node implements Explainable {
    public abstract Integer compare(VirtualFrame frame, Object fst, Object snd) throws InteropException, FrameSlotTypeException;

    public RexComparatorNode reversed() {
        return new RexComparatorNode() {
            @Override
            public Integer compare(VirtualFrame frame, Object fst, Object snd) throws InteropException, FrameSlotTypeException {
                return -RexComparatorNode.this.compare(frame, fst, snd);
            }
        };
    }
}
