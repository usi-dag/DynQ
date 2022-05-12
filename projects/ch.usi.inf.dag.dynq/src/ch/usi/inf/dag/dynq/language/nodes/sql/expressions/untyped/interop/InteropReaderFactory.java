package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.interop;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.InteropReaderRexTruffleNode;

public class InteropReaderFactory {

    public static InteropReaderRexTruffleNode get(String fieldName) {
        return InteropReaderRexTruffleNode.create(fieldName);
    }

}
