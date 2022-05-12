package ch.usi.inf.dag.dynq.runtime.types;


import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import java.time.LocalDate;

@ExportLibrary(InteropLibrary.class)
@CompilerDirectives.ValueType
public class InternalDateWrapper implements Comparable<Object>, TruffleObject {

    private final int daysOfEpoch;
    private LocalDate localDate;

    public InternalDateWrapper(int value) {
        this.daysOfEpoch = value;
    }

    public int getDaysOfEpoch() {
        return daysOfEpoch;
    }

    public LocalDate getLocalDate() {
        if(localDate != null) {
            return localDate;
        }
        return localDate = LocalDate.ofEpochDay(daysOfEpoch);
    }

    @Override
    public int compareTo(Object obj) {
        // TODO this is fine as a fallback, but these specializations should be added in comparator nodes (e.g., lessthan)
        if(obj instanceof InternalDateWrapper) {
            return Integer.compare(daysOfEpoch, ((InternalDateWrapper) obj).daysOfEpoch);
        }
        if(obj instanceof LocalDateWrapper) {
            return Long.compare(daysOfEpoch, ((LocalDateWrapper) obj).getDaysOfEpoch());
        }
        CompilerDirectives.transferToInterpreter();
        throw new IllegalArgumentException("Cannot compare InternalDateWrapper with " + obj.getClass());
    }


    @ExportMessage
    public boolean isDate() {
        return true;
    }

    @ExportMessage
    public LocalDate asDate() {
        return getLocalDate();
    }

    @Override
    public String toString() {
        return getLocalDate().toString();
    }

}
