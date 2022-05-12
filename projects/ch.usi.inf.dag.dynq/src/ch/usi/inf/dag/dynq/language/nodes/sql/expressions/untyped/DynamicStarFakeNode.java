package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.VirtualFrame;


public class DynamicStarFakeNode extends RexTruffleNode {
    @Override
    public Object executeWith(VirtualFrame frame, Object input) {
        return input;
    }
}
