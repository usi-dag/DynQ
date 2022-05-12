package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;


import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class DivRexTruffleNode extends BinaryRexForContainerTruffleNode {

    // int, double, long

    @Specialization
    public double runII(int left, int right) {
        return left / (double) right;
    }

    @Specialization
    public double runDD(double left, double right) {
        return left / right;
    }

    @Specialization
    public double runLL(long left, long right) {
        return left / (double) right;
    }


    // permutations

    @Specialization
    public double runID(int left, double right) {
        return left / right;
    }

    @Specialization
    public double runIL(int left, long right) {
        return left / (double) right;
    }

    @Specialization
    public double runDI(double left, int right) {
        return left / right;
    }

    @Specialization
    public double runDL(double left, long right) {
        return left / right;
    }

    @Specialization
    public double runLD(long left, double right) {
        return left / right;
    }

    @Specialization
    public double runLI(long left, int right) {
        return left / (double) right;
    }

    // something may be null

    @Fallback
    public Object runNull(Object left, Object right) {
        if(left == null || right == null) {
            return null;
        }
        CompilerDirectives.transferToInterpreter();
        throw new RuntimeException("Should not reach here: " + left + " / " + right);
    }


}
