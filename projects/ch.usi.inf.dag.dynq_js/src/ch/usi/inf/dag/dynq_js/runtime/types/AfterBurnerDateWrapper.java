package ch.usi.inf.dag.dynq_js.runtime.types;

import ch.usi.inf.dag.dynq.runtime.objects.resultsets.ShipToDynLangWithToString;
import com.oracle.truffle.api.interop.TruffleObject;

import java.time.LocalDate;


public final class AfterBurnerDateWrapper implements Comparable<Object>, TruffleObject, ShipToDynLangWithToString {

    private final int daysOfEpoch;
    private LocalDate localDate = null;

    public AfterBurnerDateWrapper(int daysOfEpoch) {
        this.daysOfEpoch = daysOfEpoch;
    }

    public int getDaysOfEpoch() {
        return daysOfEpoch;
    }

    public LocalDate asLocalDate() {
        if(localDate != null) {
            return localDate;
        }
        return localDate = LocalDate.ofEpochDay(daysOfEpoch);
    }

    // TODO this is fine as a fallback, but these specializations should be added in comparator nodes (e.g., lessthan)
    @Override
    public int compareTo(Object obj) {
        if(obj instanceof AfterBurnerDateWrapper) {
            return compareTo((AfterBurnerDateWrapper) obj);
        }
        if(obj instanceof LocalDateWrapper) {
            return compareTo((LocalDateWrapper) obj);
        }
        throw new ClassCastException();
    }

    @Override
    public int hashCode() {
        return daysOfEpoch;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AfterBurnerDateWrapper) {
            return daysOfEpoch == ((AfterBurnerDateWrapper) obj).daysOfEpoch;
        }
        if(obj instanceof LocalDateWrapper) {
            return daysOfEpoch == ((LocalDateWrapper) obj).getDaysOfEpoch();
        }
        return false;
    }

    public int compareTo(AfterBurnerDateWrapper other) {
        return Integer.compare(daysOfEpoch, other.daysOfEpoch);
    }

    public int compareTo(LocalDateWrapper other) {
        return Long.compare(daysOfEpoch, other.getDaysOfEpoch());
    }

    @Override
    public String toString() {
        return asLocalDate().toString();
    }
}
