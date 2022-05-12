package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;


abstract class AfterBurnerStringLikeNode extends AfterBurnerStringBooleanExpressionNode {

    public AfterBurnerStringLikeNode(AfterBurnerColumnarAccessorNodeState accessor) {
        super(accessor);
    }

    static class AfterBurnerStringStartsWithNode extends AfterBurnerStringLikeNode {
        @CompilerDirectives.CompilationFinal(dimensions = 1)
        private final byte[] chars;

        public AfterBurnerStringStartsWithNode(AfterBurnerColumnarAccessorNodeState accessor,
                                               String prefix) {
            super(accessor);
            chars = prefix.getBytes();
        }

        @Override
        @ExplodeLoop
        public boolean runBoolean(VirtualFrame frame, Object input) {
            int pointer = accessor.readInt((int) input);
            for (int i = 0; i < chars.length; i++) {
                int current = accessor.read(pointer + i);
                if(chars[i] != current) return false;
            }
            return true;
        }
    }

    static class AfterBurnerStringEndsWithNode extends AfterBurnerStringLikeNode {
        @CompilerDirectives.CompilationFinal(dimensions = 1)
        private final byte[] chars;

        public AfterBurnerStringEndsWithNode(AfterBurnerColumnarAccessorNodeState accessor, String string) {
            super(accessor);
            this.chars = new StringBuffer(string).reverse().toString().getBytes();
        }

        @Override
        @ExplodeLoop
        public boolean runBoolean(VirtualFrame frame, Object input) {
            int startPointer = accessor.readInt((int) input);
            int endPointer = scanUntilEnd(startPointer);
            return endsWith(startPointer, endPointer);
        }

        private int scanUntilEnd(int pointer) {
            while(accessor.read(pointer) != 0)
                pointer++;
            return pointer - 1;
        }

        @ExplodeLoop
        private boolean endsWith(int startPointer, int endPointer) {
            if(CompilerDirectives.injectBranchProbability(CompilerDirectives.SLOWPATH_PROBABILITY,
                    endPointer - startPointer + 1 < chars.length)) {
                return false;
            }
            for (int i = 0; i < chars.length; i++) {
                int current = accessor.read(endPointer - i);
                if(chars[i] != current) {
                    return false;
                }
            }
            return true;
        }
    }

    static class AfterBurnerStringContainsNode extends AfterBurnerStringLikeNode {
        @CompilerDirectives.CompilationFinal(dimensions = 1)
        private final byte[] chars;

        public AfterBurnerStringContainsNode(AfterBurnerColumnarAccessorNodeState accessor, String str) {
            super(accessor);
            this.chars = str.getBytes();
        }

        @Override
        public boolean runBoolean(VirtualFrame frame, Object input) {
            int pointer = accessor.readInt((int) input);
            int current;
            int i = 0;
            while ((current = accessor.read(pointer + i)) != 0) {
                if(current == chars[0] && compareFrom(pointer + i))
                    return true;
                i++;
            }
            return false;
        }

        @ExplodeLoop
        boolean compareFrom(int pointer) {
            for (int i = 1; i < chars.length; i++) {
                int current = accessor.read(pointer + i);
                if(chars[i] != current) return false;
            }
            return true;
        }

    }

    static class AfterBurnerStringMultiContainsNode extends AfterBurnerStringLikeNode {
        @CompilerDirectives.CompilationFinal(dimensions = 1)
        final String[] strings;

        private int size = 64;
        private char[] workingMemory = new char[size];

        public AfterBurnerStringMultiContainsNode(AfterBurnerColumnarAccessorNodeState accessor, String[] strings) {
            super(accessor);
            this.strings = strings;
        }

        @Override
        public boolean runBoolean(VirtualFrame frame, Object input) {
            int pointer = accessor.readInt((int) input);
            char current;
            int bytePtr = 0;
            while((current = (char) accessor.read(pointer + bytePtr)) != 0) {
                workingMemory[bytePtr] = current;
                bytePtr++;
                if(CompilerDirectives.injectBranchProbability(CompilerDirectives.SLOWPATH_PROBABILITY, bytePtr == size)) {
                    int newSize = (int)(size*1.5);
                    char[] newWorkingMemory = new char[newSize];
                    System.arraycopy(workingMemory, 0, newWorkingMemory, 0, size);
                    size = newSize;
                    workingMemory = newWorkingMemory;
                }
            }
            return runBooleanWithString(new String(workingMemory, 0, bytePtr));
        }

        @ExplodeLoop
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

}
