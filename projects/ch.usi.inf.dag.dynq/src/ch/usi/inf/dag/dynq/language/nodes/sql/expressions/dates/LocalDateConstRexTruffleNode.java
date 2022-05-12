package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.dates;

import com.oracle.truffle.api.frame.VirtualFrame;

import java.time.LocalDate;


public final class LocalDateConstRexTruffleNode extends LocalDateRexTruffleNode {

    private final LocalDate date;

    public LocalDateConstRexTruffleNode(LocalDate date) {
        this.date = date;
    }

    @Override
    public LocalDate executeWith(VirtualFrame frame, Object input) {
        return date;
    }

    @Override
    public String explain() {
        return super.explain() + "(" + date + ")";
    }
}
