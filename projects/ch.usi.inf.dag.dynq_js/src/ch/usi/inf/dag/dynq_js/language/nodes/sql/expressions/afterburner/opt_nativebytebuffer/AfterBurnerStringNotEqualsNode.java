package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;


class AfterBurnerStringNotEqualsNode extends AfterBurnerStringBooleanExpressionNode {

    final String constant;

    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private final char[] chars;

    public AfterBurnerStringNotEqualsNode(AfterBurnerColumnarAccessorNodeState accessor, String constant) {
        super(accessor);
        this.constant = constant;
        this.chars = constant.toCharArray();
    }

    @Override
    @ExplodeLoop
    public boolean runBoolean(VirtualFrame frame, Object input) {
        int pointer = accessor.readInt((int) input);
        for (int i = 0; i < chars.length; i++) {
            int current = accessor.read(pointer + i);
            if(chars[i] != current) return true;
        }
        return accessor.read(pointer + chars.length) != 0;
    }

    @Override
    public String explain() {
        return "AfterBurnerStringNotEqualsNode(" + constant + ")";
    }
}
