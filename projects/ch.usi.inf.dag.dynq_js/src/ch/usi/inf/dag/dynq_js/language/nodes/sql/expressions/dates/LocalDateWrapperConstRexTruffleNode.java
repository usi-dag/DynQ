package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.dates;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import ch.usi.inf.dag.dynq_js.runtime.types.LocalDateWrapper;

import java.time.LocalDate;


public final class LocalDateWrapperConstRexTruffleNode extends RexTruffleNode {

    private final LocalDateWrapper date;

    public LocalDateWrapperConstRexTruffleNode(LocalDate date) {
        this.date = new LocalDateWrapper(date);
    }

    @Override
    public LocalDateWrapper executeWith(VirtualFrame frame, Object input) {
        return date;
    }

}
