package ch.usi.inf.dag.dynq.runtime.objects.data;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;


@ExportLibrary(InteropLibrary.class)
public class DynQStringValue implements TruffleObject {
    private final String string;

    private DynQStringValue(String string) {
        this.string = string;
    }

    public static DynQStringValue create(String string) {
        return new DynQStringValue(string);
    }

    @ExportMessage
    public boolean isString() {
        return true;
    }

    @ExportMessage
    public String asString() {
        return string;
    }

}
