package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.runtime.types.InternalDateWrapper;
import ch.usi.inf.dag.dynq.runtime.types.LocalDateWrapper;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;

import java.time.LocalDate;


public abstract class LessThanRexTruffleNode extends BinaryRexForContainerTruffleNode {

    // primitives

    @Specialization
    boolean runInt(int left, int right) {
        return left < right;
    }

    @Specialization
    boolean runLong(long left, long right) {
        return left < right;
    }

    @Specialization
    boolean runDouble(double left, double right) {
        return left < right;
    }



    // permutations

    @Specialization
    public boolean runID(int left, double right) {
        return left < right;
    }

    @Specialization
    public boolean runIL(int left, long right) {
        return left < right;
    }

    @Specialization
    public boolean runDI(double left, int right) {
        return left < right;
    }

    @Specialization
    public boolean runDL(double left, long right) {
        return left < right;
    }

    @Specialization
    public boolean runLD(long left, double right) {
        return left < right;
    }

    @Specialization
    public boolean runLI(long left, int right) {
        return left < right;
    }


    // objects

    @Specialization
    boolean runLocalDate(LocalDate left, LocalDate right) {
        return left.isBefore(right);
    }


    @Specialization
    public boolean runLocalDateWrapper(LocalDateWrapper left, LocalDateWrapper right) {
        return left.getDaysOfEpoch() < right.getDaysOfEpoch();
    }

    @Specialization
    public boolean runInternalDateWrapper(InternalDateWrapper left, InternalDateWrapper right) {
        return left.getDaysOfEpoch() < right.getDaysOfEpoch();
    }

    // permutations
    @Specialization
    public boolean runDateWrapperIwLw(InternalDateWrapper left, LocalDateWrapper right) {
        return left.getDaysOfEpoch() < right.getDaysOfEpoch();
    }
    @Specialization
    public boolean runDateWrapperLwIw(LocalDateWrapper left, InternalDateWrapper right) {
        return left.getDaysOfEpoch() < right.getDaysOfEpoch();
    }
    @Specialization
    public boolean runDateWrapperLwL(LocalDateWrapper left, LocalDate right) {
        return left.getDate().isBefore(right);
    }
    @Specialization
    public boolean runDateWrapperLLw(LocalDate left, LocalDateWrapper right) {
        return left.isBefore(right.getDate());
    }

    // TODO Fallback or Specialization?
    // TODO should this return null?
    @Fallback
    boolean runGeneric(Object left, Object right) {
        CompilerDirectives.transferToInterpreter();
        throw new RuntimeException("Cannot run generic comparison: left=" + left.getClass() + " right=" + right.getClass());
    }


}
