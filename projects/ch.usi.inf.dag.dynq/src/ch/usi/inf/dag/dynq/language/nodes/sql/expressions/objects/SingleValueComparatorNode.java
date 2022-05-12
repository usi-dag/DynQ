package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;


import ch.usi.inf.dag.dynq.language.nodes.utils.Explainable;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;

import java.time.LocalDate;


public abstract class SingleValueComparatorNode extends Node implements Explainable, SingleValueObjectComparatorNode {

    @CompilerDirectives.CompilationFinal
    private final boolean reversed;

    public SingleValueComparatorNode(boolean reversed) {
        this.reversed = reversed;
    }

    public abstract int execute(Object fst, Object snd);

    @Specialization
    public int compareInt(int x, int y) {
        return reverse(x - y);
    }

    @Specialization
    public int compareDouble(double x, double y) {
        return reverse(Double.compare(x, y));
    }

    @Specialization
    public int compareLong(long x, long y) {
        return reverse(Long.compare(x, y));
    }

    @Specialization
    public int compareString(String x, String y) {
        return reverse(x.compareTo(y));
    }

    @Specialization
    public int compareDate(LocalDate x, LocalDate y) {
        return reverse(x.compareTo(y));
    }

    @SuppressWarnings({"rawtypes", "unchecked"}) // TODO improve
    @Specialization
    public int compareComparableLeft(Comparable left, Object right) {
        return left.compareTo(right);
    }

    @Fallback
    public int compareObjects(Object left, Object right) {
        if(left == null || right == null) {
            if(left == null && right == null) {
                return 0;
            }
            // TODO should this be return null? Look at Comparable doc
        }
        throw new RuntimeException("Cannot run generic comparison: left=" + left.getClass() + " right=" + right.getClass());
    }

    private int reverse(int result) {
        return reversed ? -result : result;
    }
}
