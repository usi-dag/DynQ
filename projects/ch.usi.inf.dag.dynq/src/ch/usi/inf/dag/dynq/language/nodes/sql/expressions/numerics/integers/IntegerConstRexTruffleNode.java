package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.integers;


import com.oracle.truffle.api.frame.VirtualFrame;


public final class IntegerConstRexTruffleNode extends IntegerRexTruffleNode {
    final int value;

    public IntegerConstRexTruffleNode(int value) {
        this.value = value;
    }

    @Override
    public Integer executeWith(VirtualFrame frame, Object input) {
        return value;
    }

    @Override
    public int runInt(VirtualFrame frame, Object input) {
        return value;
    }

    @Override
    public String explain() {
        return super.explain() + "(" + value + ")";
    }

}
