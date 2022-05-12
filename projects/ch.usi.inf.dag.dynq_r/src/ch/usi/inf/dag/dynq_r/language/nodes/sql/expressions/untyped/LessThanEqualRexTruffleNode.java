package ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.BinaryRexForContainerTruffleNode;
import ch.usi.inf.dag.dynq.runtime.types.InternalDateWrapper;
import ch.usi.inf.dag.dynq.runtime.types.LocalDateWrapper;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;

import java.time.LocalDate;


public abstract class LessThanEqualRexTruffleNode extends BinaryRexForContainerTruffleNode {

    // primitives

    @Specialization
    boolean runInt(int left, int right) {
        return left <= right;
    }

    @Specialization
    boolean runLong(long left, long right) {
        return left <= right;
    }

    @Specialization
    boolean runDouble(double left, double right) {
        return left <= right;
    }

    @Specialization
    boolean runBoolean(boolean left, boolean right) {
        int leftVal = left ? 1 : 0;
        int rightVal = right ? 1 : 0;
        return leftVal <= rightVal;
    }


    // permutations

    @Specialization
    public boolean runID(int left, double right) {
        return left <= right;
    }

    @Specialization
    public boolean runIL(int left, long right) {
        return left <= right;
    }

    @Specialization
    public boolean runDI(double left, int right) {
        return left <= right;
    }

    @Specialization
    public boolean runDL(double left, long right) {
        return left <= right;
    }

    @Specialization
    public boolean runLD(long left, double right) {
        return left <= right;
    }

    @Specialization
    public boolean runLI(long left, int right) {
        return left <= right;
    }

    // dates

    @Specialization
    public boolean runLocalDate(LocalDate left, LocalDate right) {
        return left.compareTo(right) <= 0;
    }

    @Specialization
    public boolean runLocalDateWrapper(LocalDateWrapper left, LocalDateWrapper right) {
        return left.getDaysOfEpoch() <= right.getDaysOfEpoch();
    }

    @Specialization
    public boolean runInternalDateWrapper(InternalDateWrapper left, InternalDateWrapper right) {
        return left.getDaysOfEpoch() <= right.getDaysOfEpoch();
    }

    // permutations
    @Specialization
    public boolean runDateWrapperIwLw(InternalDateWrapper left, LocalDateWrapper right) {
        return left.getDaysOfEpoch() <= right.getDaysOfEpoch();
    }
    @Specialization
    public boolean runDateWrapperLwIw(LocalDateWrapper left, InternalDateWrapper right) {
        return left.getDaysOfEpoch() <= right.getDaysOfEpoch();
    }
    @Specialization
    public boolean runDateWrapperLwL(LocalDateWrapper left, LocalDate right) {
        return left.getDate().compareTo(right) <= 0;
    }
    @Specialization
    public boolean runDateWrapperLLw(LocalDate left, LocalDateWrapper right) {
        return left.compareTo(right.getDate()) <= 0;
    }

    // R specific stuff
    @Specialization
    boolean runLocalDateLeftR(LocalDateWrapper left, Double right) {
        return left.getDaysOfEpoch() <= right;
    }

    @Specialization
    boolean runLocalDateRightR(Double left, LocalDateWrapper right) {
        return left <= right.getDaysOfEpoch();
    }

    @Specialization
        // TODO Fallback instead of Specialization?
    boolean runGeneric(Object left, Object right) {
        CompilerDirectives.transferToInterpreter();
        throw new RuntimeException("Cannot run generic comparison: left=" + left.getClass() + " right=" + right.getClass());
    }

}
