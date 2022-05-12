package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanRexTruffleNode;
import com.oracle.truffle.api.frame.VirtualFrame;


final class AfterBurnerDateInRangeNode extends BooleanRexTruffleNode {

    final AfterBurnerColumnarAccessorNodeState accessor;
    final long from;
    final long to;

    public AfterBurnerDateInRangeNode(AfterBurnerColumnarAccessorNodeState accessor, long from, long to) {
        this.accessor = accessor;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean runBoolean(VirtualFrame frame, Object input) {
        int index = (int) input;
        int date = accessor.readInt(index);
        return date >= from && date < to;
    }

    @Override
    public String explain() {
        return "AfterBurnerDateInRangeNode(" + from + ", " + to + ")";
    }
}
