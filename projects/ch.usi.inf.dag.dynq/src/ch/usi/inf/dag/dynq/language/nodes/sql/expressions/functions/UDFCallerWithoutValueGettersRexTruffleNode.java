package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.functions;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;


// TODO rename
// TODO generalize (w.r.t. UDFCallerRexTruffleNode)
public abstract class UDFCallerWithoutValueGettersRexTruffleNode extends RexTruffleNode {

    private final Object udf;

    UDFCallerWithoutValueGettersRexTruffleNode(Object udf) {
        this.udf = udf;
    }

    public static UDFCallerWithoutValueGettersRexTruffleNode create(Object udf) {
        return UDFCallerWithoutValueGettersRexTruffleNodeGen.create(udf);
    }

    // TODO (in DynQ_JS) optimize for JS function call, i.e., get rid of interop
    @Specialization
    Object call(Object row, @CachedLibrary(limit = "1") InteropLibrary interopCall)
            throws InteropException {
        return interopCall.execute(udf, row);
    }


}
