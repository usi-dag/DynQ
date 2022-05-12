package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ArrayItemGetter extends RexTruffleNode {

    public static ArrayItemGetter create(int index) {
        return ArrayItemGetterNodeGen.create(index);
    }

    final int index;

    ArrayItemGetter(int index) {
        this.index = index;
    }

    @Specialization
    public Object executeArray(Object[] input) {
        return input[index];
    }

    @Specialization(guards = "input == null")
    public Object executeNull(Object input) {
        return null;
    }

    @Override
    public String explain() {
        return super.explain() + "(" + index + ")";
    }
}
