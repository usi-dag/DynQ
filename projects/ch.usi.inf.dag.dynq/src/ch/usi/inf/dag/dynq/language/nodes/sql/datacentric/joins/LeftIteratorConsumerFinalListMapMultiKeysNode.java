package ch.usi.inf.dag.dynq.language.nodes.sql.datacentric.joins;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ArrayRexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing.ArrayBox;
import ch.usi.inf.dag.dynq.language.nodes.sql.operators.hashing.ArrayBoxHashNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class LeftIteratorConsumerFinalListMapMultiKeysNode extends LeftIteratorConsumerFinalListMapNode {

    @Child private ArrayRexTruffleNode buildKeyGetter;
    @Child private ArrayBoxHashNode boxHashNode;

    public LeftIteratorConsumerFinalListMapMultiKeysNode(ArrayRexTruffleNode buildKeyGetter) {
        this.buildKeyGetter = buildKeyGetter;
        this.boxHashNode = new ArrayBoxHashNode(buildKeyGetter.size());
    }

    @Override
    public void execute(VirtualFrame frame, Object row) throws InteropException, FrameSlotTypeException {
        Object[] data = buildKeyGetter.executeWith(frame, row);
        ArrayBox key = new ArrayBox(data, boxHashNode.hash(data));
        insert(key, row);
    }
    
}
