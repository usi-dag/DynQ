package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.objects;


import com.oracle.truffle.api.frame.VirtualFrame;


public final class CastToStringRexTruffleNode extends StringRexTruffleNode {

    @Override
    public String executeWith(VirtualFrame frame, Object input) {
        return input.toString();
    }

}
