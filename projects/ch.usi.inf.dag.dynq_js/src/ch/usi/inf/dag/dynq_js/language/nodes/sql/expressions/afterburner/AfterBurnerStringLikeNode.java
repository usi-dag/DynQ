package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.array.TypedArray;


abstract class AfterBurnerStringLikeNode extends AfterBurnerStringBooleanExpressionNode {

    public AfterBurnerStringLikeNode(TypedArray.TypedIntArray int32Array,
                                     TypedArray.TypedIntArray int8Array,
                                     DynamicObject buffer,
                                     int offset) {
        super(int32Array, int8Array, buffer, offset);
    }

    static class AfterBurnerStringStartsWithNode extends AfterBurnerStringLikeNode {
        @CompilerDirectives.CompilationFinal(dimensions = 1)
        private final byte[] chars;

        public AfterBurnerStringStartsWithNode(TypedArray.TypedIntArray int32Array,
                                               TypedArray.TypedIntArray int8Array,
                                               DynamicObject buffer,
                                               int offset,
                                               String prefix) {
            super(int32Array, int8Array, buffer, offset);
            chars = prefix.getBytes();
        }

        @Override
        @ExplodeLoop
        public boolean runBoolean(VirtualFrame frame, Object input) {
            int pointer = getPointer((int) input);
            for (int i = 0; i < chars.length; i++) {
                int current = int8Array.getInt(buffer, pointer + i, null);
                if(chars[i] != current) return false;
            }
            return true;
        }
    }

    static class AfterBurnerStringEndsWithNode extends AfterBurnerStringLikeNode {
        @CompilerDirectives.CompilationFinal(dimensions = 1)
        private final byte[] chars;

        public AfterBurnerStringEndsWithNode(TypedArray.TypedIntArray int32Array,
                                             TypedArray.TypedIntArray int8Array,
                                             DynamicObject buffer,
                                             int offset,
                                             String string) {
            super(int32Array, int8Array, buffer, offset);
            this.chars = new StringBuffer(string).reverse().toString().getBytes();
        }

        @Override
        @ExplodeLoop
        public boolean runBoolean(VirtualFrame frame, Object input) {
            int startPointer = getPointer((int) input);
            int endPointer = scanUntilEnd(startPointer);
            return endsWith(startPointer, endPointer);
        }

        private int scanUntilEnd(int pointer) {
            while(int8Array.getInt(buffer, pointer, null) != 0)
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
                int current = int8Array.getInt(buffer, endPointer - i, null);
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

        public AfterBurnerStringContainsNode(TypedArray.TypedIntArray int32Array,
                                             TypedArray.TypedIntArray int8Array,
                                             DynamicObject buffer,
                                             int offset,
                                             String str) {
            super(int32Array, int8Array, buffer, offset);
            this.chars = str.getBytes();
        }

        @Override
        public boolean runBoolean(VirtualFrame frame, Object input) {
            int pointer = getPointer((int) input);
            int current;
            int i = 0;
            while ((current = int8Array.getInt(buffer, pointer + i, null)) != 0) {
                if(current == chars[0] && compareFrom(pointer + i))
                    return true;
                i++;
            }
            return false;
        }

        @ExplodeLoop
        boolean compareFrom(int pointer) {
            for (int i = 1; i < chars.length; i++) {
                int current = int8Array.getInt(buffer, pointer + i, null);
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

        public AfterBurnerStringMultiContainsNode(TypedArray.TypedIntArray int32Array,
                                                  TypedArray.TypedIntArray int8Array,
                                                  DynamicObject buffer,
                                                  int offset,
                                                  String[] strings) {
            super(int32Array, int8Array, buffer, offset);
            this.strings = strings;
        }

        @Override
        public boolean runBoolean(VirtualFrame frame, Object input) {
            int pointer = getPointer((int) input);
            char current;
            int bytePtr = 0;
            while((current = (char) int8Array.getInt(buffer, pointer + bytePtr, null)) != 0) {
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
