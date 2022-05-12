package ch.usi.inf.dag.dynq.runtime.objects.data;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;


@ExportLibrary(InteropLibrary.class)
public class DynQNullValue implements TruffleObject {
    private DynQNullValue(){}

    public static final Object INSTANCE = new DynQNullValue();

    @ExportMessage
    public boolean isNull() {
        return true;
    }
}
