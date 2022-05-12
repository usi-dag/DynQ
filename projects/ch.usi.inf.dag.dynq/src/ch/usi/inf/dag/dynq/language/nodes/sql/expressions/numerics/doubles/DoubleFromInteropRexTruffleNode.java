package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.doubles;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;


public abstract class DoubleFromInteropRexTruffleNode extends DoubleRexTruffleNode  {

    private final String member;

    public DoubleFromInteropRexTruffleNode(String member) {
        this.member = member;
    }

    @Specialization
    public Double executeCached(Object input,
                                @CachedLibrary(limit = "1") InteropLibrary interopRead,
                                @CachedLibrary(limit = "1") InteropLibrary interopAsDouble) throws InteropException {
        if(input == null) {
            return null;
        }
        return interopAsDouble.asDouble(interopRead.readMember(input, member));
    }


    @Override
    public String explain() {
        return super.explain() + "(" + member + ")" ;
    }

}
