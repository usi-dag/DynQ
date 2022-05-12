package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.booleans;


import com.oracle.truffle.api.frame.VirtualFrame;


public final class BooleanConstRexTruffleNode extends BooleanRexTruffleNode {

    final boolean value;

    public BooleanConstRexTruffleNode(boolean value) {
        this.value = value;
    }

    @Override
    public Boolean executeWith(VirtualFrame frame, Object input) {
        return value;
    }

    @Override
    public boolean runBoolean(VirtualFrame frame, Object input) {
        return value;
    }

}
