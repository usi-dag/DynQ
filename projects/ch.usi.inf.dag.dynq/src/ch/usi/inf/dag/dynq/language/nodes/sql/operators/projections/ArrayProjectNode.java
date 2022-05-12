package ch.usi.inf.dag.dynq.language.nodes.sql.operators.projections;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.ArrayRexTruffleNode;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;


public final class ArrayProjectNode extends ProjectNode {

    @Child
    ArrayRexTruffleNode arrayRexNode;

    public ArrayProjectNode(ArrayRexTruffleNode arrayRexNode) {
        this.arrayRexNode = arrayRexNode;
    }

    public static ArrayProjectNode create(ArrayRexTruffleNode arrayRexNode) {
        return new ArrayProjectNode(arrayRexNode);
    }


    @Override
    public Object[] execute(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return arrayRexNode.executeWith(frame, input);
    }

    @Override
    public Class<?> getOutputRowJavaType() {
        return Object[].class;
    }

    @Override
    public String explain() {
        return super.explain() + "(" + arrayRexNode.size() + ")";
    }

}
