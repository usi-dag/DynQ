package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;


@ExportLibrary(InteropLibrary.class)
public class AfterBurnerStringWrapperNativeByteBufferUnsafe implements Comparable<Object>, TruffleObject {

    // TODO thread-locals once implemented parallelization
    static private int size = 64;
    static private char[] workingMemory = new char[size];

    private final int position;
    private final AfterBurnerColumnarAccessorNodeState accessor;

    private String cachedString;
    private int cachedHash = -1;

    public AfterBurnerStringWrapperNativeByteBufferUnsafe(AfterBurnerColumnarAccessorNodeState accessor, int position) {
        this.position = position;
        this.accessor = accessor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AfterBurnerStringWrapperNativeByteBufferUnsafe) {
            return equals((AfterBurnerStringWrapperNativeByteBufferUnsafe) obj);
        }
        if (obj instanceof String) {
            return equals((String) obj);
        }
        return false;
    }

    public boolean equals(AfterBurnerStringWrapperNativeByteBufferUnsafe other) {
        if(position == other.position) {
            return true;
        }
        int current, otherCurrent;
        int bytePtr = 0;
        while(true) {
            current = accessor.read(position + bytePtr);
            otherCurrent = other.accessor.read(other.position + bytePtr);
            if(current != otherCurrent) {
                return false;
            }
            if(current == 0) {
                return true;
            }
            bytePtr++;
        }
    }

    public boolean equals(String other) {
        int length = other.length();
        for (int i = 0; i < length; i++) {
            int current = accessor.read(position + i);
            if(other.charAt(i) != current) {
                return false;
            }
        }
        return accessor.read(position + length) == 0;
    }


    @Override
    public int hashCode() {
        if(cachedHash == -1) {
            int h = 0, i = 0;
            int current;
            do {
                current = accessor.read(position + i);
                i++;
                h = 31 * h + current;
            }
            while(current != 0);
            cachedHash = h;
        }
        return cachedHash;
    }


    @Override
    public int compareTo(Object obj) {
        if (obj instanceof AfterBurnerStringWrapperNativeByteBufferUnsafe) {
            return compareTo((AfterBurnerStringWrapperNativeByteBufferUnsafe) obj);
        }
        if (obj instanceof String) {
            return compareTo((String) obj);
        }
        throw new ClassCastException();
    }

    public int compareTo(AfterBurnerStringWrapperNativeByteBufferUnsafe other) {
        if(position == other.position) {
            return 0;
        }
        int bytePtr = 0;
        while(true) {
            int current = accessor.read(position + bytePtr);
            int otherCurrent = other.accessor.read(other.position + bytePtr);
            if(current != otherCurrent) {
                return current - otherCurrent;
            }
            if(current == 0) {
                return 0;
            }
            bytePtr++;
        }
    }

    public int compareTo(String other) {
        int length = other.length();
        for (int i = 0; i < length; i++) {
            int current = accessor.read(position + i);
            if(current != other.charAt(i)) {
                return current - other.charAt(i);
            }
        }
        if(accessor.read(position + length) == 0) {
            return 0;
        }
        return 1;
    }

    @Override
    public String toString() {
        if (cachedString != null) {
            return cachedString;
        }
        return cachedString = convert();
    }

    private String convert() {
        char current;
        int bytePtr = 0;
        while((current = (char) accessor.read(position + bytePtr)) != 0) {
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
        return new String(workingMemory, 0, bytePtr);
    }

    @ExportMessage
    public boolean isString() {
        return true;
    }

    @ExportMessage
    public String asString() {
        return toString();
    }

}