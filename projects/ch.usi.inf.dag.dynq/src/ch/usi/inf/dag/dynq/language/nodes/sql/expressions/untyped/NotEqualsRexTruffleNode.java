package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;


import com.oracle.truffle.api.dsl.Specialization;

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
    public boolean runLocalDate(LocalDate left, LocalDate right) {
        return !left.isEqual(right);
    }

    @Specialization
    public boolean runGeneric(Object left, Object right) {
        return !left.equals(right);
    }

}
