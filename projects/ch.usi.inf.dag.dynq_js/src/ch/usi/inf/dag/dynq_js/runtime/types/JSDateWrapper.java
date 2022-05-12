package ch.usi.inf.dag.dynq_js.runtime.types;

import ch.usi.inf.dag.dynq.runtime.objects.resultsets.WrapsDynLangValue;
import ch.usi.inf.dag.dynq_js.runtime.utils.JSInteropUtils;
import com.oracle.truffle.api.object.DynamicObject;

import java.time.LocalDate;

public final class JSDateWrapper implements Comparable<JSDateWrapper>, WrapsDynLangValue {

    private final double millisOfEpoch;
    private final DynamicObject date;
    private LocalDate localDate = null;

    public JSDateWrapper(double daysOfEpoch, DynamicObject date) {
        this.millisOfEpoch = daysOfEpoch;
        this.date = date;
    }

    public double getMillisOfEpoch() {
        return millisOfEpoch;
    }

    public DynamicObject getDate() {
        return date;
    }

    @Override
    public Object getValue() {
        return date;
    }

    public LocalDate asLocalDate() {
        if(localDate != null) {
            return localDate;
        }
        return localDate = LocalDate.ofEpochDay((long)millisOfEpoch / JSInteropUtils.DAY_TO_MILLISECOND_MULTIPLIER);
    }

    @Override
    public int compareTo(JSDateWrapper o) {
        return Double.compare(millisOfEpoch, o.millisOfEpoch);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(millisOfEpoch);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof JSDateWrapper) {
            return millisOfEpoch == ((JSDateWrapper) obj).millisOfEpoch;
        }
        return false;
    }

    @Override
    public String toString() {
        return getDate().toString();
    }
}
