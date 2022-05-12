package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects.StringRexTruffleNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.array.TypedArray;


abstract class AfterBurnerStringExpressionNode extends StringRexTruffleNode {

    final TypedArray.TypedIntArray int32Array;
    final TypedArray.TypedIntArray int8Array;

    final DynamicObject buffer;
    final int offset;

    AfterBurnerStringExpressionNode(TypedArray.TypedIntArray int32Array,
                                    TypedArray.TypedIntArray int8Array,
                                    DynamicObject buffer,
                                    int offset) {
        this.int32Array = int32Array;
        this.int8Array = int8Array;
        this.buffer = buffer;
        this.offset = offset;
    }

    int getPointer(int index) {
        return int32Array.getInt(buffer, (offset + (index << 2)) >> 2, null);
    }
}
