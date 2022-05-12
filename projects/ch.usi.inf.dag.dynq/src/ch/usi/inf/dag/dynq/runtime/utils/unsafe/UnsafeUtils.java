package ch.usi.inf.dag.dynq.runtime.utils.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtils {

    public static final Unsafe UNSAFE;

    static {
        Unsafe unsafe;
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            unsafe = null;
        }
        UNSAFE = unsafe;
    }

}
