package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.VirtualFrame;


public final class ConstRexTruffleNode extends RexTruffleNode {

    public static ConstRexTruffleNode create(Object constant) {
        return new ConstRexTruffleNode(constant);
    }

    private final Object constant;

    private ConstRexTruffleNode(Object constant) {
        this.constant = constant;
    }


    @Override
    public Object executeWith(VirtualFrame frame, Object input) {
        return constant;
    }

}
