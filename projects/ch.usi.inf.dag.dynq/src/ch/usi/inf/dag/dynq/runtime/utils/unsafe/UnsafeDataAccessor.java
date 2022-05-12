package ch.usi.inf.dag.dynq.runtime.utils.unsafe;

import sun.misc.Unsafe;

import static ch.usi.inf.dag.dynq.runtime.utils.unsafe.UnsafeUtils.UNSAFE;


public class UnsafeDataAccessor {


    final long address;

    public UnsafeDataAccessor(long address) {
        this.address = address;
    }

    public int readInt(int input) {
        return UNSAFE.getInt(address + (long) input * Unsafe.ARRAY_INT_INDEX_SCALE);
    }

    public double readDouble(int input) {
        return UNSAFE.getDouble(address + (long) input * Unsafe.ARRAY_DOUBLE_INDEX_SCALE);
    }

}
