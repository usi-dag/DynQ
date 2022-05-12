package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.BinaryRexForContainerTruffleNode;
import com.oracle.truffle.api.dsl.Specialization;
import ch.usi.inf.dag.dynq_js.runtime.types.JSDateWrapper;
import ch.usi.inf.dag.dynq_js.runtime.types.LocalDateWrapper;

import java.time.LocalDate;

public abstract class NotEqualsRexTruffleNode extends BinaryRexForContainerTruffleNode {

    // int, double, long

    @Specialization
    public boolean runII(int left, int right) {
        return left != right;
    }

    @Specialization
    public boolean runDD(double left, double right) {
        return left != right;
    }

    @Specialization
    public boolean runLL(long left, long right) {
        return left != right;
    }


    // permutations

    @Specialization
    public boolean runID(int left, double right) {
        return left != right;
    }

    @Specialization
    public boolean runIL(int left, long right) {
        return left != right;
    }

    @Specialization
    public boolean runDI(double left, int right) {
        return left != right;
    }

    @Specialization
    public boolean runDL(double left, long right) {
        return left != right;
    }

    @Specialization
    public boolean runLD(long left, double right) {
        return left != right;
    }

    @Specialization
    public boolean runLI(long left, int right) {
        return left != right;
    }

    @Specialization
    boolean runLocalDateWrapper(LocalDate left, LocalDateWrapper right) {
        return !left.equals(right.getDate());
    }

    @Specialization
    public boolean runLocalDate(LocalDate left, LocalDate right) {
        return !left.isEqual(right);
    }

    @Specialization
    boolean runLocalDateWrapperJS(LocalDateWrapper left, JSDateWrapper right) {
        return left.getMillisOfEpoch() != right.getMillisOfEpoch();
    }

    @Specialization
    boolean runLocalDateWrapperJS(JSDateWrapper left, LocalDateWrapper right) {
        return left.getMillisOfEpoch() != right.getMillisOfEpoch();
    }

    @Specialization
    boolean runLocalDateWrapperJS(JSDateWrapper left, JSDateWrapper right) {
        return left.getMillisOfEpoch() != right.getMillisOfEpoch();
    }

    @Specialization
    public boolean runGeneric(Object left, Object right) {
        return !left.equals(right);
    }

}
