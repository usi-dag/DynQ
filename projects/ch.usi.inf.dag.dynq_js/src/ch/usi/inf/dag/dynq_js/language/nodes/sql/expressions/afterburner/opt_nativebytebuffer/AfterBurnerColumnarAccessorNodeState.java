package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

class AfterBurnerColumnarAccessorNodeState {

    static final Unsafe UNSAFE;
    static final boolean ENABLED;

    static {
        boolean enabled;
        Unsafe unsafe;
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            enabled = true;
        } catch (Exception e) {
            enabled = false;
            unsafe = null;
        }
        ENABLED = enabled;
        UNSAFE = unsafe;
    }

    final int offset;
    final long address;

    public AfterBurnerColumnarAccessorNodeState(ByteBuffer buffer, int offset) {
        this.offset = offset;
        address = ((DirectBuffer) buffer).address();
    }

    int getPosition(int index) {
        return (offset + (index << 2)) >> 2;
    }

    byte read(int input) {
        return UNSAFE.getByte(address + input);
    }

    int readInt(int input) {
        return UNSAFE.getInt(address + getPosition(input) * 4);
    }

    // Note: in AfterBurner doubles are actually floats
    double readDouble(int input) {
        return UNSAFE.getFloat(address + getPosition(input) * 4);
    }
}
