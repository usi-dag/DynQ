package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.predicates.constant_range_predicates;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq_js.runtime.types.LocalDateWrapper;
import com.oracle.truffle.api.CompilerDirectives;

import java.time.LocalDate;


abstract class AbstractLocalDateInRange extends RexTruffleNode { // TODO BooleanRexTruffleNode?

    @CompilerDirectives.CompilationFinal
    final LocalDateWrapper left;

    @CompilerDirectives.CompilationFinal
    final LocalDateWrapper right;

    public AbstractLocalDateInRange(LocalDate left, LocalDate right) {
        this.left = new LocalDateWrapper(left);
        this.right = new LocalDateWrapper(right);
    }

    @Override
    public String explain() {
        return String.format("%s(%s, %s)", super.explain(), left, right);
    }
}
