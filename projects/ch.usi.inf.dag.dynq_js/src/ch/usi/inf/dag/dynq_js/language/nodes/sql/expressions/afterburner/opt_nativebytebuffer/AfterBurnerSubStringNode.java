package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;


class AfterBurnerSubStringNode extends AfterBurnerStringExpressionNode {

    final int from, len;
    final char[] storage;

    public AfterBurnerSubStringNode(AfterBurnerColumnarAccessorNodeState accessor, int from, int len) {
        super(accessor);
        this.from = from;
        this.len = len;
        storage = new char[len];
    }


    @Override
    public String executeWith(VirtualFrame frame, Object input) {
        int pointer = accessor.readInt((int) input);
        if(CompilerDirectives.injectBranchProbability(CompilerDirectives.FASTPATH_PROBABILITY, longerThanFrom(pointer))) {
            return executeSubString(pointer + from);
        }
        return "";
    }

    @ExplodeLoop
    private boolean longerThanFrom(int pointer) {
        for (int i = 0; i < from; i++) {
            int current = accessor.read(pointer + i);
            if(CompilerDirectives.injectBranchProbability(CompilerDirectives.SLOWPATH_PROBABILITY, current == 0)) {
                return false;
            }
        }
        return true;
    }

    @ExplodeLoop
    private String executeSubString(int pointer) {
        for (int i = 0; i < len; i++) {
            int current = accessor.read(pointer + i);
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
