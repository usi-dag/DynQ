package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.array.TypedArray;


class AfterBurnerStringEqualsNode extends AfterBurnerStringBooleanExpressionNode {

    final String constant;

    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private final char[] chars;

    public AfterBurnerStringEqualsNode(TypedArray.TypedIntArray int32Array,
                                       TypedArray.TypedIntArray int8Array,
                                       DynamicObject buffer,
                                       int offset,
                                       String constant) {
        super(int32Array, int8Array, buffer, offset);
        this.constant = constant;
        this.chars = constant.toCharArray();
    }

    @Override
    @ExplodeLoop
    public boolean runBoolean(VirtualFrame frame, Object input) {
        int pointer = getPointer((int) input);
        for (int i = 0; i < chars.length; i++) {
            int current = int8Array.getInt(buffer, pointer + i, null);
            if(chars[i] != current) return false;
        }
        return int8Array.getInt(buffer, pointer + chars.length, null) == 0;
    }

    @Override
    public String explain() {
        return "AfterBurnerStringEqualsNode(" + constant + ")";
    }
}
