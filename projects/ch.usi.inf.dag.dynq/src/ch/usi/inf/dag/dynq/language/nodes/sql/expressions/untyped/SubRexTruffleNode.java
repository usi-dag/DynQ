package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;


import com.oracle.truffle.api.dsl.Specialization;

public abstract class SubRexTruffleNode extends BinaryRexForContainerTruffleNode {

    // int, double, long

    @Specialization
    public int runII(int left, int right) {
        return left - right;
    }

    @Specialization
    public double runDD(double left, double right) {
        return left - right;
    }

    @Specialization
    public long runLL(long left, long right) {
        return left - right;
    }


    // permutations

    @Specialization
    public double runID(int left, double right) {
        return left - right;
    }

    @Specialization
    public long runIL(int left, long right) {
        return left - right;
    }

    @Specialization
    public double runDI(double left, int right) {
        return left - right;
    }

    @Specialization
    public double runDL(double left, long right) {
        return left - right;
    }

    @Specialization
    public double runLD(long left, double right) {
        return left - right;
    }

    @Specialization
    public long runLI(long left, int right) {
        return left - right;
    }


}
