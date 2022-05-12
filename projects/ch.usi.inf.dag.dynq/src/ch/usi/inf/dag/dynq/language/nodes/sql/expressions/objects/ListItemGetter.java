package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.dsl.Specialization;

import java.util.List;


public abstract class ListItemGetter extends RexTruffleNode {

    final int index;

    ListItemGetter(int index) {
        this.index = index;
    }

    @Specialization
    public Object execute(List<?> input) {
        return input.get(index);
    }

    @Override
    public String explain() {
        return super.explain() + "(" + index + ")";
    }
}
