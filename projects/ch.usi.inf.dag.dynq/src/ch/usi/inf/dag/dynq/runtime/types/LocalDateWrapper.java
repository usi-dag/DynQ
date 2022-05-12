package ch.usi.inf.dag.dynq.runtime.types;

import java.time.LocalDate;


public class LocalDateWrapper {

    private final LocalDate date;
    private final long daysOfEpoch;

    public LocalDateWrapper(LocalDate date) {
        this.date = date;
        this.daysOfEpoch = date.toEpochDay();
    }

    public LocalDate getDate() {
        return date;
    }

    public long getDaysOfEpoch() {
        return daysOfEpoch;
    }

    @Override
    public String toString() {
        return date.toString();
    }
}