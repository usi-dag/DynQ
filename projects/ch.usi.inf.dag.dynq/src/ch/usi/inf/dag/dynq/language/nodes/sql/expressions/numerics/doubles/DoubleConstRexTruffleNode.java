package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.doubles;

import com.oracle.truffle.api.frame.VirtualFrame;


public final class DoubleConstRexTruffleNode extends DoubleRexTruffleNode {

    final double value;

    public DoubleConstRexTruffleNode(double value) {
        this.value = value;
    }

    @Override
    public Double executeWith(VirtualFrame frame, Object input) {
        return value;
    }

    @Override
    public double runDouble(VirtualFrame frame, Object input) {
        return value;
    }

    @Override
    public String explain() {
        return super.explain() + "(" + value + ")";
    }
}
