package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.constant_range_predicates;


import com.oracle.truffle.api.dsl.Specialization;


public abstract class DoubleInLeftOpenRange extends AbstractDoubleInRange {

    public DoubleInLeftOpenRange(double left, double right) {
        super(left, right);
    }

    @Specialization
    public boolean checkDouble(double value) {
        return left <= value && right > value;
    }

    @Specialization
    public boolean checkInt(int value) {
        return left <= value && right > value;
    }

    @Specialization
    public boolean checkLong(long value) {
        return left <= value && right > value;
    }

}
