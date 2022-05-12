package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import java.util.regex.Pattern;


public abstract class StringLikeRexTruffleNode extends BooleanRexTruffleNode {

    @Child
    RexTruffleNode stringGetter;

    private StringLikeRexTruffleNode(RexTruffleNode stringGetter) {
        this.stringGetter = stringGetter;
    }

    @Override
    public String runString(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return stringGetter.runString(frame, input);
    }

    @Override
    public Boolean executeWith(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
        return runBoolean(frame, input);
    }

    public abstract boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException;

    public static StringLikeRexTruffleNode create(RexTruffleNode stringGetter, String reLike) {
        String regex = quotemeta(reLike);
        int starCount = (int) regex.chars().filter(ch -> ch == '%').count();
        if(!regex.contains("_") && starCount > 0) {
            // startsWith
            if(starCount == 1 && regex.endsWith("%")) {
                return new StringStartsWithRexTruffleNode(stringGetter, regex.substring(0, regex.length() - 1));
            }
            // endsWith
            if(starCount == 1 && regex.startsWith("%")) {
                return new StringEndsWithRexTruffleNode(stringGetter, regex.substring(1));
            }
            // contains
            if(starCount == 2 && regex.startsWith("%") && regex.endsWith("%")) {
                return new StringContainsRexTruffleNode(stringGetter, regex.substring(1, regex.length() - 1));
            }
            if(regex.startsWith("%") && regex.endsWith("%")) {
                String[] patterns = regex.substring(1, regex.length() - 1).split("%");
                return new StringMultiContainsRexTruffleNode(stringGetter, patterns);
            }
        }

        // TODO use Tregex

        Pattern pattern = like(regex);
        return new StringLikeRexTruffleNode(stringGetter) {

            @Override
            public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
                return match(runString(frame, input));
            }

            @CompilerDirectives.TruffleBoundary
            private boolean match(String s) {
                return pattern.matcher(s).matches();
            }
        };
    }

    static final class StringStartsWithRexTruffleNode extends StringLikeRexTruffleNode {

        @CompilerDirectives.CompilationFinal
        private final String prefix;

        StringStartsWithRexTruffleNode(RexTruffleNode stringGetter, String prefix) {
            super(stringGetter);
            this.prefix = prefix;
        }

        @Override
        @ExplodeLoop
        public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
            return runString(frame, input).startsWith(prefix);
        }
    }


    static final class StringEndsWithRexTruffleNode extends StringLikeRexTruffleNode {

        @CompilerDirectives.CompilationFinal
        private final String suffix;

        StringEndsWithRexTruffleNode(RexTruffleNode stringGetter, String suffix) {
            super(stringGetter);
            this.suffix = suffix;
        }

        @Override
        @ExplodeLoop
        public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
            return runString(frame, input).endsWith(suffix);
        }
    }


    static final class StringContainsRexTruffleNode extends StringLikeRexTruffleNode {

        @CompilerDirectives.CompilationFinal
        private final String midlde;

        StringContainsRexTruffleNode(RexTruffleNode stringGetter, String midlde) {
            super(stringGetter);
            this.midlde = midlde;
        }

        @Override
        public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
            return runString(frame, input).contains(midlde);
        }
    }


    static final class StringMultiContainsRexTruffleNode extends StringLikeRexTruffleNode {

        @CompilerDirectives.CompilationFinal(dimensions = 1)
        private final String[] strings;

        @CompilerDirectives.CompilationFinal
        private final int totalLen;

        StringMultiContainsRexTruffleNode(RexTruffleNode stringGetter, String[] strings) {
            super(stringGetter);
            this.strings = strings;
            int len = 0;
            for (String s : strings) len += s.length();
            totalLen = len;
        }

        @Override
        public boolean runBoolean(VirtualFrame frame, Object input) throws InteropException, FrameSlotTypeException {
            String s = runString(frame, input);
            return s.length() >= totalLen && runBooleanWithString(s);
        }

        @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_EXPLODE)
        public boolean runBooleanWithString(String string) {
            int start = 0;
            for(String sub : strings) {
                int find = string.indexOf(sub, start);
                if(find == -1) {
                    return false;
                }
                start = find + sub.length();
            }
            return true;
        }
    }


    public static Pattern like(String regex){
        regex = regex.replace("_", ".").replace("%", ".*?");
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }

    public static String quotemeta(String s){
        if (s == null) {
            throw new IllegalArgumentException("String cannot be null");
        }

        int len = s.length();
        if (len == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if ("[](){}.*+?$^|#\\".indexOf(c) != -1) {
                sb.append("\\");
            }
            sb.append(c);
        }
        return sb.toString();
    }

}
