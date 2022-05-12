package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.js.runtime.array.TypedArray;

import java.util.Set;


class AfterBurnerStringInConstantStringSetNode extends AfterBurnerStringBooleanExpressionNode {

    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private final ConditionProfile[] profiles;

    @CompilerDirectives.CompilationFinal(dimensions = 2)
    private final char[][] chars;

    private final Set<String> constants;

    public AfterBurnerStringInConstantStringSetNode(TypedArray.TypedIntArray int32Array,
                                                    TypedArray.TypedIntArray int8Array,
                                                    DynamicObject buffer,
                                                    int offset,
                                                    Set<String> constants) {
        super(int32Array, int8Array, buffer, offset);
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
        int pointer = getPointer((int) input);
        for (int i = 0; i < chars.length; i++) {
            if(profiles[i].profile(valueEqIthConstant(i, pointer)))
                return true;
        }
        return false;
    }

    @ExplodeLoop
    private boolean valueEqIthConstant(int i, int pointer) {
        for (int j = 0; j < chars[i].length; j++) {
            int current = int8Array.getInt(buffer, pointer + j, null);
            if(chars[i][j] != current) return false;
        }
        return true;
    }

    @Override
    public String explain() {
        return "AfterBurnerStringInConstantStringSetNode(" + constants + ")";
    }

}
