package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped.interop;


import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped.InteropReaderRexTruffleNode;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.untyped.InteropReaderRexTruffleNodeGen;

public class InteropReaderFactory {

    public static InteropReaderRexTruffleNode get(String fieldName) {
        return InteropReaderRexTruffleNodeGen.create(fieldName);
    }

}
