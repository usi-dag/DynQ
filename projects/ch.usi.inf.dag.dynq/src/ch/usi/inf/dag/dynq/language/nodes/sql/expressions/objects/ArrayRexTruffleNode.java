package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.ExplodeLoop;


public final class ArrayRexTruffleNode extends RexTruffleNode {

    public static ArrayRexTruffleNode create(RexTruffleNode[] children) {
        return new ArrayRexTruffleNode(children);
    }


    @Children
    private RexTruffleNode[] children;

    private ArrayRexTruffleNode(RexTruffleNode[] children) {
        this.children = children;
    }

    public int size() {
        return children.length;
    }

    @Override
    @ExplodeLoop
    public Object[] executeWith(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
        Object[] result = new Object[children.length];
        for (int i = 0; i < children.length; i++) {
            result[i] = children[i].executeWith(frame, row);
        }
        return result;
    }

    public RexTruffleNode[] getRexChildren() {
        return children;
    }
}
