package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped;


import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;


public abstract class InteropArrayItemGetterRexTruffleNode extends RexTruffleNode  {

    private final int index;

    public static InteropArrayItemGetterRexTruffleNode create(int index) {
        return InteropArrayItemGetterRexTruffleNodeGen.create(index);
    }

    InteropArrayItemGetterRexTruffleNode(int index) {
        this.index = index;
    }

    @Specialization(guards = "interop.hasArrayElements(input)")
    public Object read(Object input,
                       @CachedLibrary(limit = "1") InteropLibrary interop) throws InteropException {
        return interop.readArrayElement(input, index);
    }

    @Fallback()
    public Object notAnArray(Object unused) {
        return null;
    }

    @Override
    public String explain() {
        return super.explain() + "(" + index + ")" ;
    }

}
