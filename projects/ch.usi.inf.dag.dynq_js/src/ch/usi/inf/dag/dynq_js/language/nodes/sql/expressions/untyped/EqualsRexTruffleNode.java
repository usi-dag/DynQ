package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.BinaryRexForContainerTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.afterburner.opt_nativebytebuffer.AfterBurnerStringWrapperNativeByteBufferUnsafe;
import ch.usi.inf.dag.dynq_js.runtime.types.JSDateWrapper;
import ch.usi.inf.dag.dynq_js.runtime.types.LocalDateWrapper;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;

import java.time.LocalDate;
import java.util.Objects;

public abstract class EqualsRexTruffleNode extends BinaryRexForContainerTruffleNode {

    @Override
    public abstract Boolean execute(Object left, Object right);

    // int, double, long

    @Specialization
    public boolean runII(int left, int right) {
        return left == right;
    }

    @Specialization
    public boolean runDD(double left, double right) {
        return left == right;
    }

    @Specialization
    public boolean runLL(long left, long right) {
        return left == right;
    }

    // permutations

    @Specialization
    public boolean runID(int left, double right) {
        return left == right;
    }

    @Specialization
    public boolean runIL(int left, long right) {
        return left == right;
    }

    @Specialization
    public boolean runDI(double left, int right) {
        return left == right;
    }

    @Specialization
    public boolean runDL(double left, long right) {
        return left == right;
    }

    @Specialization
    public boolean runLD(long left, double right) {
        return left == right;
    }

    @Specialization
    public boolean runLI(long left, int right) {
        return left == right;
    }

    // Objects

    @Specialization
    public boolean runString(String left, String right) {
        return TruffleBoundaryUtils.stringEquals(left, right);
    }


    @Specialization
    public boolean runAfterBurnerString(AfterBurnerStringWrapperNativeByteBufferUnsafe left, AfterBurnerStringWrapperNativeByteBufferUnsafe right) {
        return left.equals(right);
    }


    @Specialization
    public boolean runLocalDate(LocalDate left, LocalDate right) {
        return left.isEqual(right);
    }

    @Specialization
    boolean runLocalDateWrapper(LocalDate left, LocalDateWrapper right) {
        return left.equals(right.getDate());
    }

    @Specialization
    boolean runLocalDateWrapperJS(LocalDateWrapper left, JSDateWrapper right) {
        return left.getMillisOfEpoch() == right.getMillisOfEpoch();
    }

    @Specialization
    boolean runLocalDateWrapperJS(JSDateWrapper left, LocalDateWrapper right) {
        return left.getMillisOfEpoch() == right.getMillisOfEpoch();
    }

    @Specialization
    boolean runLocalDateWrapperJS(JSDateWrapper left, JSDateWrapper right) {
        return left.getMillisOfEpoch() == right.getMillisOfEpoch();
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

    @Fallback
    public boolean runGeneric(Object left, Object right) {
        return Objects.equals(left, right);
    }

}
