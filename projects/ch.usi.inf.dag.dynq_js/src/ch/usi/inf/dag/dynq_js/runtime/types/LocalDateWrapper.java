package ch.usi.inf.dag.dynq_js.runtime.types;

import ch.usi.inf.dag.dynq_js.runtime.utils.JSInteropUtils;

import java.time.LocalDate;


public final class LocalDateWrapper {

    private final LocalDate date;
    private final long daysOfEpoch;
    private final double millisOfEpochDouble;

    public LocalDateWrapper(LocalDate date) {
        this.date = date;
        this.daysOfEpoch = date.toEpochDay();
        long millisOfEpoch = daysOfEpoch * JSInteropUtils.DAY_TO_MILLISECOND_MULTIPLIER;
        this.millisOfEpochDouble = (double) millisOfEpoch;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getMillisOfEpoch() {
        return millisOfEpochDouble;
    }

    public long getDaysOfEpoch() {
        return daysOfEpoch;
    }

    @Override
    public String toString() {
        return date.toString();
    }
}
