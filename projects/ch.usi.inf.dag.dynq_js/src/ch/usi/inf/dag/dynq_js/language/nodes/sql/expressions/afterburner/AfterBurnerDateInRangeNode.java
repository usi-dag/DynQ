package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanRexTruffleNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.array.TypedArray;


class AfterBurnerDateInRangeNode extends BooleanRexTruffleNode {

    final int offset;
    final long from;
    final long to;
    final TypedArray.TypedIntArray int32Array;
    final DynamicObject buffer;

    public AfterBurnerDateInRangeNode(TypedArray.TypedIntArray int32Array,
                                      DynamicObject buffer,
                                      int offset,
                                      long from,
                                      long to) {
        this.int32Array = int32Array;
        this.buffer = buffer;
        this.offset = offset;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean runBoolean(VirtualFrame frame, Object input) {
        int index = (int) input;
        int date = int32Array.getInt(buffer, (offset + (index << 2)) >> 2, null);
        return date >= from && date < to;
    }

    @Override
    public String explain() {
        return "AfterBurnerDateInRangeNode(" + from + ", " + to + ")";
    }
}
