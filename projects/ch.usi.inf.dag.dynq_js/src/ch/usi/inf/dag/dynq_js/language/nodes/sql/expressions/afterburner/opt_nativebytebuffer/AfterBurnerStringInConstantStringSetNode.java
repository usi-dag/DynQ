package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.ConditionProfile;

import java.util.Set;


class AfterBurnerStringInConstantStringSetNode extends AfterBurnerStringBooleanExpressionNode {

    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private final ConditionProfile[] profiles;

    @CompilerDirectives.CompilationFinal(dimensions = 2)
    private final char[][] chars;

    private final Set<String> constants;

    public AfterBurnerStringInConstantStringSetNode(AfterBurnerColumnarAccessorNodeState accessor,
                                                    Set<String> constants) {
        super(accessor);
        this.constants = constants;
        String[] elements = constants.stream().sorted().toArray(String[]::new);
        profiles = new ConditionProfile[elements.length];
        chars = new char[elements.length][];
        for (int i = 0; i < elements.length; i++) {
            profiles[i] = ConditionProfile.createCountingProfile();
            chars[i] = elements[i].toCharArray();
        }
    }

    @Override
    @ExplodeLoop
    public boolean runBoolean(VirtualFrame frame, Object input) {
        int pointer = accessor.readInt((int) input);
        for (int i = 0; i < chars.length; i++) {
            if(profiles[i].profile(valueEqIthConstant(i, pointer)))
                return true;
        }
        return false;
    }

    @ExplodeLoop
    private boolean valueEqIthConstant(int i, int pointer) {
        for (int j = 0; j < chars[i].length; j++) {
            int current = accessor.read(pointer + j);
            if(chars[i][j] != current) return false;
        }
        return true;
    }

    @Override
    public String explain() {
        return "AfterBurnerStringInConstantStringSetNode(" + constants + ")";
    }

}
