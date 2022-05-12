package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;



public final class LeftIteratorConsumerFinalListMapSingleKeyNode extends LeftIteratorConsumerFinalListMapNode {

    @Child private RexTruffleNode buildKeyGetter;

    public LeftIteratorConsumerFinalListMapSingleKeyNode(RexTruffleNode buildKeyGetter) {
        this.buildKeyGetter = buildKeyGetter;
    }

    @Override
    public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
        insert(buildKeyGetter.executeWith(frame, row), row);
    }

}
