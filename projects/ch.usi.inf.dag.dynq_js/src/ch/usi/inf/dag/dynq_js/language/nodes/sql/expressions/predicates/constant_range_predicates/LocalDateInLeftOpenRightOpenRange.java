package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.predicates.constant_range_predicates;


import ch.usi.inf.dag.dynq_js.runtime.types.JSDateWrapper;
import com.oracle.truffle.api.dsl.Specialization;

import java.time.LocalDate;


public abstract class LocalDateInLeftOpenRightOpenRange extends AbstractLocalDateInRange {

    public LocalDateInLeftOpenRightOpenRange(LocalDate left, LocalDate right) {
        super(left, right);
    }

    @Specialization
    public boolean checkRangeLocalDateWrapper(JSDateWrapper date) {
        return left.getMillisOfEpoch() <= date.getMillisOfEpoch() && right.getMillisOfEpoch() >= date.getMillisOfEpoch();
    }

    @Specialization
    public boolean checkRangeLocalDate(LocalDate date) {
        return left.getDate().compareTo(date) <= 0 && right.getDate().compareTo(date) >= 0;
    }

}
