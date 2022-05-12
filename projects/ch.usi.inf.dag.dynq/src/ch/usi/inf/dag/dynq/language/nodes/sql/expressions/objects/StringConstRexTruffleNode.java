package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class StringConstRexTruffleNode extends StringRexTruffleNode {

    private final String value;

    public StringConstRexTruffleNode(String value) {
        this.value = value;
    }

    @Override
    public String executeWith(VirtualFrame frame, Object input) {
        return value;
    }

}
