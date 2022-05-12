package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.BinaryRexForContainerTruffleNode;
import ch.usi.inf.dag.dynq_js.runtime.types.AfterBurnerDateWrapper;
import ch.usi.inf.dag.dynq_js.runtime.types.JSDateWrapper;
import ch.usi.inf.dag.dynq_js.runtime.types.LocalDateWrapper;
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

    // JS specific dates optimizations

    @Specialization
    boolean runLocalDateWrapper(LocalDateWrapper left, LocalDate right) {
        return left.getDate().isBefore(right);
    }

    @Specialization
    boolean runLocalDateWrapper(LocalDate left, LocalDateWrapper right) {
        return left.isBefore(right.getDate());
    }

    @Specialization
    boolean runLocalDateWrapperJS(LocalDateWrapper left, JSDateWrapper right) {
        return left.getMillisOfEpoch() < right.getMillisOfEpoch();
    }

    @Specialization
    boolean runLocalDateWrapperJS(JSDateWrapper left, LocalDateWrapper right) {
        return left.getMillisOfEpoch() < right.getMillisOfEpoch();
    }

    @Specialization
    boolean runLocalDateWrapperJS(JSDateWrapper left, JSDateWrapper right) {
        return left.getMillisOfEpoch() < right.getMillisOfEpoch();
    }

    // AfterBurner specific date optimizations
    @Specialization
    boolean runLocalDateWrapperJS(LocalDateWrapper left, AfterBurnerDateWrapper right) {
        return left.getDaysOfEpoch() < right.getDaysOfEpoch();
    }

    @Specialization
    boolean runLocalDateWrapperJS(AfterBurnerDateWrapper left, LocalDateWrapper right) {
        return left.getDaysOfEpoch() < right.getDaysOfEpoch();
    }

    @Specialization
    boolean runLocalDateWrapperJS(AfterBurnerDateWrapper left, AfterBurnerDateWrapper right) {
        return left.getDaysOfEpoch() < right.getDaysOfEpoch();
    }

    // nulls: TODO: fix - they should return null
    @Specialization(guards = "left == null")
    boolean runNullLeft(Object left, Object right) {
        return false;
    }
    @Specialization(guards = "right == null")
    boolean runNullRight(Object left, Object right) {
        return false;
    }

    // TODO Fallback or Specialization?
    // TODO should this return null?
    @Fallback
    boolean runGeneric(Object left, Object right) {
        CompilerDirectives.transferToInterpreter();
        throw new RuntimeException("Cannot run generic comparison: left=" + left.getClass() + " right=" + right.getClass());
    }


}
