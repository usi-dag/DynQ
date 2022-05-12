package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.constant_range_predicates;


import com.oracle.truffle.api.dsl.Specialization;

import java.time.LocalDate;


public abstract class LocalDateInLeftOpenRightOpenRange extends AbstractLocalDateInRange {

    public LocalDateInLeftOpenRightOpenRange(LocalDate left, LocalDate right) {
        super(left, right);
    }

    @Specialization
    public boolean checkRange(LocalDate date) {
        return left.compareTo(date) <= 0 && right.compareTo(date) >= 0;
    }

}
