package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.constant_range_predicates;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;


abstract class AbstractDoubleInRange extends RexTruffleNode { // TODO BooleanRexTruffleNode?

    @CompilerDirectives.CompilationFinal
    final double left;

    @CompilerDirectives.CompilationFinal
    final double right;

    public AbstractDoubleInRange(double left, double right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String explain() {
        return String.format("%s(%f, %f)", super.explain(), left, right);
    }
}
