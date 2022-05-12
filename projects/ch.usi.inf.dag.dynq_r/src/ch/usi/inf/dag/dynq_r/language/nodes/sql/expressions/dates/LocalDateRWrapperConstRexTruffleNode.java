package ch.usi.inf.dag.dynq_r.language.nodes.sql.expressions.dates;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.runtime.types.LocalDateWrapper;
import com.oracle.truffle.api.frame.VirtualFrame;

import java.time.LocalDate;


public final class LocalDateRWrapperConstRexTruffleNode extends RexTruffleNode {

    private final LocalDateWrapper date;

    public LocalDateRWrapperConstRexTruffleNode(LocalDate date) {
        this.date = new LocalDateWrapper(date);
    }

    @Override
    public LocalDateWrapper executeWith(VirtualFrame frame, Object input) {
        return date;
    }

    @Override
    public String explain() {
        return super.explain() + "(" + date + ")";
    }
}
