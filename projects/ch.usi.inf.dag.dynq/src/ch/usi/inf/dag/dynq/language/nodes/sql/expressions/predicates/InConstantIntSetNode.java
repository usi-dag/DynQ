package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanRexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.ConditionProfile;

import java.util.Arrays;
import java.util.Set;


public abstract class InConstantIntSetNode extends BooleanRexTruffleNode {

    @Child RexTruffleNode valueGetter;

    @CompilerDirectives.CompilationFinal(dimensions = 1)
    final int[] elements;

    public static InConstantIntSetNode create(RexTruffleNode valueGetter, Set<Integer> elements) {
        if(elements.size() > 16) {
            return new InConstantIntSetArrayBinSearchNode(valueGetter, elements);
        } else {
            return new InConstantIntSetArrayUnrolledNode(valueGetter, elements);
        }
    }

    public InConstantIntSetNode(RexTruffleNode valueGetter, Set<Integer> elements) {
        this.valueGetter = valueGetter;
        this.elements = elements.stream().mapToInt(x -> x).sorted().toArray();
    }

    @Override
    public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        try {
            int value = valueGetter.runInt(frame, input);
            return valueInElements(value);
        } catch (ClassCastException e) {
            return false; // TODO check: perf are really bad if we reach this
        }
    }

    abstract boolean valueInElements(int value);

    private static class InConstantIntSetArrayUnrolledNode extends InConstantIntSetNode {
        @CompilerDirectives.CompilationFinal(dimensions = 1)
        final ConditionProfile[] profiles;

        public InConstantIntSetArrayUnrolledNode(RexTruffleNode valueGetter, Set<Integer> elements) {
            super(valueGetter, elements);
            profiles = new ConditionProfile[elements.size()];
            for (int i = 0; i < elements.size(); i++) {
                profiles[i] = ConditionProfile.createCountingProfile();
            }
        }

        @Override
        @ExplodeLoop
        boolean valueInElements(int value) {
            for (int i = 0; i < elements.length; i++) {
                if(profiles[i].profile(value == elements[i])) return true;
            }
            return false;
        }
    }

    private static class InConstantIntSetArrayBinSearchNode extends InConstantIntSetNode {
        public InConstantIntSetArrayBinSearchNode(RexTruffleNode valueGetter, Set<Integer> elements) {
            super(valueGetter, elements);
        }

        boolean valueInElements(int value) {
            return Arrays.binarySearch(elements, value) >= 0;
        }
    }
}
