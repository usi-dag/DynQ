package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.integers;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;


public abstract class IntegerFromInteropRexTruffleNode extends IntegerRexTruffleNode  {

    private final String member;

    public IntegerFromInteropRexTruffleNode(String member) {
        this.member = member;
    }

    @Specialization
    public Integer executeCached(Object input,
                                 @CachedLibrary(limit = "1") InteropLibrary interopRead,
                                 @CachedLibrary(limit = "1") InteropLibrary interopAsDate) throws InteropException {
        if(input == null) {
            return null;
        }
        return interopAsDate.asInt(interopRead.readMember(input, member));
    }


    @Override
    public String explain() {
        return super.explain() + "(" + member + ")" ;
    }

}
