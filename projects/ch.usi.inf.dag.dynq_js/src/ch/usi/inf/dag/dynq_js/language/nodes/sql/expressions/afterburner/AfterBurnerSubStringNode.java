package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.array.TypedArray;


class AfterBurnerSubStringNode extends AfterBurnerStringExpressionNode {

    final int from, len;
    final char[] storage;

    public AfterBurnerSubStringNode(TypedArray.TypedIntArray int32Array,
                                    TypedArray.TypedIntArray int8Array,
                                    DynamicObject buffer,
                                    int offset,
                                    int from, int len) {
        super(int32Array, int8Array, buffer, offset);
        this.from = from;
        this.len = len;
        storage = new char[len];
    }


    @Override
    public String executeWith(VirtualFrame frame, Object input) {
        int pointer = getPointer((int) input);
        if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, longerThanFrom(pointer))) {
            return executeSubString(pointer + from);
        }
        return "";
    }

    @ExplodeLoop
    private boolean longerThanFrom(int pointer) {
        for (int i = 0; i < from; i++) {
            int current = int8Array.getInt(buffer, pointer + i, null);
            if(CompilerDirectives.injectBranchProbability(CompilerDirectives.SLOWPATH_PROBABILITY, current == 0)) {
                return false;
            }
        }
        return true;
    }

    @ExplodeLoop
    private String executeSubString(int pointer) {
        for (int i = 0; i < len; i++) {
            int current = int8Array.getInt(buffer, pointer + i, null);
            if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, current != 0)) {
                storage[i] = (char) current;
            } else {
                return new String(storage, 0, i);
            }
        }
        return new String(storage);
    }

    @Override
    public String explain() {
        return "AfterBurnerSubStringNode(" + from + ", " + len + ")";
    }

}
