package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import java.nio.ByteBuffer;


@ExportLibrary(InteropLibrary.class)
public class AfterBurnerStringWrapperNativeByteBuffer implements Comparable<Object>, TruffleObject {

    // TODO thread-locals once implemented parallelization
    static private int size = 64;
    static private char[] workingMemory = new char[size];

    private final int position;
    private final ByteBuffer buffer;

    private String cachedString;
    private int cachedHash = -1;

    public AfterBurnerStringWrapperNativeByteBuffer(ByteBuffer buffer, int position) {
        this.position = position;
        this.buffer = buffer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AfterBurnerStringWrapperNativeByteBuffer) {
            return equals((AfterBurnerStringWrapperNativeByteBuffer) obj);
        }
        if (obj instanceof String) {
            return equals((String) obj);
        }
        return false;
    }

    public boolean equals(AfterBurnerStringWrapperNativeByteBuffer other) {
        if(position == other.position) {
            return true;
        }
        int current, otherCurrent;
        int bytePtr = 0;
        while(true) {
            current = buffer.get(position + bytePtr);
            otherCurrent = other.buffer.get(other.position + bytePtr);
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
            int current = buffer.get(position + i);
            if(other.charAt(i) != current) {
                return false;
            }
        }
        return buffer.get(position + length) == 0;
    }


    @Override
    public int hashCode() {
        if(cachedHash == -1) {
            int h = 0, i = 0;
            int current;
            do {
                current = buffer.get(position + i);
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
        if (obj instanceof AfterBurnerStringWrapperNativeByteBuffer) {
            return compareTo((AfterBurnerStringWrapperNativeByteBuffer) obj);
        }
        if (obj instanceof String) {
            return compareTo((String) obj);
        }
        throw new ClassCastException();
    }

    public int compareTo(AfterBurnerStringWrapperNativeByteBuffer other) {
        if(position == other.position) {
            return 0;
        }
        int bytePtr = 0;
        while(true) {
            int current = buffer.get(position + bytePtr);
            int otherCurrent = other.buffer.get(other.position + bytePtr);
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
            int current = buffer.get(position + i);
            if(current != other.charAt(i)) {
                return current - other.charAt(i);
            }
        }
        if(buffer.get(position + length) == 0) {
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
        while((current = (char) buffer.get(position + bytePtr)) != 0) {
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