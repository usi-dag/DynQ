package ch.usi.inf.dag.dynq.language.nodes.sql.expressions.numerics.longs;


import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;


public abstract class LongFromInteropRexTruffleNode extends LongRexTruffleNode  {

    private final String member;


    public LongFromInteropRexTruffleNode(String member) {
        this.member = member;
    }

    @Specialization
    public Long executeCached(Object input,
                              @CachedLibrary(limit = "1") InteropLibrary interopRead,
                              @CachedLibrary(limit = "1") InteropLibrary interopAsLong) throws InteropException {
        if(input == null) {
            return null;
        }
        return interopAsLong.asLong(interopRead.readMember(input, member));
    }

    @Override
    public String explain() {
        return super.explain() + "(" + member + ")" ;
    }


}
