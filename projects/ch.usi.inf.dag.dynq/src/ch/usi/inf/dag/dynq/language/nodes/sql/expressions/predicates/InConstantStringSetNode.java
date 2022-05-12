package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans.BooleanRexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import java.util.HashSet;
import java.util.Set;


public abstract class InConstantStringSetNode extends BooleanRexTruffleNode {

    public static final int MAX_UNROLL;
    static {
        int max;
        try {
            max = Integer.parseInt(System.getenv("DYNQ_InConstantStringSetNode_MAX_UNROLL"));
        } catch (Exception ignored) {
            max = 16;
        }
        MAX_UNROLL = max;
    }
    @Child RexTruffleNode valueGetter;

    public static InConstantStringSetNode create(RexTruffleNode valueGetter, Set<String> elements) {
        if(elements.size() > MAX_UNROLL) {
            return new InConstantStringHashSetBinSearchNode(valueGetter, elements);
        }
        return new InConstantStringSetArrayUnrolledNode(valueGetter, elements);
    }

    private InConstantStringSetNode(RexTruffleNode valueGetter) {
        this.valueGetter = valueGetter;
    }

    @Override
    public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return valueInElements(valueGetter.runString(frame, input));
    }

    abstract boolean valueInElements(String value);

    private static class InConstantStringSetArrayUnrolledNode extends InConstantStringSetNode {
        @CompilerDirectives.CompilationFinal(dimensions = 2)
        private final char[][] chars;

        public InConstantStringSetArrayUnrolledNode(RexTruffleNode valueGetter, Set<String> constants) {
            super(valueGetter);
            String[] elements = constants.stream().sorted().toArray(String[]::new);
            chars = new char[elements.length][];
            for (int i = 0; i < elements.length; i++) {
                chars[i] = elements[i].toCharArray();
            }
        }

        @Override
        @ExplodeLoop
        boolean valueInElements(String value) {
            for (int i = 0; i < chars.length; i++) {
                boolean found = true;
                if(chars[i].length != value.length()) continue;
                for (int j = 0; j < chars[i].length; j++) {
                    if(chars[i][j] != value.charAt(j)) {
                        found = false;
                        break;
                    }
                }
                if(found) return true;
            }
            return false;
        }

    }

    private static class InConstantStringHashSetBinSearchNode extends InConstantStringSetNode {
        private final HashSet<String> elementsSet;

        public InConstantStringHashSetBinSearchNode(RexTruffleNode valueGetter, Set<String> elements) {
            super(valueGetter);
            this.elementsSet = new HashSet<>(elements);
        }

        @Override
        boolean valueInElements(String value) {
            return elementsSet.contains(value);
        }
    }
}
