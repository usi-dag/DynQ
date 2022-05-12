package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.predicates.constant_range_predicates;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.CompilerDirectives;

import java.time.LocalDate;


abstract class AbstractLocalDateInRange extends RexTruffleNode { // TODO BooleanRexTruffleNode?

    @CompilerDirectives.CompilationFinal
    final LocalDate left;

    @CompilerDirectives.CompilationFinal
    final LocalDate right;

    public AbstractLocalDateInRange(LocalDate left, LocalDate right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String explain() {
        return String.format("%s(%s, %s)", super.explain(), left, right);
    }
}
