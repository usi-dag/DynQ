package ch.usi.inf.dag.dynq.language.nodes.sql.operators.projections;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.object.DynamicObject;


public final class DynamicObjectProjectNode extends ProjectNode {

    @Child
    RexTruffleNode rexTruffleNode;

    public DynamicObjectProjectNode(RexTruffleNode arrayRexNode) {
        this.rexTruffleNode = arrayRexNode;
    }

    @Override
    public Object execute(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return rexTruffleNode.executeWith(frame, input);
    }

    @Override
    public Class<?> getOutputRowJavaType() {
        return DynamicObject.class;
    }

}
